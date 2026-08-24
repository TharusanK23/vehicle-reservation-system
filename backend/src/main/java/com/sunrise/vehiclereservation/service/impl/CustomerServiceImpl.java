package com.sunrise.vehiclereservation.service.impl;

import com.sunrise.vehiclereservation.dto.response.CustomerResponse;
import com.sunrise.vehiclereservation.exception.ResourceNotFoundException;
import com.sunrise.vehiclereservation.repository.CustomerRepository;
import com.sunrise.vehiclereservation.service.CustomerService;
import com.sunrise.vehiclereservation.util.DtoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream().map(DtoMapper::toResponse).toList();
    }

    @Override
    public CustomerResponse findById(Long id) {
        return customerRepository.findById(id)
                .map(DtoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    @Override
    public List<CustomerResponse> search(String name) {
        return customerRepository.findByFullNameContainingIgnoreCase(name).stream().map(DtoMapper::toResponse).toList();
    }
}
