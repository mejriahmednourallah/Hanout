package com.hanout.haggling;

import com.hanout.catalog.CatalogService;
import com.hanout.catalog.Product;
import com.hanout.catalog.Sku;
import com.hanout.common.Money;
import com.hanout.inventory.InventoryService;
import com.hanout.inventory.ReservationReason;
import com.hanout.vendor.HagglingPolicy;
import com.hanout.vendor.VendorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HagglingService {

    private final OfferRepository offerRepository;
    private final OfferEventRepository eventRepository;
    private final CatalogService catalogService;
    private final VendorService vendorService;
    private final InventoryService inventoryService;
    private final HagglingRateLimiter rateLimiter;

    public HagglingService(OfferRepository offerRepository,
                           OfferEventRepository eventRepository,
                           CatalogService catalogService,
                           VendorService vendorService,
                           InventoryService inventoryService,
                           HagglingRateLimiter rateLimiter) {
        this.offerRepository = offerRepository;
        this.eventRepository = eventRepository;
        this.catalogService = catalogService;
        this.vendorService = vendorService;
        this.inventoryService = inventoryService;
        this.rateLimiter = rateLimiter;
    }

    @Transactional
    public Offer submitOffer(Long buyerId, Long skuId, int qty, Money proposedAmount) {
        if (!rateLimiter.tryConsume(buyerId)) {
            throw new HagglingExceptions.RateLimitExceededException("Too many haggling requests. Please slow down.");
        }

        if (offerRepository.findActiveOfferByBuyerAndSku(buyerId, skuId).isPresent()) {
            throw new HagglingExceptions.ConflictException("You already have an active negotiation for this SKU.");
        }

        Sku sku = catalogService.findSkuById(skuId)
                .orElseThrow(() -> new IllegalArgumentException("SKU not found: " + skuId));
        Product product = catalogService.findProductById(sku.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + sku.getProductId()));

        HagglingPolicy policy = vendorService.getEffectivePolicy(product.getVendorId(), product.getId());

        Money unitListPrice = sku.getPriceMillimes();
        Money totalListPrice = unitListPrice.multiply(qty);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(policy.getOfferTtlHours());

        Offer offer = new Offer(skuId, buyerId, product.getVendorId(), qty, totalListPrice, proposedAmount, expiresAt);
        offer = offerRepository.save(offer);

        eventRepository.save(new OfferEvent(offer.getId(), OfferActor.BUYER, OfferEventType.SUBMITTED, proposedAmount));

        // Evaluate Auto-rules
        BigDecimal pct = BigDecimal.valueOf(proposedAmount.millimes())
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalListPrice.millimes()), 2, RoundingMode.HALF_UP);

        Money totalFloor = policy.getFloorMillimes().multiply(qty);

        if (proposedAmount.isLessThan(totalFloor) || pct.compareTo(policy.getAutoRejectPct()) < 0) {
            // Auto reject
            offer.setStatus(OfferStatus.REJECTED);
            offer.setUpdatedAt(now);
            offer = offerRepository.save(offer);
            eventRepository.save(new OfferEvent(offer.getId(), OfferActor.SYSTEM, OfferEventType.REJECTED, proposedAmount));
        } else if (pct.compareTo(policy.getAutoAcceptPct()) >= 0) {
            // Auto accept & reserve stock immediately
            LocalDateTime lockExpiresAt = now.plusMinutes(policy.getPriceLockMinutes());
            inventoryService.reserveStock(skuId, qty, ReservationReason.OFFER, "OFFER-" + offer.getId(), lockExpiresAt);
            offer.setStatus(OfferStatus.ACCEPTED);
            offer.setLockExpiresAt(lockExpiresAt);
            offer.setUpdatedAt(now);
            offer = offerRepository.save(offer);
            eventRepository.save(new OfferEvent(offer.getId(), OfferActor.SYSTEM, OfferEventType.ACCEPTED, proposedAmount));
        }

        return offer;
    }

    @Transactional
    public Offer vendorAccept(Long offerId, Long vendorId) {
        Offer offer = getOfferForVendor(offerId, vendorId);
        if (!offer.getStatus().canMoveTo(OfferStatus.ACCEPTED)) {
            throw new HagglingExceptions.InvalidActionException("Cannot accept offer in status: " + offer.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        int updated = offerRepository.updateStatusGuarded(offer.getId(), offer.getStatus(), OfferStatus.ACCEPTED, offer.getVersion(), now);
        if (updated == 0) {
            throw new HagglingExceptions.ConflictException("Offer state changed concurrently. Please refresh.");
        }

        Product product = catalogService.findProductById(catalogService.findSkuById(offer.getSkuId()).orElseThrow().getProductId()).orElseThrow();
        HagglingPolicy policy = vendorService.getEffectivePolicy(vendorId, product.getId());
        LocalDateTime lockExpiresAt = now.plusMinutes(policy.getPriceLockMinutes());

        // Reserve stock
        inventoryService.reserveStock(offer.getSkuId(), offer.getQty(), ReservationReason.OFFER, "OFFER-" + offer.getId(), lockExpiresAt);

        offer = offerRepository.findById(offerId).orElseThrow();
        offer.setLockExpiresAt(lockExpiresAt);
        offerRepository.save(offer);

        eventRepository.save(new OfferEvent(offer.getId(), OfferActor.VENDOR, OfferEventType.ACCEPTED, offer.getCurrentAmount()));
        return offer;
    }

    @Transactional
    public Offer vendorReject(Long offerId, Long vendorId) {
        Offer offer = getOfferForVendor(offerId, vendorId);
        if (!offer.getStatus().canMoveTo(OfferStatus.REJECTED)) {
            throw new HagglingExceptions.InvalidActionException("Cannot reject offer in status: " + offer.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        int updated = offerRepository.updateStatusGuarded(offer.getId(), offer.getStatus(), OfferStatus.REJECTED, offer.getVersion(), now);
        if (updated == 0) {
            throw new HagglingExceptions.ConflictException("Offer state changed concurrently. Please refresh.");
        }

        eventRepository.save(new OfferEvent(offer.getId(), OfferActor.VENDOR, OfferEventType.REJECTED, offer.getCurrentAmount()));
        return offerRepository.findById(offerId).orElseThrow();
    }

    @Transactional
    public Offer vendorCounter(Long offerId, Long vendorId, Money counterAmount) {
        Offer offer = getOfferForVendor(offerId, vendorId);
        if (!offer.getStatus().canMoveTo(OfferStatus.COUNTERED)) {
            throw new HagglingExceptions.InvalidActionException("Cannot counter offer in status: " + offer.getStatus());
        }

        Product product = catalogService.findProductById(catalogService.findSkuById(offer.getSkuId()).orElseThrow().getProductId()).orElseThrow();
        HagglingPolicy policy = vendorService.getEffectivePolicy(vendorId, product.getId());

        if (offer.getRound() >= policy.getMaxRounds()) {
            throw new HagglingExceptions.InvalidActionException("Maximum negotiation rounds reached (" + policy.getMaxRounds() + ")");
        }

        if (counterAmount.isLessThan(policy.getFloorMillimes().multiply(offer.getQty()))) {
            throw new HagglingExceptions.InvalidActionException("Counter amount is below vendor floor price.");
        }

        LocalDateTime now = LocalDateTime.now();
        offer.setStatus(OfferStatus.COUNTERED);
        offer.setRound(offer.getRound() + 1);
        offer.setCurrentAmount(counterAmount);
        offer.setExpiresAt(now.plusHours(policy.getOfferTtlHours()));
        offer.setUpdatedAt(now);
        offer = offerRepository.save(offer);

        eventRepository.save(new OfferEvent(offer.getId(), OfferActor.VENDOR, OfferEventType.COUNTERED, counterAmount));
        return offer;
    }

    @Transactional
    public Offer buyerAccept(Long offerId, Long buyerId) {
        Offer offer = getOfferForBuyer(offerId, buyerId);
        if (!offer.getStatus().canMoveTo(OfferStatus.ACCEPTED)) {
            throw new HagglingExceptions.InvalidActionException("Cannot accept offer in status: " + offer.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        int updated = offerRepository.updateStatusGuarded(offer.getId(), offer.getStatus(), OfferStatus.ACCEPTED, offer.getVersion(), now);
        if (updated == 0) {
            throw new HagglingExceptions.ConflictException("Offer state changed concurrently. Please refresh.");
        }

        Product product = catalogService.findProductById(catalogService.findSkuById(offer.getSkuId()).orElseThrow().getProductId()).orElseThrow();
        HagglingPolicy policy = vendorService.getEffectivePolicy(offer.getVendorId(), product.getId());
        LocalDateTime lockExpiresAt = now.plusMinutes(policy.getPriceLockMinutes());

        inventoryService.reserveStock(offer.getSkuId(), offer.getQty(), ReservationReason.OFFER, "OFFER-" + offer.getId(), lockExpiresAt);

        offer = offerRepository.findById(offerId).orElseThrow();
        offer.setLockExpiresAt(lockExpiresAt);
        offerRepository.save(offer);

        eventRepository.save(new OfferEvent(offer.getId(), OfferActor.BUYER, OfferEventType.ACCEPTED, offer.getCurrentAmount()));
        return offer;
    }

    @Transactional
    public Offer buyerCounter(Long offerId, Long buyerId, Money counterAmount) {
        Offer offer = getOfferForBuyer(offerId, buyerId);
        if (!offer.getStatus().canMoveTo(OfferStatus.PENDING)) {
            throw new HagglingExceptions.InvalidActionException("Cannot counter offer in status: " + offer.getStatus());
        }

        Product product = catalogService.findProductById(catalogService.findSkuById(offer.getSkuId()).orElseThrow().getProductId()).orElseThrow();
        HagglingPolicy policy = vendorService.getEffectivePolicy(offer.getVendorId(), product.getId());

        if (offer.getRound() >= policy.getMaxRounds()) {
            throw new HagglingExceptions.InvalidActionException("Maximum negotiation rounds reached (" + policy.getMaxRounds() + ")");
        }

        LocalDateTime now = LocalDateTime.now();
        offer.setStatus(OfferStatus.PENDING);
        offer.setRound(offer.getRound() + 1);
        offer.setCurrentAmount(counterAmount);
        offer.setExpiresAt(now.plusHours(policy.getOfferTtlHours()));
        offer.setUpdatedAt(now);
        offer = offerRepository.save(offer);

        eventRepository.save(new OfferEvent(offer.getId(), OfferActor.BUYER, OfferEventType.COUNTERED, counterAmount));
        return offer;
    }

    @Transactional
    public Offer buyerWithdraw(Long offerId, Long buyerId) {
        Offer offer = getOfferForBuyer(offerId, buyerId);
        if (!offer.getStatus().canMoveTo(OfferStatus.WITHDRAWN)) {
            throw new HagglingExceptions.InvalidActionException("Cannot withdraw offer in status: " + offer.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        int updated = offerRepository.updateStatusGuarded(offer.getId(), offer.getStatus(), OfferStatus.WITHDRAWN, offer.getVersion(), now);
        if (updated == 0) {
            throw new HagglingExceptions.ConflictException("Offer state changed concurrently. Please refresh.");
        }

        eventRepository.save(new OfferEvent(offer.getId(), OfferActor.BUYER, OfferEventType.WITHDRAWN, offer.getCurrentAmount()));
        return offerRepository.findById(offerId).orElseThrow();
    }

    @Transactional
    public void convertOffer(Long offerId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + offerId));
        if (offer.getStatus() != OfferStatus.ACCEPTED) {
            throw new IllegalStateException("Only ACCEPTED offers can be converted at checkout");
        }
        offer.setStatus(OfferStatus.CONVERTED);
        offer.setUpdatedAt(LocalDateTime.now());
        offerRepository.save(offer);
    }

    public Optional<Offer> findById(Long id) {
        return offerRepository.findById(id);
    }

    public List<Offer> findPendingOffersForVendor(Long vendorId) {
        return offerRepository.findByVendorIdAndStatus(vendorId, OfferStatus.PENDING);
    }

    public List<OfferEvent> getOfferHistory(Long offerId) {
        return eventRepository.findByOfferIdOrderByCreatedAtAsc(offerId);
    }

    private Offer getOfferForVendor(Long offerId, Long vendorId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + offerId));
        if (!offer.getVendorId().equals(vendorId)) {
            throw new HagglingExceptions.ConflictException("Offer does not belong to vendor: " + vendorId);
        }
        return offer;
    }

    private Offer getOfferForBuyer(Long offerId, Long buyerId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + offerId));
        if (!offer.getBuyerId().equals(buyerId)) {
            throw new HagglingExceptions.ConflictException("Offer does not belong to buyer: " + buyerId);
        }
        return offer;
    }
}
