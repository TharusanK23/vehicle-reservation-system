package com.sunrise.vehiclereservation.repository;

import com.sunrise.vehiclereservation.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByReservationId(Long reservationId);

    Optional<Bill> findByBillNumber(String billNumber);

    @Query("""
           SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b
           WHERE b.generatedAt >= :startInclusive AND b.generatedAt < :endExclusive
           """)
    BigDecimal sumRevenueBetween(@Param("startInclusive") LocalDateTime startInclusive,
                                  @Param("endExclusive") LocalDateTime endExclusive);
}
