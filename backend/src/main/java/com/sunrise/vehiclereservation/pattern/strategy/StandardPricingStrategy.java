package com.sunrise.vehiclereservation.pattern.strategy;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Default strategy: subtotal = dailyRate x days, no surcharge, no discount, plus standard tax. */
public class StandardPricingStrategy extends AbstractPricingStrategy {

    public StandardPricingStrategy(double taxRate) {
        super(taxRate);
    }

    @Override
    public PricingResult calculate(BigDecimal dailyRate, int days, LocalDate pickupDate) {
        BigDecimal subtotal = dailyRate.multiply(BigDecimal.valueOf(days));
        return buildResult(subtotal, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Override
    public String getName() {
        return "STANDARD";
    }
}
