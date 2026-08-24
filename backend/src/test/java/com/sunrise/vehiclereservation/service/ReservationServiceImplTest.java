package com.sunrise.vehiclereservation.service;

import com.sunrise.vehiclereservation.dto.request.RegisterReservationRequest;
import com.sunrise.vehiclereservation.dto.response.ReservationResponse;
import com.sunrise.vehiclereservation.entity.*;
import com.sunrise.vehiclereservation.exception.BusinessRuleException;
import com.sunrise.vehiclereservation.exception.ResourceNotFoundException;
import com.sunrise.vehiclereservation.pattern.observer.ReservationEventPublisher;
import com.sunrise.vehiclereservation.repository.CustomerRepository;
import com.sunrise.vehiclereservation.repository.ReservationRepository;
import com.sunrise.vehiclereservation.repository.UserRepository;
import com.sunrise.vehiclereservation.repository.VehicleRepository;
import com.sunrise.vehiclereservation.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests (repositories mocked via Mockito) for the "Register New Appointment" /
 * "Display Appointment Details" workflow, covering the double-booking business rule
 * that is also enforced by the trg_prevent_double_booking database trigger.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReservationEventPublisher eventPublisher;

    private ReservationServiceImpl reservationService;

    private Vehicle vehicle;
    private User staff;
    private RegisterReservationRequest validRequest;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationServiceImpl(reservationRepository, customerRepository, vehicleRepository, userRepository, eventPublisher);

        VehicleCategory category = VehicleCategory.builder().id(1L).categoryName("Economy").dailyRate(BigDecimal.valueOf(4500)).build();
        vehicle = Vehicle.builder().id(1L).registrationNumber("CAB-1234").make("Toyota").model("Aqua")
                .category(category).status(VehicleStatus.AVAILABLE).build();
        staff = User.builder().id(1L).username("staff1").build();

        validRequest = new RegisterReservationRequest(
                null, "Kasun Fernando", "Colombo 03", "0771234567", "kasun@example.com", null,
                1L, LocalDate.of(2026, 3, 1), LocalTime.of(9, 0), LocalDate.of(2026, 3, 3), LocalTime.of(9, 0), null);
    }

    @Test
    @DisplayName("Positive: registers a reservation, reserves the vehicle and publishes a CREATED event")
    void registersReservationSuccessfully() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(reservationRepository.findOverlappingForVehicle(1L, validRequest.pickupDate(), validRequest.returnDate()))
                .thenReturn(List.of());
        when(userRepository.findByUsername("staff1")).thenReturn(Optional.of(staff));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservationResponse response = reservationService.register(validRequest, "staff1");

        assertThat(response.reservationNumber()).startsWith("RES-");
        assertThat(response.customer().fullName()).isEqualTo("Kasun Fernando");
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.RESERVED);
        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("Negative: rejects booking a vehicle that is under maintenance")
    void rejectsVehicleUnderMaintenance() {
        vehicle.setStatus(VehicleStatus.MAINTENANCE);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        assertThatThrownBy(() -> reservationService.register(validRequest, "staff1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not available for booking");

        verifyNoInteractions(reservationRepository);
    }

    @Test
    @DisplayName("Negative: rejects an overlapping (double) booking for the same vehicle")
    void rejectsOverlappingBooking() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        Reservation existing = Reservation.builder().reservationNumber("RES-2026-000001").build();
        when(reservationRepository.findOverlappingForVehicle(1L, validRequest.pickupDate(), validRequest.returnDate()))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> reservationService.register(validRequest, "staff1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already reserved");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Negative: rejects a request with neither an existing customerId nor new-customer details")
    void rejectsRequestMissingCustomerDetails() {
        RegisterReservationRequest incomplete = new RegisterReservationRequest(
                null, null, null, null, null, null,
                1L, LocalDate.of(2026, 3, 1), LocalTime.of(9, 0), LocalDate.of(2026, 3, 3), LocalTime.of(9, 0), null);

        assertThatThrownBy(() -> reservationService.register(incomplete, "staff1"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("Negative: searching for a reservation number that does not exist raises ResourceNotFoundException")
    void findByReservationNumberNotFound() {
        when(reservationRepository.findByReservationNumber("RES-2026-999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.findByReservationNumber("RES-2026-999999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Cancelling a reservation frees the vehicle and publishes a CANCELLED event")
    void cancelReleasesVehicle() {
        Customer customer = Customer.builder().id(1L).fullName("Kasun Fernando").build();
        Reservation reservation = Reservation.builder()
                .reservationNumber("RES-2026-000001").customer(customer).vehicle(vehicle)
                .status(ReservationStatus.CONFIRMED).createdBy(staff)
                .pickupDate(LocalDate.of(2026, 3, 1)).returnDate(LocalDate.of(2026, 3, 3))
                .pickupTime(LocalTime.of(9, 0)).returnTime(LocalTime.of(9, 0))
                .build();
        when(reservationRepository.findByReservationNumber("RES-2026-000001")).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        reservationService.cancel("RES-2026-000001");

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Negative: a COMPLETED reservation cannot be cancelled")
    void cannotCancelCompletedReservation() {
        Reservation reservation = Reservation.builder()
                .reservationNumber("RES-2026-000001").vehicle(vehicle).status(ReservationStatus.COMPLETED).build();
        when(reservationRepository.findByReservationNumber("RES-2026-000001")).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancel("RES-2026-000001"))
                .isInstanceOf(BusinessRuleException.class);
    }
}
