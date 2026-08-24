package com.sunrise.vehiclereservation.controller;

import com.sunrise.vehiclereservation.dto.response.CustomerResponse;
import com.sunrise.vehiclereservation.service.CustomerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<CustomerResponse> findAll(@RequestParam(required = false) String search) {
        return (search == null || search.isBlank()) ? customerService.findAll() : customerService.search(search);
    }

    @GetMapping("/{id}")
    public CustomerResponse findById(@PathVariable Long id) {
        return customerService.findById(id);
    }
}
