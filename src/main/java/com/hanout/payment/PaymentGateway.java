package com.hanout.payment;

import com.hanout.common.Money;
import java.util.Map;

public interface PaymentGateway {

    record PaymentRequest(
            Long orderId,
            Money amount,
            String currency,
            String description,
            String idempotencyKey
    ) {}

    record PaymentIntent(
            String externalRef,
            PaymentStatus status,
            String redirectUrl
    ) {}

    record Refund(
            String refundId,
            PaymentStatus status
    ) {}

    record WebhookEvent(
            String eventType,
            String externalRef,
            PaymentStatus status,
            boolean verified
    ) {}

    String getGatewayCode();

    PaymentIntent create(PaymentRequest request);

    PaymentStatus status(String externalRef);

    Refund refund(String externalRef, Money amount);

    WebhookEvent verifyWebhook(Map<String, String> headers, byte[] body);
}
