package com.sunrise.vehiclereservation.controller;

import com.sunrise.vehiclereservation.dto.request.CreateVehicleCategoryRequest;
import com.sunrise.vehiclereservation.dto.response.VehicleCategoryResponse;
import com.sunrise.vehiclereservation.service.VehicleCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-categories")
public class VehicleCategoryController {

    private final VehicleCategoryService categoryService;

    public VehicleCategoryController(VehicleCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<VehicleCategoryResponse> findAll() {
        return categoryService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleCategoryResponse create(@Valid @RequestBody CreateVehicleCategoryRequest request) {
        return categoryService.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}
