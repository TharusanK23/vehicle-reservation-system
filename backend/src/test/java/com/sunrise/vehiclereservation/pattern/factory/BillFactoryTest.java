package com.sunrise.vehiclereservation.pattern.factory;

import com.sunrise.vehiclereservation.entity.*;
import com.sunrise.vehiclereservation.pattern.strategy.PricingContext;
import com.sunrise.vehiclereservation.pattern.strategy.PricingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillFactoryTest {

    @Mock
    private PricingContext pricingContext;

    private BillFactory billFactory;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        billFactory = new BillFactory(pricingContext);

        VehicleCategory category = VehicleCategory.builder().id(1L).categoryName("SUV").dailyRate(BigDecimal.valueOf(9500)).build();
        Vehicle vehicle = Vehicle.builder().id(1L).registrationNumber("CBA-1122").make("Toyota").model("Prado").category(category).build();
        Customer customer = Customer.builder().id(1L).fullName("Ruwan Silva").build();
        User staff = User.builder().id(1L).username("staff1").build();

        reservation = Reservation.builder()
                .reservationNumber("RES-2026-000001")
                .customer(customer)
                .vehicle(vehicle)
                .pickupDate(LocalDate.of(2026, 3, 1))
                .pickupTime(LocalTime.of(9, 0))
                .returnDate(LocalDate.of(2026, 3, 4))
                .returnTime(LocalTime.of(9, 0))
                .status(ReservationStatus.CONFIRMED)
                .createdBy(staff)
                .build();
    }

    @Test
    @DisplayName("Derives the bill number from the reservation number and delegates pricing to PricingContext for a 3-day rental")
    void createsBillFromReservation() {
        PricingResult mockResult = new PricingResult(
                BigDecimal.valueOf(28500), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(2280), BigDecimal.valueOf(30780), "STANDARD");
        when(pricingContext.price(any(), anyInt(), any())).thenReturn(mockResult);

        Bill bill = billFactory.create(reservation);

        assertThat(bill.getBillNumber()).isEqualTo("INV-2026-000001");
        assertThat(bill.getNumberOfDays()).isEqualTo(3);
        assertThat(bill.getTotalAmount()).isEqualByComparingTo("30780");
        assertThat(bill.getPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);
    }

    @Test
    @DisplayName("Boundary: a same-day pickup/return is still billed as a minimum of 1 day")
    void sameDayRentalIsBilledAsOneDayMinimum() {
        reservation.setReturnDate(reservation.getPickupDate());
        PricingResult mockResult = new PricingResult(
                BigDecimal.valueOf(9500), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(760), BigDecimal.valueOf(10260), "STANDARD");
        when(pricingContext.price(any(), anyInt(), any())).thenReturn(mockResult);

        Bill bill = billFactory.create(reservation);

        assertThat(bill.getNumberOfDays()).isEqualTo(1);
    }
}
