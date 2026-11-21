package com.hanout.ordering;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vendor-orders")
public class VendorOrderController {

    private final VendorOrderService vendorOrderService;

    public VendorOrderController(VendorOrderService vendorOrderService) {
        this.vendorOrderService = vendorOrderService;
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<VendorOrder> accept(@PathVariable Long id, Authentication auth) {
        Long vendorId = auth != null && auth.getPrincipal() instanceof Long ? (Long) auth.getPrincipal() : 1L;
        return ResponseEntity.ok(vendorOrderService.acceptVendorOrder(id, vendorId));
    }

    @PostMapping("/{id}/ship")
    public ResponseEntity<VendorOrder> ship(@PathVariable Long id, Authentication auth) {
        Long vendorId = auth != null && auth.getPrincipal() instanceof Long ? (Long) auth.getPrincipal() : 1L;
        return ResponseEntity.ok(vendorOrderService.shipVendorOrder(id, vendorId));
    }

    @PostMapping("/{id}/deliver")
    public ResponseEntity<VendorOrder> deliver(@PathVariable Long id) {
        return ResponseEntity.ok(vendorOrderService.deliverVendorOrder(id));
    }
}
