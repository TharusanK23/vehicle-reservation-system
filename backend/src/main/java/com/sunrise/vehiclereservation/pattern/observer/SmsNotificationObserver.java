package com.sunrise.vehiclereservation.pattern.observer;

import com.sunrise.vehiclereservation.entity.NotificationLog;
import com.sunrise.vehiclereservation.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Simulated SMS notification channel - see {@link EmailNotificationObserver} for the simulation rationale. */
@Component
public class SmsNotificationObserver implements ReservationObserver {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationObserver.class);
    private final NotificationLogRepository notificationLogRepository;

    public SmsNotificationObserver(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    @Override
    public void onReservationEvent(ReservationEvent event) {
        String message = "SMS to %s: Booking %s is %s.".formatted(
                event.reservation().getCustomer().getContactNumber(),
                event.reservation().getReservationNumber(),
                event.type().name().toLowerCase()
        );
        log.info("[SMS] {}", message);
        notificationLogRepository.save(NotificationLog.builder()
                .reservationNumber(event.reservation().getReservationNumber())
                .channel("SMS")
                .message(message)
                .build());
    }
}
