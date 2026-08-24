package com.sunrise.vehiclereservation.pattern.strategy;

import com.sunrise.vehiclereservation.config.BusinessProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TDD note (see docs/ASSIGNMENT_REPORT.md, "Test-Driven Development" section): this
 * test class was written BEFORE {@link PricingContext} and its three concrete
 * strategies existed - the first run failed to compile (red), the strategy classes
 * were then implemented to the shape these tests demanded (green), and the
 * tax/rounding logic was subsequently pulled up into {@link AbstractPricingStrategy}
 * once all three strategies were passing (refactor), without changing this file.
 */
class PricingContextTest {

    private PricingContext pricingContext;

    @BeforeEach
    void setUp() {
        BusinessProperties props = new BusinessProperties();
        props.setTaxRate(0.08);
        props.setWeekendSurchargeRate(0.10);
        props.setLongTermDiscountDays(7);
        props.setLongTermDiscountRate(0.15);
        props.setCurrencySymbol("Rs.");
        pricingContext = new PricingContext(props);
    }

    @Nested
    @DisplayName("Standard weekday pricing")
    class StandardPricing {

        @Test
        @DisplayName("2-day rental on a Tuesday has no surcharge or discount, plus 8% tax")
        void calculatesStandardPriceForWeekday() {
            // Tuesday
            LocalDate pickup = LocalDate.of(2026, 1, 6);
            PricingResult result = pricingContext.price(BigDecimal.valueOf(5000), 2, pickup);

            assertThat(result.subtotal()).isEqualByComparingTo("10000.00");
            assertThat(result.surchargeAmount()).isEqualByComparingTo("0.00");
            assertThat(result.discountAmount()).isEqualByComparingTo("0.00");
            assertThat(result.taxAmount()).isEqualByComparingTo("800.00");
            assertThat(result.totalAmount()).isEqualByComparingTo("10800.00");
            assertThat(result.strategyName()).isEqualTo("STANDARD");
        }

        @Test
        @DisplayName("Boundary: a 1-day rental is priced as exactly one day, not zero")
        void oneDayRentalIsBilledAsOneDay() {
            LocalDate pickup = LocalDate.of(2026, 1, 6); // Tuesday
            PricingResult result = pricingContext.price(BigDecimal.valueOf(5000), 1, pickup);

            assertThat(result.subtotal()).isEqualByComparingTo("5000.00");
        }
    }

    @Nested
    @DisplayName("Weekend surcharge pricing")
    class WeekendPricing {

        @Test
        @DisplayName("Pickup on a Saturday applies a 10% weekend surcharge")
        void appliesWeekendSurchargeOnSaturdayPickup() {
            LocalDate saturday = LocalDate.of(2026, 1, 10);
            PricingResult result = pricingContext.price(BigDecimal.valueOf(5000), 2, saturday);

            assertThat(result.subtotal()).isEqualByComparingTo("10000.00");
            assertThat(result.surchargeAmount()).isEqualByComparingTo("1000.00");
            assertThat(result.discountAmount()).isEqualByComparingTo("0.00");
            assertThat(result.strategyName()).isEqualTo("WEEKEND_SURCHARGE");
        }

        @Test
        @DisplayName("Pickup on a Sunday also applies the weekend surcharge")
        void appliesWeekendSurchargeOnSundayPickup() {
            LocalDate sunday = LocalDate.of(2026, 1, 11);
            PricingResult result = pricingContext.price(BigDecimal.valueOf(5000), 1, sunday);

            assertThat(result.strategyName()).isEqualTo("WEEKEND_SURCHARGE");
        }
    }

    @Nested
    @DisplayName("Long-term discount pricing")
    class LongTermPricing {

        @Test
        @DisplayName("A 7+ day rental gets a 15% discount, taking priority over any weekend surcharge")
        void appliesLongTermDiscountAndOverridesWeekendSurcharge() {
            LocalDate saturday = LocalDate.of(2026, 1, 10); // would otherwise trigger a weekend surcharge
            PricingResult result = pricingContext.price(BigDecimal.valueOf(5000), 7, saturday);

            assertThat(result.subtotal()).isEqualByComparingTo("35000.00");
            assertThat(result.discountAmount()).isEqualByComparingTo("5250.00");
            assertThat(result.surchargeAmount()).isEqualByComparingTo("0.00");
            assertThat(result.strategyName()).isEqualTo("LONG_TERM_DISCOUNT");
        }

        @Test
        @DisplayName("Boundary: a 6-day rental does NOT yet qualify for the long-term discount")
        void sixDaysDoesNotQualifyForDiscount() {
            LocalDate tuesday = LocalDate.of(2026, 1, 6);
            PricingResult result = pricingContext.price(BigDecimal.valueOf(5000), 6, tuesday);

            assertThat(result.strategyName()).isNotEqualTo("LONG_TERM_DISCOUNT");
        }
    }
}
