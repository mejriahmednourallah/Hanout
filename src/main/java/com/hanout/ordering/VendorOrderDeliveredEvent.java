package com.hanout.ordering;

import com.hanout.common.Money;

public record VendorOrderDeliveredEvent(
        Long vendorOrderId,
        Long vendorId,
        Money subTotal,
        Money commissionAmount
) {}
