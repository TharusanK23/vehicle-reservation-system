package com.sunrise.vehiclereservation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Persisted record of a notification raised by the Observer-pattern event publisher
 * (e.g. "email" / "SMS" alerts sent when a reservation is created, confirmed or
 * cancelled). Channels are simulated for coursework purposes - see docs/SETUP.md.
 */
@Entity
@Table(name = "notification_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_number", nullable = false, length = 20)
    private String reservationNumber;

    @Column(nullable = false, length = 20)
    private String channel; // EMAIL, SMS, AUDIT

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }
}
