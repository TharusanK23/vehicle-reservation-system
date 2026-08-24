package com.sunrise.vehiclereservation.pattern.builder;

import com.sunrise.vehiclereservation.entity.Customer;
import com.sunrise.vehiclereservation.entity.Reservation;
import com.sunrise.vehiclereservation.entity.User;
import com.sunrise.vehiclereservation.entity.Vehicle;
import com.sunrise.vehiclereservation.entity.VehicleCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationBuilderTest {

    private Customer customer;
    private Vehicle vehicle;
    private User staff;

    @BeforeEach
    void setUp() {
        customer = Customer.builder().id(1L).fullName("Kasun Fernando").address("Colombo").contactNumber("0771234567").build();
        VehicleCategory category = VehicleCategory.builder().id(1L).categoryName("Economy").dailyRate(BigDecimal.valueOf(4500)).build();
        vehicle = Vehicle.builder().id(1L).registrationNumber("CAB-1234").make("Toyota").model("Aqua").manufactureYear(2019).category(category).build();
        staff = User.builder().id(1L).username("kirisha").build();
    }

    @Test
    @DisplayName("Builds a valid reservation with a generated, non-blank reservation number")
    void buildsValidReservation() {
        Reservation reservation = new ReservationBuilder()
                .withCustomer(customer)
                .withVehicle(vehicle)
                .withPickup(LocalDate.of(2026, 3, 1), LocalTime.of(9, 0))
                .withReturn(LocalDate.of(2026, 3, 3), LocalTime.of(9, 0))
                .withCreatedBy(staff)
                .build();

        assertThat(reservation.getReservationNumber()).isNotBlank().startsWith("RES-");
        assertThat(reservation.getCustomer()).isEqualTo(customer);
        assertThat(reservation.getVehicle()).isEqualTo(vehicle);
    }

    @Test
    @DisplayName("Negative: rejects a return date earlier than the pickup date")
    void rejectsReturnDateBeforePickupDate() {
        ReservationBuilder builder = new ReservationBuilder()
                .withCustomer(customer)
                .withVehicle(vehicle)
                .withPickup(LocalDate.of(2026, 3, 5), LocalTime.of(9, 0))
                .withReturn(LocalDate.of(2026, 3, 1), LocalTime.of(9, 0))
                .withCreatedBy(staff);

        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Return date cannot be before the pickup date");
    }

    @Test
    @DisplayName("Boundary: same-day rental requires the return time to be strictly after the pickup time")
    void rejectsSameDayReturnTimeNotAfterPickupTime() {
        ReservationBuilder builder = new ReservationBuilder()
                .withCustomer(customer)
                .withVehicle(vehicle)
                .withPickup(LocalDate.of(2026, 3, 5), LocalTime.of(14, 0))
                .withReturn(LocalDate.of(2026, 3, 5), LocalTime.of(10, 0))
                .withCreatedBy(staff);

        assertThatThrownBy(builder::build).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Negative: rejects when mandatory fields (e.g. vehicle) are missing")
    void rejectsMissingMandatoryFields() {
        ReservationBuilder builder = new ReservationBuilder()
                .withCustomer(customer)
                .withPickup(LocalDate.of(2026, 3, 1), LocalTime.of(9, 0))
                .withReturn(LocalDate.of(2026, 3, 3), LocalTime.of(9, 0))
                .withCreatedBy(staff);

        assertThatThrownBy(builder::build).isInstanceOf(IllegalStateException.class);
    }
}
