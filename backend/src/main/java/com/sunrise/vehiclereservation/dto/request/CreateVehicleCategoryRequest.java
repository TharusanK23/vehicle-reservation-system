package com.sunrise.vehiclereservation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateVehicleCategoryRequest(
        @NotBlank(message = "Category name is required") String categoryName,
        @NotNull(message = "Daily rate is required")
        @DecimalMin(value = "0.01", message = "Daily rate must be greater than zero")
        BigDecimal dailyRate,
        String description
) {
}
