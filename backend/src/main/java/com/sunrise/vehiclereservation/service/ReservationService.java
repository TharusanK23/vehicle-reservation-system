package com.sunrise.vehiclereservation.service;

import com.sunrise.vehiclereservation.dto.request.RegisterReservationRequest;
import com.sunrise.vehiclereservation.dto.response.ReservationResponse;

import java.util.List;

public interface ReservationService {
    ReservationResponse register(RegisterReservationRequest request, String createdByUsername);
    ReservationResponse findByReservationNumber(String reservationNumber);
    List<ReservationResponse> findAll();
    ReservationResponse cancel(String reservationNumber);
    ReservationResponse updateStatus(String reservationNumber, String status);
}
