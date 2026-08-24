package com.sunrise.vehiclereservation.pattern.observer;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Observer pattern "subject". Spring collects every bean implementing
 * {@link ReservationObserver} into the constructor-injected list automatically, so
 * adding a new notification channel (push notifications, a webhook...) is a matter of
 * writing one new {@code @Component} class - {@code ReservationService} never changes.
 */
@Component
public class ReservationEventPublisher {

    private final List<ReservationObserver> observers;

    public ReservationEventPublisher(List<ReservationObserver> observers) {
        this.observers = observers;
    }

    public void publish(ReservationEvent event) {
        for (ReservationObserver observer : observers) {
            observer.onReservationEvent(event);
        }
    }
}
