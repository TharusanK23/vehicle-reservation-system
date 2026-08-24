package com.sunrise.vehiclereservation.pattern.strategy;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Applied when the pickup date falls on Friday, Saturday or Sunday: adds a weekend surcharge on top of the subtotal. */
public class WeekendPricingStrategy extends AbstractPricingStrategy {

    private final double surchargeRate;

    public WeekendPricingStrategy(double taxRate, double surchargeRate) {
        super(taxRate);
        this.surchargeRate = surchargeRate;
    }

    @Override
    public PricingResult calculate(BigDecimal dailyRate, int days, LocalDate pickupDate) {
        BigDecimal subtotal = dailyRate.multiply(BigDecimal.valueOf(days));
        BigDecimal surcharge = subtotal.multiply(BigDecimal.valueOf(surchargeRate));
        return buildResult(subtotal, surcharge, BigDecimal.ZERO);
    }

    @Override
    public String getName() {
        return "WEEKEND_SURCHARGE";
    }
}
