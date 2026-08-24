package com.sunrise.vehiclereservation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalised business rules (tax rate, surcharges, discount thresholds) bound
 * from {@code application.yml} (prefix {@code app.business}) so pricing policy can
 * change without recompiling the pricing {@link com.sunrise.vehiclereservation.pattern.strategy.PricingStrategy}
 * implementations.
 */
@ConfigurationProperties(prefix = "app.business")
public class BusinessProperties {

    private double taxRate;
    private double weekendSurchargeRate;
    private int longTermDiscountDays;
    private double longTermDiscountRate;
    private String currencySymbol;

    public double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
    }

    public double getWeekendSurchargeRate() {
        return weekendSurchargeRate;
    }

    public void setWeekendSurchargeRate(double weekendSurchargeRate) {
        this.weekendSurchargeRate = weekendSurchargeRate;
    }

    public int getLongTermDiscountDays() {
        return longTermDiscountDays;
    }

    public void setLongTermDiscountDays(int longTermDiscountDays) {
        this.longTermDiscountDays = longTermDiscountDays;
    }

    public double getLongTermDiscountRate() {
        return longTermDiscountRate;
    }

    public void setLongTermDiscountRate(double longTermDiscountRate) {
        this.longTermDiscountRate = longTermDiscountRate;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }
}
