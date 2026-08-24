package com.sunrise.vehiclereservation.dto.response;

import com.sunrise.vehiclereservation.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BillResponse(
        Long id,
        String billNumber,
        ReservationResponse reservation,
        int numberOfDays,
        BigDecimal dailyRate,
        BigDecimal subtotal,
        BigDecimal surchargeAmount,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String pricingStrategy,
        PaymentStatus paymentStatus,
        String paymentMethod,
        LocalDateTime generatedAt
) {
}
