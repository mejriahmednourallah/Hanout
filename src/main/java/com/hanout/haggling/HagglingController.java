package com.hanout.haggling;

import com.hanout.common.Money;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class HagglingController {

    private final HagglingService hagglingService;

    public HagglingController(HagglingService hagglingService) {
        this.hagglingService = hagglingService;
    }

    public record SubmitOfferRequest(Long skuId, int qty, String amountDinar) {}
    public record CounterRequest(String amountDinar) {}

    @PostMapping("/offers")
    public ResponseEntity<Offer> submitOffer(@RequestBody SubmitOfferRequest request, Authentication auth) {
        Long buyerId = auth != null && auth.getPrincipal() instanceof Long ? (Long) auth.getPrincipal() : 1L; // default buyer 1 for testing
        Money proposed = Money.parse(request.amountDinar());
        Offer offer = hagglingService.submitOffer(buyerId, request.skuId(), request.qty(), proposed);
        return ResponseEntity.ok(offer);
    }

    @PostMapping("/offers/{id}/accept")
    public ResponseEntity<Offer> acceptOffer(@PathVariable Long id, Authentication auth) {
        Long vendorId = auth != null && auth.getPrincipal() instanceof Long ? (Long) auth.getPrincipal() : 1L; // default vendor 1
        Offer offer = hagglingService.vendorAccept(id, vendorId);
        return ResponseEntity.ok(offer);
    }

    @PostMapping("/offers/{id}/reject")
    public ResponseEntity<Offer> rejectOffer(@PathVariable Long id, Authentication auth) {
        Long vendorId = auth != null && auth.getPrincipal() instanceof Long ? (Long) auth.getPrincipal() : 1L;
        Offer offer = hagglingService.vendorReject(id, vendorId);
        return ResponseEntity.ok(offer);
    }

    @PostMapping("/offers/{id}/counter")
    public ResponseEntity<Offer> counterOffer(@PathVariable Long id,
                                             @RequestBody CounterRequest request,
                                             Authentication auth) {
        Long vendorId = auth != null && auth.getPrincipal() instanceof Long ? (Long) auth.getPrincipal() : 1L;
        Money counterAmount = Money.parse(request.amountDinar());
        Offer offer = hagglingService.vendorCounter(id, vendorId, counterAmount);
        return ResponseEntity.ok(offer);
    }

    @PostMapping("/offers/{id}/buyer-accept")
    public ResponseEntity<Offer> buyerAccept(@PathVariable Long id, Authentication auth) {
        Long buyerId = auth != null && auth.getPrincipal() instanceof Long ? (Long) auth.getPrincipal() : 1L;
        Offer offer = hagglingService.buyerAccept(id, buyerId);
        return ResponseEntity.ok(offer);
    }

    @PostMapping("/offers/{id}/buyer-counter")
    public ResponseEntity<Offer> buyerCounter(@PathVariable Long id,
                                             @RequestBody CounterRequest request,
                                             Authentication auth) {
        Long buyerId = auth != null && auth.getPrincipal() instanceof Long ? (Long) auth.getPrincipal() : 1L;
        Money counterAmount = Money.parse(request.amountDinar());
        Offer offer = hagglingService.buyerCounter(id, buyerId, counterAmount);
        return ResponseEntity.ok(offer);
    }

    @PostMapping("/offers/{id}/withdraw")
    public ResponseEntity<Offer> withdrawOffer(@PathVariable Long id, Authentication auth) {
        Long buyerId = auth != null && auth.getPrincipal() instanceof Long ? (Long) auth.getPrincipal() : 1L;
        Offer offer = hagglingService.buyerWithdraw(id, buyerId);
        return ResponseEntity.ok(offer);
    }

    @GetMapping("/vendors/me/offers")
    public ResponseEntity<List<Offer>> getVendorOffers(@RequestParam(defaultValue = "PENDING") String status,
                                                       Authentication auth) {
        Long vendorId = auth != null && auth.getPrincipal() instanceof Long ? (Long) auth.getPrincipal() : 1L;
        return ResponseEntity.ok(hagglingService.findPendingOffersForVendor(vendorId));
    }

    @GetMapping("/offers/{id}")
    public ResponseEntity<Offer> getOffer(@PathVariable Long id) {
        return hagglingService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/offers/{id}/events")
    public ResponseEntity<List<OfferEvent>> getOfferEvents(@PathVariable Long id) {
        return ResponseEntity.ok(hagglingService.getOfferHistory(id));
    }
}
