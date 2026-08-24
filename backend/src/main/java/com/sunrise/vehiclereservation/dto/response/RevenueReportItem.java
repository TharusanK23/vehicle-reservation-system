package com.sunrise.vehiclereservation.dto.response;

import java.math.BigDecimal;

public record RevenueReportItem(String periodLabel, BigDecimal totalRevenue, long reservationCount) {
}
