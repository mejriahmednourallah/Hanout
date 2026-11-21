package com.hanout.ordering;

import com.hanout.common.Money;
import java.util.List;

public record OrderPlacedEvent(
        Long orderId,
        Long buyerId,
        Money totalAmount,
        List<Long> vendorOrderIds,
        String paymentGateway,
        String phone,
        String shippingAddress
) {}
