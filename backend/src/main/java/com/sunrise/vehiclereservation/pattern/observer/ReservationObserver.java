package com.sunrise.vehiclereservation.pattern.observer;

/** Observer pattern: implementations react to a {@link ReservationEvent} without the Subject knowing how many, or which, observers exist. */
public interface ReservationObserver {
    void onReservationEvent(ReservationEvent event);
}
