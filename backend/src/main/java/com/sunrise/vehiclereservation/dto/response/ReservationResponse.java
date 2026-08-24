package com.sunrise.vehiclereservation.dto.response;

import com.sunrise.vehiclereservation.entity.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ReservationResponse(
        Long id,
        String reservationNumber,
        CustomerResponse customer,
        VehicleResponse vehicle,
        LocalDate pickupDate,
        LocalTime pickupTime,
        LocalDate returnDate,
        LocalTime returnTime,
        ReservationStatus status,
        String notes,
        String createdByUsername,
        LocalDateTime createdAt
) {
}
