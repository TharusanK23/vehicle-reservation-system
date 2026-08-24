package com.sunrise.vehiclereservation.exception;

/** Raised when a request is well-formed but violates a business rule (e.g. double-booking a vehicle, return before pickup). */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
