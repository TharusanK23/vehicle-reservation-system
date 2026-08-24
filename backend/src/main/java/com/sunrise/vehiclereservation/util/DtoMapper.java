package com.sunrise.vehiclereservation.util;

import com.sunrise.vehiclereservation.dto.response.*;
import com.sunrise.vehiclereservation.entity.*;

/** Pure, stateless mapping functions between JPA entities and the DTOs exposed over the REST API. */
public final class DtoMapper {

    private DtoMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(), user.getRole());
    }

    public static CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getFullName(), customer.getAddress(),
                customer.getContactNumber(), customer.getEmail(), customer.getLicenseNumber());
    }

    public static VehicleCategoryResponse toResponse(VehicleCategory category) {
        return new VehicleCategoryResponse(category.getId(), category.getCategoryName(), category.getDailyRate(), category.getDescription());
    }

    public static VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(vehicle.getId(), vehicle.getRegistrationNumber(), vehicle.getMake(),
                vehicle.getModel(), vehicle.getManufactureYear(), toResponse(vehicle.getCategory()), vehicle.getStatus());
    }

    public static ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getReservationNumber(),
                toResponse(reservation.getCustomer()),
                toResponse(reservation.getVehicle()),
                reservation.getPickupDate(),
                reservation.getPickupTime(),
                reservation.getReturnDate(),
                reservation.getReturnTime(),
                reservation.getStatus(),
                reservation.getNotes(),
                reservation.getCreatedBy().getUsername(),
                reservation.getCreatedAt()
        );
    }

    public static BillResponse toResponse(Bill bill) {
        return new BillResponse(
                bill.getId(),
                bill.getBillNumber(),
                toResponse(bill.getReservation()),
                bill.getNumberOfDays(),
                bill.getDailyRate(),
                bill.getSubtotal(),
                bill.getSurchargeAmount(),
                bill.getDiscountAmount(),
                bill.getTaxAmount(),
                bill.getTotalAmount(),
                bill.getPricingStrategy(),
                bill.getPaymentStatus(),
                bill.getPaymentMethod(),
                bill.getGeneratedAt()
        );
    }
}
