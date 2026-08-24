package com.sunrise.vehiclereservation.dto.response;

import java.math.BigDecimal;

public record VehicleCategoryResponse(Long id, String categoryName, BigDecimal dailyRate, String description) {
}
