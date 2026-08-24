package com.sunrise.vehiclereservation.service;

import com.sunrise.vehiclereservation.dto.request.CreateVehicleCategoryRequest;
import com.sunrise.vehiclereservation.dto.response.VehicleCategoryResponse;

import java.util.List;

public interface VehicleCategoryService {
    List<VehicleCategoryResponse> findAll();
    VehicleCategoryResponse create(CreateVehicleCategoryRequest request);
    void delete(Long id);
}
