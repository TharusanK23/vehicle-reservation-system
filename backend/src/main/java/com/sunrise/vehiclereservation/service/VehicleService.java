package com.sunrise.vehiclereservation.service;

import com.sunrise.vehiclereservation.dto.request.CreateVehicleRequest;
import com.sunrise.vehiclereservation.dto.response.VehicleResponse;

import java.time.LocalDate;
import java.util.List;

public interface VehicleService {
    List<VehicleResponse> findAll();
    VehicleResponse findById(Long id);
    VehicleResponse create(CreateVehicleRequest request);
    void delete(Long id);
    List<VehicleResponse> findAvailable(LocalDate pickupDate, LocalDate returnDate, Long categoryId);
}
