package com.sunrise.vehiclereservation.dto.response;

public record LoginResponse(UserResponse user, long expiresInSeconds) {
}
