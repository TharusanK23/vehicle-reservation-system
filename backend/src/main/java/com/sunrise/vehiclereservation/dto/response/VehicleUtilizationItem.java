package com.sunrise.vehiclereservation.dto.response;

public record VehicleUtilizationItem(String registrationNumber, String make, String model, long timesBooked) {
}
