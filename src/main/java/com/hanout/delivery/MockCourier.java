package com.hanout.delivery;

import com.hanout.common.Money;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class MockCourier implements Courier {

    @Override
    public String getCourierCode() {
        return "RAPID_POST_TN";
    }

    @Override
    public ShipmentResult book(ShipmentRequest request) {
        String trackingNo = "RP-TN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new ShipmentResult(getCourierCode(), trackingNo, "BOOKED");
    }

    @Override
    public TrackingStatus track(String trackingNumber) {
        return new TrackingStatus(trackingNumber, "IN_TRANSIT", "Centre de Tri Tunis", LocalDateTime.now());
    }

    @Override
    public CodRemittance remittances(LocalDate day) {
        return new CodRemittance(day, List.of(), Money.ZERO);
    }
}
