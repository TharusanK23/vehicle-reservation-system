package com.sunrise.vehiclereservation.service.impl;

import com.sunrise.vehiclereservation.dto.request.CreateVehicleRequest;
import com.sunrise.vehiclereservation.dto.response.VehicleResponse;
import com.sunrise.vehiclereservation.entity.Vehicle;
import com.sunrise.vehiclereservation.entity.VehicleCategory;
import com.sunrise.vehiclereservation.exception.DuplicateResourceException;
import com.sunrise.vehiclereservation.exception.ResourceNotFoundException;
import com.sunrise.vehiclereservation.repository.VehicleCategoryRepository;
import com.sunrise.vehiclereservation.repository.VehicleRepository;
import com.sunrise.vehiclereservation.service.VehicleService;
import com.sunrise.vehiclereservation.util.DtoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleCategoryRepository categoryRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository, VehicleCategoryRepository categoryRepository) {
        this.vehicleRepository = vehicleRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> findAll() {
        return vehicleRepository.findAll().stream().map(DtoMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse findById(Long id) {
        return vehicleRepository.findById(id)
                .map(DtoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
    }

    @Override
    public VehicleResponse create(CreateVehicleRequest request) {
        if (vehicleRepository.existsByRegistrationNumber(request.registrationNumber())) {
            throw new DuplicateResourceException("A vehicle with registration number '" + request.registrationNumber() + "' already exists.");
        }
        VehicleCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle category not found with id: " + request.categoryId()));

        Vehicle vehicle = Vehicle.builder()
                .registrationNumber(request.registrationNumber().toUpperCase())
                .make(request.make())
                .model(request.model())
                .manufactureYear(request.manufactureYear())
                .category(category)
                .build();
        return DtoMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Override
    public void delete(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicle not found with id: " + id);
        }
        vehicleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> findAvailable(LocalDate pickupDate, LocalDate returnDate, Long categoryId) {
        return vehicleRepository.findAvailableVehicles(pickupDate, returnDate, categoryId)
                .stream().map(DtoMapper::toResponse).toList();
    }
}
