package com.sunrise.vehiclereservation.controller;

import com.sunrise.vehiclereservation.dto.request.CreateVehicleRequest;
import com.sunrise.vehiclereservation.dto.response.VehicleResponse;
import com.sunrise.vehiclereservation.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public List<VehicleResponse> findAll() {
        return vehicleService.findAll();
    }

    @GetMapping("/{id}")
    public VehicleResponse findById(@PathVariable Long id) {
        return vehicleService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse create(@Valid @RequestBody CreateVehicleRequest request) {
        return vehicleService.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        vehicleService.delete(id);
    }

    @GetMapping("/available")
    public List<VehicleResponse> findAvailable(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pickupDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
            @RequestParam(required = false) Long categoryId) {
        return vehicleService.findAvailable(pickupDate, returnDate, categoryId);
    }
}
