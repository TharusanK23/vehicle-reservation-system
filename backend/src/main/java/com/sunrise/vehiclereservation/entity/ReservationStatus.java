package com.sunrise.vehiclereservation.entity;

/** Lifecycle state of a reservation, from creation through to completion or cancellation. */
public enum ReservationStatus {
    PENDING,
    CONFIRMED,
    ONGOING,
    COMPLETED,
    CANCELLED
}
