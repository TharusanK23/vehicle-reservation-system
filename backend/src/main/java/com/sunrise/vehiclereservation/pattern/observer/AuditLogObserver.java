package com.sunrise.vehiclereservation.pattern.observer;

import com.sunrise.vehiclereservation.entity.NotificationLog;
import com.sunrise.vehiclereservation.repository.NotificationLogRepository;
import org.springframework.stereotype.Component;

/** Internal audit-trail channel: every reservation lifecycle event is recorded for traceability, independent of customer-facing alerts. */
@Component
public class AuditLogObserver implements ReservationObserver {

    private final NotificationLogRepository notificationLogRepository;

    public AuditLogObserver(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    @Override
    public void onReservationEvent(ReservationEvent event) {
        String message = "AUDIT: reservation %s changed to %s by staff at %s".formatted(
                event.reservation().getReservationNumber(),
                event.type().name(),
                event.reservation().getCreatedBy().getUsername()
        );
        notificationLogRepository.save(NotificationLog.builder()
                .reservationNumber(event.reservation().getReservationNumber())
                .channel("AUDIT")
                .message(message)
                .build());
    }
}
