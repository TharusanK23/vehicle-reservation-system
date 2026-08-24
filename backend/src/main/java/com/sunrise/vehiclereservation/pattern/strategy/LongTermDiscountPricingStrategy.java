package com.sunrise.vehiclereservation.pattern.strategy;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Applied when the rental spans 7+ days: rewards long-term hires with a percentage discount on the subtotal. */
public class LongTermDiscountPricingStrategy extends AbstractPricingStrategy {

    private final double discountRate;

    public LongTermDiscountPricingStrategy(double taxRate, double discountRate) {
        super(taxRate);
        this.discountRate = discountRate;
    }

    @Override
    public PricingResult calculate(BigDecimal dailyRate, int days, LocalDate pickupDate) {
        BigDecimal subtotal = dailyRate.multiply(BigDecimal.valueOf(days));
        BigDecimal discount = subtotal.multiply(BigDecimal.valueOf(discountRate));
        return buildResult(subtotal, BigDecimal.ZERO, discount);
    }

    @Override
    public String getName() {
        return "LONG_TERM_DISCOUNT";
    }
}
