package com.sunrise.vehiclereservation.repository;

import com.sunrise.vehiclereservation.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findByReservationNumber(String reservationNumber);
}
