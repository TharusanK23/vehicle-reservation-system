package com.sunrise.vehiclereservation.pattern.factory;

import com.sunrise.vehiclereservation.entity.Bill;
import com.sunrise.vehiclereservation.entity.PaymentStatus;
import com.sunrise.vehiclereservation.entity.Reservation;
import com.sunrise.vehiclereservation.pattern.singleton.ReservationNumberGenerator;
import com.sunrise.vehiclereservation.pattern.strategy.PricingContext;
import com.sunrise.vehiclereservation.pattern.strategy.PricingResult;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

/**
 * Factory Method pattern: hides the multi-step process of turning a {@link Reservation}
 * into a fully-priced {@link Bill} (duration calculation -> strategy selection via
 * {@link PricingContext} -> bill-number generation via the {@link ReservationNumberGenerator}
 * singleton -> entity assembly) behind a single {@link #create(Reservation)} call, so
 * {@code BillService} never needs to know how pricing or numbering work internally.
 */
@Component
public class BillFactory implements DocumentFactory<Bill, Reservation> {

    private final PricingContext pricingContext;

    public BillFactory(PricingContext pricingContext) {
        this.pricingContext = pricingContext;
    }

    @Override
    public Bill create(Reservation reservation) {
        int days = (int) Math.max(1, ChronoUnit.DAYS.between(reservation.getPickupDate(), reservation.getReturnDate()));
        var category = reservation.getVehicle().getCategory();
        PricingResult pricing = pricingContext.price(category.getDailyRate(), days, reservation.getPickupDate());

        return Bill.builder()
                .billNumber(ReservationNumberGenerator.getInstance().billNumberFor(reservation.getReservationNumber()))
                .reservation(reservation)
                .numberOfDays(days)
                .dailyRate(category.getDailyRate())
                .subtotal(pricing.subtotal())
                .surchargeAmount(pricing.surchargeAmount())
                .discountAmount(pricing.discountAmount())
                .taxAmount(pricing.taxAmount())
                .totalAmount(pricing.totalAmount())
                .pricingStrategy(pricing.strategyName())
                .paymentStatus(PaymentStatus.UNPAID)
                .build();
    }
}
