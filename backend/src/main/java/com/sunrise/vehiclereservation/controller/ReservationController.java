package com.sunrise.vehiclereservation.controller;

import com.sunrise.vehiclereservation.dto.request.RegisterReservationRequest;
import com.sunrise.vehiclereservation.dto.response.ReservationResponse;
import com.sunrise.vehiclereservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * "Register New Appointment" and "Display Appointment Details" from the brief,
 * adapted to vehicle reservations. Search-by-number is exposed at
 * GET /api/reservations/{reservationNumber}.
 */
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<ReservationResponse> findAll() {
        return reservationService.findAll();
    }

    @GetMapping("/{reservationNumber}")
    public ReservationResponse findByNumber(@PathVariable String reservationNumber) {
        return reservationService.findByReservationNumber(reservationNumber);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse register(@Valid @RequestBody RegisterReservationRequest request, Authentication authentication) {
        return reservationService.register(request, authentication.getName());
    }

    @PostMapping("/{reservationNumber}/cancel")
    public ReservationResponse cancel(@PathVariable String reservationNumber) {
        return reservationService.cancel(reservationNumber);
    }

    @PatchMapping("/{reservationNumber}/status")
    public ReservationResponse updateStatus(@PathVariable String reservationNumber, @RequestBody Map<String, String> body) {
        return reservationService.updateStatus(reservationNumber, body.get("status"));
    }
}
