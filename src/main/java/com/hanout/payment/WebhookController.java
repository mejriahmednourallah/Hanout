package com.hanout.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private final PaymentService paymentService;

    public WebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{provider}")
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @PathVariable String provider,
            @RequestHeader Map<String, String> headers,
            @RequestBody(required = false) byte[] body) {
        // Return 200 OK acknowledging receipt idempotently
        return ResponseEntity.ok(Map.of(
                "status", "RECEIVED",
                "provider", provider
        ));
    }
}
