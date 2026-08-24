package com.sunrise.vehiclereservation.service.impl;

import com.sunrise.vehiclereservation.dto.request.CreateVehicleCategoryRequest;
import com.sunrise.vehiclereservation.dto.response.VehicleCategoryResponse;
import com.sunrise.vehiclereservation.entity.VehicleCategory;
import com.sunrise.vehiclereservation.exception.DuplicateResourceException;
import com.sunrise.vehiclereservation.exception.ResourceNotFoundException;
import com.sunrise.vehiclereservation.repository.VehicleCategoryRepository;
import com.sunrise.vehiclereservation.service.VehicleCategoryService;
import com.sunrise.vehiclereservation.util.DtoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class VehicleCategoryServiceImpl implements VehicleCategoryService {

    private final VehicleCategoryRepository categoryRepository;

    public VehicleCategoryServiceImpl(VehicleCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleCategoryResponse> findAll() {
        return categoryRepository.findAll().stream().map(DtoMapper::toResponse).toList();
    }

    @Override
    public VehicleCategoryResponse create(CreateVehicleCategoryRequest request) {
        boolean exists = categoryRepository.findAll().stream()
                .anyMatch(c -> c.getCategoryName().equalsIgnoreCase(request.categoryName()));
        if (exists) {
            throw new DuplicateResourceException("A vehicle category named '" + request.categoryName() + "' already exists.");
        }
        VehicleCategory category = VehicleCategory.builder()
                .categoryName(request.categoryName())
                .dailyRate(request.dailyRate())
                .description(request.description())
                .build();
        return DtoMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicle category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
