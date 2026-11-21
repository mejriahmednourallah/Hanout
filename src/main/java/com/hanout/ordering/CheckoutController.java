package com.hanout.ordering;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {

    private final CheckoutSagaService sagaService;

    public CheckoutController(CheckoutSagaService sagaService) {
        this.sagaService = sagaService;
    }

    public record CheckoutRequest(
            String phone,
            String shippingAddress,
            Long governorateId,
            String paymentGateway
    ) {}

    @PostMapping
    public ResponseEntity<CheckoutSagaService.CheckoutResponse> checkout(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody CheckoutRequest request,
            Authentication auth) {
        Long buyerId = auth != null && auth.getPrincipal() instanceof Long ? (Long) auth.getPrincipal() : 1L;

        CheckoutSagaService.CheckoutCommand cmd = new CheckoutSagaService.CheckoutCommand(
                buyerId,
                request.phone() != null ? request.phone() : "+21620111222",
                request.shippingAddress() != null ? request.shippingAddress() : "Avenue Habib Bourguiba, Tunis",
                request.governorateId() != null ? request.governorateId() : 1L,
                request.paymentGateway() != null ? request.paymentGateway() : "COD",
                idempotencyKey
        );

        CheckoutSagaService.CheckoutResponse response = sagaService.execute(cmd);
        return ResponseEntity.ok(response);
    }
}
