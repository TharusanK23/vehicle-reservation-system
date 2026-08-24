package com.sunrise.vehiclereservation.service;

import com.sunrise.vehiclereservation.dto.response.DashboardSummaryResponse;
import com.sunrise.vehiclereservation.dto.response.RevenueReportItem;
import com.sunrise.vehiclereservation.dto.response.VehicleUtilizationItem;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    DashboardSummaryResponse dashboardSummary();
    List<RevenueReportItem> dailyRevenue(LocalDate from, LocalDate to);
    List<VehicleUtilizationItem> vehicleUtilization();
}
