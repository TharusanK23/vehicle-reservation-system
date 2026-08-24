package com.sunrise.vehiclereservation.controller;

import com.sunrise.vehiclereservation.dto.request.SettlePaymentRequest;
import com.sunrise.vehiclereservation.dto.response.BillResponse;
import com.sunrise.vehiclereservation.service.BillService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** "Calculate and Print Bill" from the brief. */
@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping("/reservation/{reservationNumber}")
    public BillResponse generateOrFetch(@PathVariable String reservationNumber) {
        return billService.generateOrFetch(reservationNumber);
    }

    @PostMapping("/{billNumber}/settle")
    public BillResponse settle(@PathVariable String billNumber, @Valid @RequestBody SettlePaymentRequest request) {
        return billService.settlePayment(billNumber, request);
    }
}
