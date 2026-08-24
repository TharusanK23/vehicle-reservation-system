package com.sunrise.vehiclereservation.service;

import com.sunrise.vehiclereservation.dto.request.CreateUserRequest;
import com.sunrise.vehiclereservation.dto.request.UpdateUserRequest;
import com.sunrise.vehiclereservation.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> findAll();
    UserResponse create(CreateUserRequest request);
    UserResponse update(Long id, UpdateUserRequest request);
    void deactivate(Long id);
}
