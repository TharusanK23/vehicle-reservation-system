package com.sunrise.vehiclereservation.service;

import com.sunrise.vehiclereservation.dto.request.SettlePaymentRequest;
import com.sunrise.vehiclereservation.dto.response.BillResponse;

public interface BillService {
    BillResponse generateOrFetch(String reservationNumber);
    BillResponse settlePayment(String billNumber, SettlePaymentRequest request);
}
