package com.sunrise.vehiclereservation.repository;

import com.sunrise.vehiclereservation.entity.VehicleCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleCategoryRepository extends JpaRepository<VehicleCategory, Long> {
}
