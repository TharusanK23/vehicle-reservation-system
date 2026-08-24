package com.sunrise.vehiclereservation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateVehicleRequest(
        @NotBlank(message = "Registration number is required") String registrationNumber,
        @NotBlank(message = "Make is required") String make,
        @NotBlank(message = "Model is required") String model,
        @Min(value = 1990, message = "Year must be 1990 or later")
        @Max(value = 2100, message = "Year must be realistic")
        int manufactureYear,
        @NotNull(message = "Category is required") Long categoryId
) {
}
