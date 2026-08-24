package com.sunrise.vehiclereservation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Payload for "Register New Appointment" (adapted to the vehicle-reservation domain).
 * Either {@code customerId} is supplied to reuse an existing, previously-registered
 * customer, or the {@code customerFullName}/{@code customerAddress}/
 * {@code customerContactNumber} trio is supplied to register a new one inline -
 * mirroring the brief's requirement to capture patient/customer details as part of
 * booking a visit.
 */
public record RegisterReservationRequest(
        Long customerId,

        String customerFullName,
        String customerAddress,

        @Pattern(regexp = "^$|^(\\+94|0)[0-9]{9}$", message = "Contact number must be a valid Sri Lankan mobile/landline number")
        String customerContactNumber,

        @Email(message = "Email must be valid") String customerEmail,
        String customerLicenseNumber,

        @NotNull(message = "Vehicle is required") Long vehicleId,

        @NotNull(message = "Pickup date is required")
        @FutureOrPresent(message = "Pickup date cannot be in the past")
        LocalDate pickupDate,

        @NotNull(message = "Pickup time is required") LocalTime pickupTime,

        @NotNull(message = "Return date is required") LocalDate returnDate,

        @NotNull(message = "Return time is required") LocalTime returnTime,

        String notes
) {
}
