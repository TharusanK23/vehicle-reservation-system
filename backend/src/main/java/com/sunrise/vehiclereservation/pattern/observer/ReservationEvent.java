package com.sunrise.vehiclereservation.pattern.observer;

import com.sunrise.vehiclereservation.entity.Reservation;

/** The payload passed from the Subject to every Observer when a reservation's lifecycle changes. */
public record ReservationEvent(Reservation reservation, ReservationEventType type) {

    public enum ReservationEventType {
        CREATED,
        CONFIRMED,
        CANCELLED,
        BILL_GENERATED
    }
}
