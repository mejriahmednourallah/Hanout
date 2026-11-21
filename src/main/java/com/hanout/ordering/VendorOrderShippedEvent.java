package com.hanout.ordering;

public record VendorOrderShippedEvent(
        Long vendorOrderId,
        Long vendorId,
        String trackingNumber
) {}
