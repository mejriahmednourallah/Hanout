package com.hanout.payment;

import com.hanout.common.Money;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class CodGateway implements PaymentGateway {

    @Override
    public String getGatewayCode() {
        return "COD";
    }

    @Override
    public PaymentIntent create(PaymentRequest request) {
        String ref = "COD-" + request.orderId() + "-" + UUID.randomUUID().toString().substring(0, 8);
        return new PaymentIntent(ref, PaymentStatus.PENDING_ON_DELIVERY, null);
    }

    @Override
    public PaymentStatus status(String externalRef) {
        return PaymentStatus.PENDING_ON_DELIVERY;
    }

    @Override
    public Refund refund(String externalRef, Money amount) {
        return new Refund("REF-COD-" + UUID.randomUUID(), PaymentStatus.SUCCESS);
    }

    @Override
    public WebhookEvent verifyWebhook(Map<String, String> headers, byte[] body) {
        return new WebhookEvent("COD_CONFIRMED", "COD_EXTERNAL", PaymentStatus.SUCCESS, true);
    }
}
