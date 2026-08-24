package com.sunrise.vehiclereservation.controller;

import com.sunrise.vehiclereservation.dto.request.CreateUserRequest;
import com.sunrise.vehiclereservation.dto.request.UpdateUserRequest;
import com.sunrise.vehiclereservation.dto.response.UserResponse;
import com.sunrise.vehiclereservation.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Staff-account administration - restricted to ADMIN (see SecurityConfig and method-level checks below). */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    @PatchMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return userService.update(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    public void deactivate(@PathVariable Long id) {
        userService.deactivate(id);
    }
}
