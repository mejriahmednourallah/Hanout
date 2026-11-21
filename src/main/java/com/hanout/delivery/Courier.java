package com.hanout.delivery;

import com.hanout.common.Money;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface Courier {

    record ShipmentRequest(
            Long vendorOrderId,
            String recipientName,
            String phone,
            String address,
            Money codAmount
    ) {}

    record ShipmentResult(
            String courierCode,
            String trackingNumber,
            String status
    ) {}

    record TrackingStatus(
            String trackingNumber,
            String status,
            String lastLocation,
            LocalDateTime timestamp
    ) {}

    record RemittanceItem(
            String trackingNumber,
            Long vendorOrderId,
            Money amountCollected
    ) {}

    record CodRemittance(
            LocalDate day,
            List<RemittanceItem> items,
            Money totalCollected
    ) {}

    String getCourierCode();

    ShipmentResult book(ShipmentRequest request);

    TrackingStatus track(String trackingNumber);

    CodRemittance remittances(LocalDate day);
}
