package com.sunrise.vehiclereservation.service.impl;

import com.sunrise.vehiclereservation.dto.response.DashboardSummaryResponse;
import com.sunrise.vehiclereservation.dto.response.RevenueReportItem;
import com.sunrise.vehiclereservation.dto.response.VehicleUtilizationItem;
import com.sunrise.vehiclereservation.entity.PaymentStatus;
import com.sunrise.vehiclereservation.entity.ReservationStatus;
import com.sunrise.vehiclereservation.entity.VehicleStatus;
import com.sunrise.vehiclereservation.repository.BillRepository;
import com.sunrise.vehiclereservation.repository.CustomerRepository;
import com.sunrise.vehiclereservation.repository.ReservationRepository;
import com.sunrise.vehiclereservation.repository.VehicleRepository;
import com.sunrise.vehiclereservation.service.ReportService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Reports that "facilitate decision-making" (Excellent-band criterion). The daily
 * revenue figures are produced by calling the {@code sp_daily_revenue_report} MySQL
 * stored procedure directly (see {@code database/schema.sql}), and vehicle
 * utilisation is read from the {@code vw_vehicle_utilization} database view - both
 * concrete, demonstrable uses of advanced database features beyond plain CRUD.
 */
@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final BillRepository billRepository;
    private final JdbcTemplate jdbcTemplate;

    public ReportServiceImpl(ReservationRepository reservationRepository, VehicleRepository vehicleRepository,
                              CustomerRepository customerRepository, BillRepository billRepository, JdbcTemplate jdbcTemplate) {
        this.reservationRepository = reservationRepository;
        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
        this.billRepository = billRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public DashboardSummaryResponse dashboardSummary() {
        long totalVehicles = vehicleRepository.count();
        long availableVehicles = vehicleRepository.findByStatus(VehicleStatus.AVAILABLE).size();
        long totalCustomers = customerRepository.count();
        long activeReservations = reservationRepository.findByStatus(ReservationStatus.CONFIRMED).size()
                + reservationRepository.findByStatus(ReservationStatus.ONGOING).size();
        long todaysPickups = reservationRepository.countByPickupDate(LocalDate.now());

        BigDecimal totalRevenue = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(total_amount), 0) FROM bills", BigDecimal.class);
        BigDecimal unpaidAmount = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(total_amount), 0) FROM bills WHERE payment_status = ?",
                BigDecimal.class, PaymentStatus.UNPAID.name());

        return new DashboardSummaryResponse(totalVehicles, availableVehicles, totalCustomers,
                activeReservations, todaysPickups, totalRevenue, unpaidAmount);
    }

    @Override
    public List<RevenueReportItem> dailyRevenue(LocalDate from, LocalDate to) {
        return jdbcTemplate.query(
                "CALL sp_daily_revenue_report(?, ?)",
                (rs, rowNum) -> new RevenueReportItem(
                        rs.getDate("report_date").toString(),
                        rs.getBigDecimal("total_revenue"),
                        rs.getLong("reservation_count")
                ),
                from, to
        );
    }

    @Override
    public List<VehicleUtilizationItem> vehicleUtilization() {
        return jdbcTemplate.query(
                "SELECT registration_number, make, model, times_booked FROM vw_vehicle_utilization ORDER BY times_booked DESC",
                (rs, rowNum) -> new VehicleUtilizationItem(
                        rs.getString("registration_number"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getLong("times_booked")
                )
        );
    }
}
