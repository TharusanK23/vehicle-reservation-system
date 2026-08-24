package com.sunrise.vehiclereservation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload for an Admin editing an existing staff account. Deliberately scoped
 * to just full name and email - username, password and role are not editable
 * through this endpoint (username/role changes are a bigger identity/access
 * decision than a profile-detail correction, and password changes belong to
 * a dedicated reset flow, not a plain edit form).
 */
public record UpdateUserRequest(
        @NotBlank(message = "Full name is required") String fullName,
        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email
) {
}
