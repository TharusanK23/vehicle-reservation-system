package com.sunrise.vehiclereservation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SettlePaymentRequest(
        @NotBlank(message = "Payment method is required") String paymentMethod
) {
}
