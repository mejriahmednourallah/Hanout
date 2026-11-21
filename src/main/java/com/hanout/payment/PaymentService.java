package com.hanout.payment;

import com.hanout.common.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final Map<String, PaymentGateway> gateways;
    private final PaymentRepository paymentRepository;

    public PaymentService(List<PaymentGateway> gatewayList, PaymentRepository paymentRepository) {
        this.gateways = gatewayList.stream()
                .collect(Collectors.toMap(PaymentGateway::getGatewayCode, Function.identity()));
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment processPayment(Long orderId, String gatewayCode, Money amount, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Payment> existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        PaymentGateway gateway = gateways.getOrDefault(gatewayCode.toUpperCase(), gateways.get("COD"));
        PaymentGateway.PaymentIntent intent = gateway.create(new PaymentGateway.PaymentRequest(
                orderId, amount, "TND", "Order #" + orderId, idempotencyKey
        ));

        Payment payment = new Payment(orderId, gateway.getGatewayCode(), intent.externalRef(), amount, intent.status(), idempotencyKey);
        return paymentRepository.save(payment);
    }

    public Optional<Payment> findByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}
