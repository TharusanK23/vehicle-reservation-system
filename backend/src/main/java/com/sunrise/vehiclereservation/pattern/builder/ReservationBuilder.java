package com.sunrise.vehiclereservation.pattern.builder;

import com.sunrise.vehiclereservation.entity.Customer;
import com.sunrise.vehiclereservation.entity.Reservation;
import com.sunrise.vehiclereservation.entity.ReservationStatus;
import com.sunrise.vehiclereservation.entity.User;
import com.sunrise.vehiclereservation.entity.Vehicle;
import com.sunrise.vehiclereservation.pattern.singleton.ReservationNumberGenerator;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Builder pattern: assembles a {@link Reservation} through a fluent, step-by-step API
 * instead of a telescoping constructor. Centralises the cross-field validation rule
 * (return must not precede pickup) in one place so every entry point that creates a
 * reservation (REST controller today, a future console/batch importer tomorrow) is
 * guaranteed to build a consistent, valid object graph.
 */
public class ReservationBuilder {

    private Customer customer;
    private Vehicle vehicle;
    private LocalDate pickupDate;
    private LocalTime pickupTime;
    private LocalDate returnDate;
    private LocalTime returnTime;
    private String notes;
    private User createdBy;
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    public ReservationBuilder withCustomer(Customer customer) {
        this.customer = customer;
        return this;
    }

    public ReservationBuilder withVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        return this;
    }

    public ReservationBuilder withPickup(LocalDate date, LocalTime time) {
        this.pickupDate = date;
        this.pickupTime = time;
        return this;
    }

    public ReservationBuilder withReturn(LocalDate date, LocalTime time) {
        this.returnDate = date;
        this.returnTime = time;
        return this;
    }

    public ReservationBuilder withNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public ReservationBuilder withCreatedBy(User createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public ReservationBuilder withStatus(ReservationStatus status) {
        this.status = status;
        return this;
    }

    public Reservation build() {
        if (customer == null || vehicle == null || pickupDate == null || returnDate == null || createdBy == null) {
            throw new IllegalStateException("Customer, vehicle, pickup date, return date and createdBy are mandatory.");
        }
        if (returnDate.isBefore(pickupDate)) {
            throw new IllegalStateException("Return date cannot be before the pickup date.");
        }
        if (returnDate.isEqual(pickupDate) && returnTime != null && pickupTime != null && !returnTime.isAfter(pickupTime)) {
            throw new IllegalStateException("Return time must be after pickup time on a same-day rental.");
        }

        return Reservation.builder()
                .reservationNumber(ReservationNumberGenerator.getInstance().nextReservationNumber())
                .customer(customer)
                .vehicle(vehicle)
                .pickupDate(pickupDate)
                .pickupTime(pickupTime)
                .returnDate(returnDate)
                .returnTime(returnTime)
                .notes(notes)
                .createdBy(createdBy)
                .status(status)
                .build();
    }
}
