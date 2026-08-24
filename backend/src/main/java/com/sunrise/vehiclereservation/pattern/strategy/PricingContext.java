package com.sunrise.vehiclereservation.pattern.strategy;

import com.sunrise.vehiclereservation.config.BusinessProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Strategy pattern "context": chooses the correct {@link PricingStrategy} for a given
 * reservation and delegates the calculation to it. Precedence rule (documented in the
 * assignment report): a long-term rental discount takes priority over a weekend
 * surcharge, which in turn takes priority over standard pricing.
 */
@Component
public class PricingContext {

    private final BusinessProperties props;

    public PricingContext(BusinessProperties props) {
        this.props = props;
    }

    public PricingResult price(BigDecimal dailyRate, int days, LocalDate pickupDate) {
        PricingStrategy strategy = resolveStrategy(days, pickupDate);
        return strategy.calculate(dailyRate, days, pickupDate);
    }

    private PricingStrategy resolveStrategy(int days, LocalDate pickupDate) {
        if (days >= props.getLongTermDiscountDays()) {
            return new LongTermDiscountPricingStrategy(props.getTaxRate(), props.getLongTermDiscountRate());
        }
        DayOfWeek day = pickupDate.getDayOfWeek();
        if (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return new WeekendPricingStrategy(props.getTaxRate(), props.getWeekendSurchargeRate());
        }
        return new StandardPricingStrategy(props.getTaxRate());
    }
}
