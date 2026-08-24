package com.sunrise.vehiclereservation.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunrise.vehiclereservation.entity.*;
import com.sunrise.vehiclereservation.repository.UserRepository;
import com.sunrise.vehiclereservation.repository.VehicleCategoryRepository;
import com.sunrise.vehiclereservation.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration test exercising the real Spring context, real Spring
 * Security filter chain and an in-memory H2 database (see
 * src/test/resources/application-test.yml) - covers "API testing", "database
 * testing" and "integration testing" from the coursework's Testing task (Task C).
 * <p>
 * Flow under test mirrors the brief's core scenario end-to-end: staff logs in ->
 * registers a new reservation -> the system calculates and returns a bill.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReservationFlowIntegrationTest {

    private static final String COOKIE_NAME = "vrs_token";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private VehicleCategoryRepository categoryRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Vehicle vehicle;

    @BeforeEach
    void seedData() {
        userRepository.save(User.builder()
                .username("kirisha").password(passwordEncoder.encode("Kirisha@123"))
                .fullName("Test Staff").email("staff@test.local").role(Role.STAFF).enabled(true).build());

        VehicleCategory category = categoryRepository.save(VehicleCategory.builder()
                .categoryName("Economy").dailyRate(BigDecimal.valueOf(4500)).description("Test category").build());

        vehicle = vehicleRepository.save(Vehicle.builder()
                .registrationNumber("CAB-1234").make("Toyota").model("Aqua").manufactureYear(2019)
                .category(category).status(VehicleStatus.AVAILABLE).build());
    }

    @Test
    @DisplayName("End-to-end: login -> register reservation -> generate and fetch bill")
    void fullReservationAndBillingFlow() throws Exception {
        Cookie authCookie = login("kirisha", "Kirisha@123");

        String requestJson = objectMapper.writeValueAsString(Map.of(
                "customerFullName", "Kasun Fernando",
                "customerAddress", "Colombo 03",
                "customerContactNumber", "0771234567",
                "customerEmail", "kasun@example.com",
                "vehicleId", vehicle.getId(),
                "pickupDate", LocalDate.now().plusDays(1).toString(),
                "pickupTime", "09:00:00",
                "returnDate", LocalDate.now().plusDays(3).toString(),
                "returnTime", "09:00:00"
        ));

        MvcResult registerResult = mockMvc.perform(post("/api/reservations")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationNumber", org.hamcrest.Matchers.startsWith("RES-")))
                .andExpect(jsonPath("$.customer.fullName", is("Kasun Fernando")))
                .andReturn();

        String reservationNumber = objectMapper.readTree(registerResult.getResponse().getContentAsString())
                .get("reservationNumber").asText();

        mockMvc.perform(get("/api/reservations/" + reservationNumber).cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicle.registrationNumber", is("CAB-1234")));

        mockMvc.perform(get("/api/bills/reservation/" + reservationNumber).cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfDays", is(2)))
                .andExpect(jsonPath("$.totalAmount", org.hamcrest.Matchers.greaterThan(0.0)));
    }

    @Test
    @DisplayName("Negative: login with an incorrect password is rejected with 401")
    void loginWithWrongPasswordIsRejected() throws Exception {
        String requestJson = objectMapper.writeValueAsString(Map.of("username", "kirisha", "password", "wrong-password"));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Negative: a protected endpoint rejects a request with no authentication cookie")
    void protectedEndpointRejectsAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/reservations")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Negative: registering a reservation for a vehicle with an overlapping date range is rejected with 409")
    void doubleBookingIsRejectedByApi() throws Exception {
        Cookie authCookie = login("kirisha", "Kirisha@123");

        String requestJson = objectMapper.writeValueAsString(Map.of(
                "customerFullName", "Kasun Fernando",
                "customerAddress", "Colombo 03",
                "customerContactNumber", "0771234567",
                "vehicleId", vehicle.getId(),
                "pickupDate", LocalDate.now().plusDays(1).toString(),
                "pickupTime", "09:00:00",
                "returnDate", LocalDate.now().plusDays(3).toString(),
                "returnTime", "09:00:00"
        ));

        mockMvc.perform(post("/api/reservations").cookie(authCookie).contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isCreated());

        // Same vehicle, overlapping dates -> must be rejected
        mockMvc.perform(post("/api/reservations").cookie(authCookie).contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isConflict());
    }

    private Cookie login(String username, String password) throws Exception {
        String requestJson = objectMapper.writeValueAsString(Map.of("username", username, "password", password));
        MvcResult result = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isOk())
                .andReturn();
        Cookie cookie = result.getResponse().getCookie(COOKIE_NAME);
        assertThat(cookie).isNotNull();
        return cookie;
    }
}
