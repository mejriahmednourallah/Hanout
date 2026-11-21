package com.hanout.payment;

import com.hanout.common.Money;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class KonnectGateway implements PaymentGateway {

    @Override
    public String getGatewayCode() {
        return "KONNECT";
    }

    @Override
    public PaymentIntent create(PaymentRequest request) {
        String ref = "KONNECT-" + request.orderId() + "-" + UUID.randomUUID().toString().substring(0, 8);
        return new PaymentIntent(ref, PaymentStatus.REQUIRES_ACTION, "https://api.konnect.network/pay/" + ref);
    }

    @Override
    public PaymentStatus status(String externalRef) {
        return PaymentStatus.SUCCESS;
    }

    @Override
    public Refund refund(String externalRef, Money amount) {
        return new Refund("REF-KONNECT-" + UUID.randomUUID(), PaymentStatus.SUCCESS);
    }

    @Override
    public WebhookEvent verifyWebhook(Map<String, String> headers, byte[] body) {
        // Konnect webhook signature verification mock
        String sig = headers.get("x-konnect-signature");
        boolean valid = sig != null && !sig.isBlank();
        return new WebhookEvent("PAYMENT_COMPLETED", "KONNECT_REF", PaymentStatus.SUCCESS, valid);
    }
}
