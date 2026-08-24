package com.sunrise.vehiclereservation.service.impl;

import com.sunrise.vehiclereservation.dto.request.SettlePaymentRequest;
import com.sunrise.vehiclereservation.dto.response.BillResponse;
import com.sunrise.vehiclereservation.entity.Bill;
import com.sunrise.vehiclereservation.entity.PaymentStatus;
import com.sunrise.vehiclereservation.entity.Reservation;
import com.sunrise.vehiclereservation.exception.ResourceNotFoundException;
import com.sunrise.vehiclereservation.pattern.factory.BillFactory;
import com.sunrise.vehiclereservation.pattern.observer.ReservationEvent;
import com.sunrise.vehiclereservation.pattern.observer.ReservationEventPublisher;
import com.sunrise.vehiclereservation.repository.BillRepository;
import com.sunrise.vehiclereservation.repository.ReservationRepository;
import com.sunrise.vehiclereservation.service.BillService;
import com.sunrise.vehiclereservation.util.DtoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements "Calculate and Print Bill". Bill amounts are computed exactly once per
 * reservation via {@link BillFactory} (Factory Method + Strategy) and persisted, so
 * repeated calls to the print/receipt endpoint always return the same figures instead
 * of silently re-pricing a reservation whose vehicle category rate may since have changed.
 */
@Service
@Transactional
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final ReservationRepository reservationRepository;
    private final BillFactory billFactory;
    private final ReservationEventPublisher eventPublisher;

    public BillServiceImpl(BillRepository billRepository, ReservationRepository reservationRepository,
                            BillFactory billFactory, ReservationEventPublisher eventPublisher) {
        this.billRepository = billRepository;
        this.reservationRepository = reservationRepository;
        this.billFactory = billFactory;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public BillResponse generateOrFetch(String reservationNumber) {
        Reservation reservation = reservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("No reservation found with number: " + reservationNumber));

        Bill bill = billRepository.findByReservationId(reservation.getId())
                .orElseGet(() -> {
                    Bill newBill = billFactory.create(reservation);
                    Bill saved = billRepository.save(newBill);
                    eventPublisher.publish(new ReservationEvent(reservation, ReservationEvent.ReservationEventType.BILL_GENERATED));
                    return saved;
                });

        return DtoMapper.toResponse(bill);
    }

    @Override
    public BillResponse settlePayment(String billNumber, SettlePaymentRequest request) {
        Bill bill = billRepository.findByBillNumber(billNumber)
                .orElseThrow(() -> new ResourceNotFoundException("No bill found with number: " + billNumber));
        bill.setPaymentStatus(PaymentStatus.PAID);
        bill.setPaymentMethod(request.paymentMethod());
        return DtoMapper.toResponse(billRepository.save(bill));
    }
}
