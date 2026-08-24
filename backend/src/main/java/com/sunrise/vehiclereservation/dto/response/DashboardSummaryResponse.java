package com.sunrise.vehiclereservation.dto.response;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        long totalVehicles,
        long availableVehicles,
        long totalCustomers,
        long activeReservations,
        long todaysPickups,
        BigDecimal totalRevenue,
        BigDecimal unpaidAmount
) {
}
