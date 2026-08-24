package com.sunrise.vehiclereservation.pattern.observer;

import com.sunrise.vehiclereservation.entity.NotificationLog;
import com.sunrise.vehiclereservation.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simulated e-mail notification channel. No real SMTP gateway is configured for this
 * coursework build (see docs/SETUP.md, "Notification simulation"); the message that
 * would be sent to the customer is logged and persisted to {@code notification_logs}
 * as verifiable evidence that the alert fired.
 */
@Component
public class EmailNotificationObserver implements ReservationObserver {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationObserver.class);
    private final NotificationLogRepository notificationLogRepository;

    public EmailNotificationObserver(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    @Override
    public void onReservationEvent(ReservationEvent event) {
        String message = "Dear %s, your reservation %s has been %s. Vehicle: %s %s (%s).".formatted(
                event.reservation().getCustomer().getFullName(),
                event.reservation().getReservationNumber(),
                event.type().name().toLowerCase(),
                event.reservation().getVehicle().getMake(),
                event.reservation().getVehicle().getModel(),
                event.reservation().getVehicle().getRegistrationNumber()
        );
        log.info("[EMAIL] To: {} | {}", event.reservation().getCustomer().getEmail(), message);
        notificationLogRepository.save(NotificationLog.builder()
                .reservationNumber(event.reservation().getReservationNumber())
                .channel("EMAIL")
                .message(message)
                .build());
    }
}
