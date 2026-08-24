package com.sunrise.vehiclereservation.repository;

import com.sunrise.vehiclereservation.entity.Vehicle;
import com.sunrise.vehiclereservation.entity.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);

    List<Vehicle> findByStatus(VehicleStatus status);

    boolean existsByRegistrationNumber(String registrationNumber);

    /**
     * Vehicles that are not INACTIVE/MAINTENANCE and have no CONFIRMED/ONGOING/PENDING
     * reservation whose date range overlaps the requested [pickupDate, returnDate] window.
     */
    @Query("""
           SELECT v FROM Vehicle v
           WHERE v.status <> com.sunrise.vehiclereservation.entity.VehicleStatus.MAINTENANCE
             AND v.status <> com.sunrise.vehiclereservation.entity.VehicleStatus.INACTIVE
             AND (:categoryId IS NULL OR v.category.id = :categoryId)
             AND v.id NOT IN (
                 SELECT r.vehicle.id FROM Reservation r
                 WHERE r.status IN (com.sunrise.vehiclereservation.entity.ReservationStatus.PENDING,
                                     com.sunrise.vehiclereservation.entity.ReservationStatus.CONFIRMED,
                                     com.sunrise.vehiclereservation.entity.ReservationStatus.ONGOING)
                   AND r.pickupDate <= :returnDate
                   AND r.returnDate >= :pickupDate
             )
           """)
    List<Vehicle> findAvailableVehicles(@Param("pickupDate") LocalDate pickupDate,
                                         @Param("returnDate") LocalDate returnDate,
                                         @Param("categoryId") Long categoryId);
}
