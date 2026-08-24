package com.sunrise.vehiclereservation.dto.response;

import com.sunrise.vehiclereservation.entity.VehicleStatus;

public record VehicleResponse(Long id, String registrationNumber, String make, String model, int manufactureYear,
                               VehicleCategoryResponse category, VehicleStatus status) {
}
