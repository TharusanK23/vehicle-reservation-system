package com.sunrise.vehiclereservation.pattern.singleton;

import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Classic (GoF) Singleton: exactly one instance for the whole JVM, reached via
 * {@link #getInstance()} rather than dependency injection. It hands out unique,
 * human-readable reservation numbers in the form {@code RES-<year>-<6 digit seq>}
 * to guarantee no two reservations are ever created with a colliding number, even
 * under concurrent requests (backed by an {@link AtomicLong}).
 * <p>
 * Deliberately implemented as a plain-Java singleton (private constructor, eager
 * static instance, thread-safe counter) instead of relying only on Spring's
 * bean-scope "singleton" so the pattern is demonstrable independently of the
 * framework, per the coursework's Task B design-pattern requirement.
 */
public final class ReservationNumberGenerator {

    private static final ReservationNumberGenerator INSTANCE = new ReservationNumberGenerator();

    private final AtomicLong sequence = new AtomicLong(0);
    private volatile boolean initialised = false;

    private ReservationNumberGenerator() {
    }

    public static ReservationNumberGenerator getInstance() {
        return INSTANCE;
    }

    /** Seeds the counter from the current database state at application startup - see {@code AppStartupInitializer}. */
    public synchronized void initialise(long currentMaxSequence) {
        if (!initialised) {
            sequence.set(currentMaxSequence);
            initialised = true;
        }
    }

    public String nextReservationNumber() {
        long next = sequence.incrementAndGet();
        return "RES-%d-%06d".formatted(Year.now().getValue(), next);
    }

    /** Derives a matching bill number from an existing reservation number, e.g. RES-2026-000123 -> INV-2026-000123. */
    public String billNumberFor(String reservationNumber) {
        return "INV-" + reservationNumber.substring(4);
    }
}
