package com.sunrise.vehiclereservation.dto.response;

public record CustomerResponse(Long id, String fullName, String address, String contactNumber, String email, String licenseNumber) {
}
