package com.sunrise.vehiclereservation.service.impl;

import com.sunrise.vehiclereservation.dto.request.RegisterReservationRequest;
import com.sunrise.vehiclereservation.dto.response.ReservationResponse;
import com.sunrise.vehiclereservation.entity.Customer;
import com.sunrise.vehiclereservation.entity.Reservation;
import com.sunrise.vehiclereservation.entity.ReservationStatus;
import com.sunrise.vehiclereservation.entity.User;
import com.sunrise.vehiclereservation.entity.Vehicle;
import com.sunrise.vehiclereservation.entity.VehicleStatus;
import com.sunrise.vehiclereservation.exception.BusinessRuleException;
import com.sunrise.vehiclereservation.exception.ResourceNotFoundException;
import com.sunrise.vehiclereservation.pattern.builder.ReservationBuilder;
import com.sunrise.vehiclereservation.pattern.observer.ReservationEvent;
import com.sunrise.vehiclereservation.pattern.observer.ReservationEventPublisher;
import com.sunrise.vehiclereservation.repository.CustomerRepository;
import com.sunrise.vehiclereservation.repository.ReservationRepository;
import com.sunrise.vehiclereservation.repository.UserRepository;
import com.sunrise.vehiclereservation.repository.VehicleRepository;
import com.sunrise.vehiclereservation.service.ReservationService;
import com.sunrise.vehiclereservation.util.DtoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Implements "Register New Appointment", "Display Appointment Details" and the
 * reservation-lifecycle portion of the brief (adapted to vehicle bookings). Combines
 * the {@link ReservationBuilder} (Builder), {@link ReservationEventPublisher}
 * (Observer) and the repository layer (DAO) to fulfil Task B's design-pattern and
 * database requirements together in one cohesive workflow.
 */
@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final ReservationEventPublisher eventPublisher;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                   CustomerRepository customerRepository,
                                   VehicleRepository vehicleRepository,
                                   UserRepository userRepository,
                                   ReservationEventPublisher eventPublisher) {
        this.reservationRepository = reservationRepository;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ReservationResponse register(RegisterReservationRequest request, String createdByUsername) {
        Customer customer = resolveCustomer(request);

        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + request.vehicleId()));

        if (vehicle.getStatus() == VehicleStatus.MAINTENANCE || vehicle.getStatus() == VehicleStatus.INACTIVE) {
            throw new BusinessRuleException("Vehicle " + vehicle.getRegistrationNumber() + " is not available for booking (status: " + vehicle.getStatus() + ").");
        }

        List<Reservation> overlapping = reservationRepository.findOverlappingForVehicle(
                vehicle.getId(), request.pickupDate(), request.returnDate());
        if (!overlapping.isEmpty()) {
            throw new BusinessRuleException("Vehicle " + vehicle.getRegistrationNumber()
                    + " is already reserved for an overlapping date range (existing reservation "
                    + overlapping.get(0).getReservationNumber() + "). This check is also enforced at the database level by trg_prevent_double_booking.");
        }

        User createdBy = userRepository.findByUsername(createdByUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found: " + createdByUsername));

        Reservation reservation = new ReservationBuilder()
                .withCustomer(customer)
                .withVehicle(vehicle)
                .withPickup(request.pickupDate(), request.pickupTime())
                .withReturn(request.returnDate(), request.returnTime())
                .withNotes(request.notes())
                .withCreatedBy(createdBy)
                .withStatus(ReservationStatus.CONFIRMED)
                .build();

        reservation = reservationRepository.save(reservation);

        vehicle.setStatus(VehicleStatus.RESERVED);
        vehicleRepository.save(vehicle);

        eventPublisher.publish(new ReservationEvent(reservation, ReservationEvent.ReservationEventType.CREATED));

        return DtoMapper.toResponse(reservation);
    }

    private Customer resolveCustomer(RegisterReservationRequest request) {
        if (request.customerId() != null) {
            return customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.customerId()));
        }
        if (!StringUtils.hasText(request.customerFullName()) || !StringUtils.hasText(request.customerAddress())
                || !StringUtils.hasText(request.customerContactNumber())) {
            throw new BusinessRuleException("Either an existing customerId or the customer's full name, address and contact number must be provided.");
        }
        Customer customer = Customer.builder()
                .fullName(request.customerFullName())
                .address(request.customerAddress())
                .contactNumber(request.customerContactNumber())
                .email(request.customerEmail())
                .licenseNumber(request.customerLicenseNumber())
                .build();
        return customerRepository.save(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse findByReservationNumber(String reservationNumber) {
        return reservationRepository.findByReservationNumber(reservationNumber)
                .map(DtoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No reservation found with number: " + reservationNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream().map(DtoMapper::toResponse).toList();
    }

    @Override
    public ReservationResponse cancel(String reservationNumber) {
        Reservation reservation = reservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("No reservation found with number: " + reservationNumber));

        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new BusinessRuleException("A completed reservation cannot be cancelled.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        Vehicle vehicle = reservation.getVehicle();
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(vehicle);

        eventPublisher.publish(new ReservationEvent(reservation, ReservationEvent.ReservationEventType.CANCELLED));
        return DtoMapper.toResponse(reservation);
    }

    @Override
    public ReservationResponse updateStatus(String reservationNumber, String status) {
        Reservation reservation = reservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("No reservation found with number: " + reservationNumber));

        ReservationStatus newStatus;
        try {
            newStatus = ReservationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("Unknown reservation status: " + status);
        }

        reservation.setStatus(newStatus);
        reservationRepository.save(reservation);

        if (newStatus == ReservationStatus.COMPLETED || newStatus == ReservationStatus.CANCELLED) {
            Vehicle vehicle = reservation.getVehicle();
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
        }

        eventPublisher.publish(new ReservationEvent(reservation, ReservationEvent.ReservationEventType.CONFIRMED));
        return DtoMapper.toResponse(reservation);
    }
}
