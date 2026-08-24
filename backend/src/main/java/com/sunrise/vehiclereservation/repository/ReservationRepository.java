package com.sunrise.vehiclereservation.repository;

import com.sunrise.vehiclereservation.entity.Reservation;
import com.sunrise.vehiclereservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReservationNumber(String reservationNumber);

    boolean existsByReservationNumber(String reservationNumber);

    List<Reservation> findByStatus(ReservationStatus status);

    List<Reservation> findByCustomerId(Long customerId);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.pickupDate = :date")
    long countByPickupDate(@Param("date") LocalDate date);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.vehicle.id = :vehicleId
             AND r.status IN (com.sunrise.vehiclereservation.entity.ReservationStatus.PENDING,
                               com.sunrise.vehiclereservation.entity.ReservationStatus.CONFIRMED,
                               com.sunrise.vehiclereservation.entity.ReservationStatus.ONGOING)
             AND r.pickupDate <= :returnDate
             AND r.returnDate >= :pickupDate
           """)
    List<Reservation> findOverlappingForVehicle(@Param("vehicleId") Long vehicleId,
                                                 @Param("pickupDate") LocalDate pickupDate,
                                                 @Param("returnDate") LocalDate returnDate);
}
