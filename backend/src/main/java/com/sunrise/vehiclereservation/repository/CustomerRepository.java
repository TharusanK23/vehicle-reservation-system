package com.sunrise.vehiclereservation.repository;

import com.sunrise.vehiclereservation.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByContactNumber(String contactNumber);
    List<Customer> findByFullNameContainingIgnoreCase(String name);
}
