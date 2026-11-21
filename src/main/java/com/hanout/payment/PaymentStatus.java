package com.hanout.payment;

import com.hanout.common.Money;

public enum PaymentStatus {
    PENDING_ON_DELIVERY,
    REQUIRES_ACTION,
    SUCCESS,
    FAILED,
    CANCELLED
}
