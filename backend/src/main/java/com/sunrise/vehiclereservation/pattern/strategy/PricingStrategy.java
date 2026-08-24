package com.sunrise.vehiclereservation.pattern.strategy;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Strategy pattern: encapsulates one algorithm for turning a daily rate and a rental
 * duration into a priced result. Concrete strategies are interchangeable at runtime -
 * selected by {@link PricingContext} - without the caller (BillService) knowing which
 * one is in effect. This keeps new pricing rules (seasonal rates, loyalty discounts...)
 * additive rather than requiring edits to existing billing code (Open/Closed Principle).
 */
public interface PricingStrategy {

    /**
     * @param dailyRate  the {@link com.sunrise.vehiclereservation.entity.VehicleCategory} daily rate
     * @param days       number of rental days (inclusive)
     * @param pickupDate date the vehicle is collected (used by date-sensitive strategies)
     * @return the computed pricing breakdown
     */
    PricingResult calculate(BigDecimal dailyRate, int days, LocalDate pickupDate);

    /** Human-readable name recorded on the bill for auditability. */
    String getName();
}
