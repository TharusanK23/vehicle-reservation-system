package com.sunrise.vehiclereservation.dto.response;

import com.sunrise.vehiclereservation.entity.Role;

public record UserResponse(Long id, String username, String fullName, String email, Role role) {
}
