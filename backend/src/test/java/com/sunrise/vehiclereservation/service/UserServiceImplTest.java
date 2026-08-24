package com.sunrise.vehiclereservation.service;

import com.sunrise.vehiclereservation.dto.request.UpdateUserRequest;
import com.sunrise.vehiclereservation.dto.response.UserResponse;
import com.sunrise.vehiclereservation.entity.Role;
import com.sunrise.vehiclereservation.entity.User;
import com.sunrise.vehiclereservation.exception.ResourceNotFoundException;
import com.sunrise.vehiclereservation.repository.UserRepository;
import com.sunrise.vehiclereservation.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the Admin "edit staff details" feature - deliberately scoped
 * to full name and email only (see {@link UpdateUserRequest}); username,
 * password and role are untouched by this endpoint.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;
    private User existingUser;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder);
        existingUser = User.builder()
                .id(2L).username("kirisha").password("hashed-password")
                .fullName("Kirisha Fernando").email("kirisha@sunrisevehicles.lk")
                .role(Role.STAFF).enabled(true).build();
    }

    @Test
    @DisplayName("Positive: Admin updates a staff member's full name and email")
    void updatesFullNameAndEmail() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.update(2L, new UpdateUserRequest("Kirisha K. Perera", "kirisha.perera@sunrisevehicles.lk"));

        assertThat(response.fullName()).isEqualTo("Kirisha K. Perera");
        assertThat(response.email()).isEqualTo("kirisha.perera@sunrisevehicles.lk");
        // username, role and password are untouched by this endpoint
        assertThat(existingUser.getUsername()).isEqualTo("kirisha");
        assertThat(existingUser.getRole()).isEqualTo(Role.STAFF);
        assertThat(existingUser.getPassword()).isEqualTo("hashed-password");
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("Negative: updating an unknown staff id raises ResourceNotFoundException")
    void updateUnknownUserThrows() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(99L, new UpdateUserRequest("Someone", "someone@example.com")))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
