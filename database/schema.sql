-- =============================================================================
-- Sunrise Vehicle Rentals - Online Vehicle Reservation System
-- CIS6003 Advanced Programming coursework
--
-- Full database schema: tables, constraints, triggers, a stored procedure, a
-- function and reporting views, plus seed data so the system is immediately
-- usable after import.
--
-- HOW TO RUN (see docs/SETUP.md for full details):
--   Option A (phpMyAdmin):  Import this file against a new/blank server.
--   Option B (command line, from the XAMPP mysql/bin folder):
--       mysql -u root < schema.sql
-- =============================================================================

CREATE DATABASE IF NOT EXISTS vehicle_reservation_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE vehicle_reservation_db;

SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------------------------
-- Drop existing objects so this script can be re-run idempotently.
-- -----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_prevent_double_booking;
DROP TRIGGER IF EXISTS trg_sync_vehicle_status;
DROP PROCEDURE IF EXISTS sp_daily_revenue_report;
DROP FUNCTION IF EXISTS fn_calculate_rental_days;
DROP VIEW IF EXISTS vw_vehicle_utilization;
DROP VIEW IF EXISTS vw_reservation_summary;

DROP TABLE IF EXISTS notification_logs;
DROP TABLE IF EXISTS bills;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS vehicles;
DROP TABLE IF EXISTS vehicle_categories;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- -----------------------------------------------------------------------------
-- Tables
-- -----------------------------------------------------------------------------

CREATE TABLE users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    enabled     TINYINT(1)   NOT NULL DEFAULT 1,
    created_at  DATETIME     NOT NULL,
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'STAFF'))
) ENGINE = InnoDB;

CREATE TABLE customers (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name       VARCHAR(100) NOT NULL,
    address         VARCHAR(200) NOT NULL,
    contact_number  VARCHAR(20)  NOT NULL,
    email           VARCHAR(100),
    license_number  VARCHAR(30),
    created_at      DATETIME     NOT NULL
) ENGINE = InnoDB;

CREATE TABLE vehicle_categories (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name  VARCHAR(50)    NOT NULL UNIQUE,
    daily_rate     DECIMAL(10, 2) NOT NULL,
    description    VARCHAR(255),
    CONSTRAINT chk_categories_rate CHECK (daily_rate > 0)
) ENGINE = InnoDB;

CREATE TABLE vehicles (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    registration_number   VARCHAR(20)  NOT NULL UNIQUE,
    make                  VARCHAR(50)  NOT NULL,
    model                 VARCHAR(50)  NOT NULL,
    manufacture_year      INT          NOT NULL,
    category_id           BIGINT       NOT NULL,
    status                VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
    image_url             VARCHAR(255),
    CONSTRAINT fk_vehicles_category FOREIGN KEY (category_id) REFERENCES vehicle_categories (id),
    CONSTRAINT chk_vehicles_status CHECK (status IN ('AVAILABLE', 'RESERVED', 'MAINTENANCE', 'INACTIVE')),
    CONSTRAINT chk_vehicles_year CHECK (manufacture_year BETWEEN 1990 AND 2100)
) ENGINE = InnoDB;

CREATE TABLE reservations (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_number  VARCHAR(20)  NOT NULL UNIQUE,
    customer_id         BIGINT       NOT NULL,
    vehicle_id          BIGINT       NOT NULL,
    pickup_date         DATE         NOT NULL,
    pickup_time         TIME         NOT NULL,
    return_date         DATE         NOT NULL,
    return_time         TIME         NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'CONFIRMED',
    notes               VARCHAR(255),
    created_by          BIGINT       NOT NULL,
    created_at          DATETIME     NOT NULL,
    CONSTRAINT fk_reservations_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_reservations_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id),
    CONSTRAINT fk_reservations_user FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT chk_reservations_status CHECK (status IN ('PENDING', 'CONFIRMED', 'ONGOING', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_reservations_dates CHECK (return_date >= pickup_date)
) ENGINE = InnoDB;

CREATE TABLE bills (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_number       VARCHAR(20)    NOT NULL UNIQUE,
    reservation_id    BIGINT         NOT NULL UNIQUE,
    number_of_days    INT            NOT NULL,
    daily_rate        DECIMAL(10, 2) NOT NULL,
    subtotal          DECIMAL(10, 2) NOT NULL,
    surcharge_amount  DECIMAL(10, 2) NOT NULL DEFAULT 0,
    discount_amount   DECIMAL(10, 2) NOT NULL DEFAULT 0,
    tax_amount        DECIMAL(10, 2) NOT NULL,
    total_amount      DECIMAL(10, 2) NOT NULL,
    pricing_strategy  VARCHAR(40),
    payment_status    VARCHAR(20)    NOT NULL DEFAULT 'UNPAID',
    payment_method    VARCHAR(30),
    generated_at      DATETIME       NOT NULL,
    CONSTRAINT fk_bills_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id),
    CONSTRAINT chk_bills_payment_status CHECK (payment_status IN ('UNPAID', 'PAID'))
) ENGINE = InnoDB;

CREATE TABLE notification_logs (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_number  VARCHAR(20)  NOT NULL,
    channel             VARCHAR(20)  NOT NULL,
    message             VARCHAR(500) NOT NULL,
    sent_at             DATETIME     NOT NULL
) ENGINE = InnoDB;

CREATE INDEX idx_reservations_pickup_date ON reservations (pickup_date);
CREATE INDEX idx_reservations_vehicle ON reservations (vehicle_id, status);
CREATE INDEX idx_notification_logs_reservation ON notification_logs (reservation_number);

-- -----------------------------------------------------------------------------
-- Function: fn_calculate_rental_days
-- Encapsulates the "at least 1 day" business rule for rental duration so it is
-- defined once and can be reused by any report or ad-hoc query, not only by the
-- Java BillFactory.
-- -----------------------------------------------------------------------------
DELIMITER $$
CREATE FUNCTION fn_calculate_rental_days(p_pickup DATE, p_return DATE)
    RETURNS INT
    DETERMINISTIC
BEGIN
    DECLARE days INT;
    SET days = DATEDIFF(p_return, p_pickup);
    IF days < 1 THEN
        SET days = 1;
    END IF;
    RETURN days;
END$$
DELIMITER ;

-- -----------------------------------------------------------------------------
-- Trigger: trg_prevent_double_booking
-- Database-level safety net (in addition to the application-level check in
-- ReservationServiceImpl) guaranteeing a vehicle can never be double-booked,
-- even if a row were inserted by a client other than the REST API.
-- -----------------------------------------------------------------------------
DELIMITER $$
CREATE TRIGGER trg_prevent_double_booking
    BEFORE INSERT
    ON reservations
    FOR EACH ROW
BEGIN
    DECLARE conflict_count INT;

    SELECT COUNT(*)
    INTO conflict_count
    FROM reservations r
    WHERE r.vehicle_id = NEW.vehicle_id
      AND r.status IN ('PENDING', 'CONFIRMED', 'ONGOING')
      AND r.pickup_date <= NEW.return_date
      AND r.return_date >= NEW.pickup_date;

    IF conflict_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Double booking rejected: this vehicle already has an overlapping reservation.';
    END IF;
END$$
DELIMITER ;

-- -----------------------------------------------------------------------------
-- Trigger: trg_sync_vehicle_status
-- Keeps vehicles.status consistent with the reservation lifecycle as a database
-- invariant: COMPLETED/CANCELLED frees the vehicle; CONFIRMED/ONGOING reserves it.
-- -----------------------------------------------------------------------------
DELIMITER $$
CREATE TRIGGER trg_sync_vehicle_status
    AFTER UPDATE
    ON reservations
    FOR EACH ROW
BEGIN
    IF NEW.status IN ('COMPLETED', 'CANCELLED') AND OLD.status <> NEW.status THEN
        UPDATE vehicles SET status = 'AVAILABLE' WHERE id = NEW.vehicle_id;
    ELSEIF NEW.status IN ('CONFIRMED', 'ONGOING') AND OLD.status <> NEW.status THEN
        UPDATE vehicles SET status = 'RESERVED' WHERE id = NEW.vehicle_id;
    END IF;
END$$
DELIMITER ;

-- -----------------------------------------------------------------------------
-- Stored procedure: sp_daily_revenue_report
-- Used by ReportServiceImpl.dailyRevenue() to power the admin "Revenue Report"
-- screen: total billed revenue and reservation count per calendar day in range.
-- -----------------------------------------------------------------------------
DELIMITER $$
CREATE PROCEDURE sp_daily_revenue_report(IN p_start_date DATE, IN p_end_date DATE)
BEGIN
    SELECT DATE(b.generated_at)      AS report_date,
           SUM(b.total_amount)       AS total_revenue,
           COUNT(*)                  AS reservation_count
    FROM bills b
    WHERE DATE(b.generated_at) BETWEEN p_start_date AND p_end_date
    GROUP BY DATE(b.generated_at)
    ORDER BY report_date;
END$$
DELIMITER ;

-- -----------------------------------------------------------------------------
-- Views
-- -----------------------------------------------------------------------------

-- Used by ReportServiceImpl.vehicleUtilization() to power the "Vehicle Utilisation" report.
CREATE VIEW vw_vehicle_utilization AS
SELECT v.registration_number,
       v.make,
       v.model,
       COUNT(r.id) AS times_booked
FROM vehicles v
         LEFT JOIN reservations r ON r.vehicle_id = v.id AND r.status <> 'CANCELLED'
GROUP BY v.id, v.registration_number, v.make, v.model;

-- A consolidated, denormalised read model joining every table together - handy for
-- ad-hoc reporting/export straight out of phpMyAdmin without writing a fresh join each time.
CREATE VIEW vw_reservation_summary AS
SELECT r.reservation_number,
       c.full_name                          AS customer_name,
       c.contact_number,
       v.registration_number,
       v.make,
       v.model,
       vc.category_name,
       r.pickup_date,
       r.pickup_time,
       r.return_date,
       r.return_time,
       r.status                             AS reservation_status,
       b.bill_number,
       b.total_amount,
       b.payment_status,
       u.username                           AS booked_by
FROM reservations r
         JOIN customers c ON c.id = r.customer_id
         JOIN vehicles v ON v.id = r.vehicle_id
         JOIN vehicle_categories vc ON vc.id = v.category_id
         JOIN users u ON u.id = r.created_by
         LEFT JOIN bills b ON b.reservation_id = r.id;

-- -----------------------------------------------------------------------------
-- Seed data
-- -----------------------------------------------------------------------------

-- Default staff accounts. Passwords are BCrypt-hashed (never store plain text).
--   admin / admin123   (ADMIN  - full access incl. fleet & staff management, all reports)
--   staff1 / staff123  (STAFF  - day-to-day booking & billing operations)
INSERT INTO users (username, password, full_name, email, role, enabled, created_at) VALUES
    ('admin',  '$2b$10$7ADQs/tyhEp55w/XFyYAl.uewWgBwFgqiBXNvX5/yvliBicKzxJgS', 'System Administrator', 'admin@sunrisevehicles.lk',  'ADMIN', 1, NOW()),
    ('staff1', '$2b$10$4pFWxXn1Xtpz0TfHEnwND.IzbYUyMBxwy/LNBO7Xdl3JDJ2k3UK22', 'Nimali Perera',         'nimali@sunrisevehicles.lk', 'STAFF', 1, NOW());

INSERT INTO vehicle_categories (category_name, daily_rate, description) VALUES
    ('Economy', 4500.00, 'Compact, fuel-efficient cars ideal for city driving (e.g. Toyota Aqua, Suzuki Alto).'),
    ('Sedan',   6500.00, 'Comfortable mid-size saloons suitable for business travel (e.g. Toyota Allion, Honda Civic).'),
    ('SUV',     9500.00, 'Spacious sport-utility vehicles for families and rough terrain (e.g. Toyota Prado, Suzuki Vitara).'),
    ('Van',     8500.00, 'Passenger vans for group travel and airport transfers (e.g. Toyota KDH).'),
    ('Luxury', 15000.00, 'Premium vehicles for VIP transport and special occasions (e.g. Mercedes-Benz E-Class).');

INSERT INTO vehicles (registration_number, make, model, manufacture_year, category_id, status) VALUES
    ('CAB-1234', 'Toyota',  'Aqua',    2019, 1, 'AVAILABLE'),
    ('CAJ-5566', 'Suzuki',  'Alto',    2020, 1, 'AVAILABLE'),
    ('CAK-7788', 'Toyota',  'Allion',  2018, 2, 'AVAILABLE'),
    ('CAL-9900', 'Honda',   'Civic',   2021, 2, 'AVAILABLE'),
    ('CBA-1122', 'Toyota',  'Prado',   2020, 3, 'AVAILABLE'),
    ('CBC-3344', 'Suzuki',  'Vitara',  2019, 3, 'AVAILABLE'),
    ('CBD-5577', 'Toyota',  'KDH',     2017, 4, 'AVAILABLE'),
    ('CBE-8899', 'Mercedes-Benz', 'E-Class', 2022, 5, 'AVAILABLE'),
    ('CBF-2211', 'Toyota',  'Aqua',    2021, 1, 'MAINTENANCE');

INSERT INTO customers (full_name, address, contact_number, email, license_number, created_at) VALUES
    ('Kasun Fernando',   '12 Galle Road, Colombo 03',      '0771234567', 'kasun.fernando@example.com',   'B1234567', NOW()),
    ('Dilani Jayasuriya', '45 Kandy Road, Kadawatha',       '0719876543', 'dilani.j@example.com',         'B7654321', NOW()),
    ('Ruwan Silva',       '8 Temple Lane, Negombo',         '0754567890', 'ruwan.silva@example.com',      'B2468101', NOW());
