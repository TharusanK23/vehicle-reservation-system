package com.sunrise.vehiclereservation.service;

import com.sunrise.vehiclereservation.dto.response.CustomerResponse;

import java.util.List;

public interface CustomerService {
    List<CustomerResponse> findAll();
    CustomerResponse findById(Long id);
    List<CustomerResponse> search(String name);
}
