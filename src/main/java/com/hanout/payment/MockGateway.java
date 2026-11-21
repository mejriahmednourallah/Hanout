package com.hanout.payment;

import com.hanout.common.Money;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class MockGateway implements PaymentGateway {

    @Override
    public String getGatewayCode() {
        return "MOCK";
    }

    @Override
    public PaymentIntent create(PaymentRequest request) {
        String ref = "MOCK-PAY-" + request.orderId() + "-" + UUID.randomUUID().toString().substring(0, 8);
        return new PaymentIntent(ref, PaymentStatus.SUCCESS, "https://mock-gateway.hanout.tn/pay/" + ref);
    }

    @Override
    public PaymentStatus status(String externalRef) {
        return PaymentStatus.SUCCESS;
    }

    @Override
    public Refund refund(String externalRef, Money amount) {
        return new Refund("REF-MOCK-" + UUID.randomUUID(), PaymentStatus.SUCCESS);
    }

    @Override
    public WebhookEvent verifyWebhook(Map<String, String> headers, byte[] body) {
        return new WebhookEvent("PAYMENT_SUCCESS", "MOCK_EVENT", PaymentStatus.SUCCESS, true);
    }
}
