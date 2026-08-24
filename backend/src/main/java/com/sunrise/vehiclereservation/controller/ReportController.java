package com.sunrise.vehiclereservation.controller;

import com.sunrise.vehiclereservation.dto.response.DashboardSummaryResponse;
import com.sunrise.vehiclereservation.dto.response.RevenueReportItem;
import com.sunrise.vehiclereservation.dto.response.VehicleUtilizationItem;
import com.sunrise.vehiclereservation.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Decision-support reports (dashboard KPIs, daily revenue via stored procedure, vehicle utilisation via a database view). */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    public DashboardSummaryResponse dashboard() {
        return reportService.dashboardSummary();
    }

    @GetMapping("/revenue")
    public List<RevenueReportItem> revenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportService.dailyRevenue(from, to);
    }

    @GetMapping("/vehicle-utilization")
    public List<VehicleUtilizationItem> vehicleUtilization() {
        return reportService.vehicleUtilization();
    }
}
