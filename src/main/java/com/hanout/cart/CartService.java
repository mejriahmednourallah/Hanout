package com.hanout.cart;

import com.hanout.catalog.CatalogService;
import com.hanout.catalog.Sku;
import com.hanout.common.Money;
import com.hanout.haggling.HagglingService;
import com.hanout.haggling.Offer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final CatalogService catalogService;
    private final HagglingService hagglingService;

    public CartService(CartItemRepository cartItemRepository,
                       CatalogService catalogService,
                       HagglingService hagglingService) {
        this.cartItemRepository = cartItemRepository;
        this.catalogService = catalogService;
        this.hagglingService = hagglingService;
    }

    public record ResolvedCartLine(
            Long cartItemId,
            Long skuId,
            String skuCode,
            Long vendorId,
            int qty,
            Long offerId,
            boolean isOfferLocked,
            Money unitPrice,
            Money lineTotal
    ) {}

    public record ResolvedCart(
            Long buyerId,
            List<ResolvedCartLine> lines,
            Money total
    ) {}

    @Transactional
    public CartItem addToCart(Long buyerId, Long skuId, int qty, Long offerId) {
        if (offerId != null) {
            Offer offer = hagglingService.findById(offerId)
                    .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + offerId));
            if (!offer.isLockValid()) {
                throw new IllegalStateException("Offer " + offerId + " does not have an active price lock");
            }
        }

        CartItem item = cartItemRepository.findByBuyerIdAndSkuId(buyerId, skuId)
                .orElseGet(() -> new CartItem(buyerId, skuId, qty, offerId));

        item.setQty(qty);
        item.setOfferId(offerId);
        return cartItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public ResolvedCart getCart(Long buyerId) {
        List<CartItem> items = cartItemRepository.findByBuyerId(buyerId);
        List<ResolvedCartLine> resolvedLines = new ArrayList<>();
        Money grandTotal = Money.ZERO;

        for (CartItem item : items) {
            Sku sku = catalogService.findSkuById(item.getSkuId())
                    .orElseThrow(() -> new IllegalStateException("SKU not found for cart item: " + item.getSkuId()));
            var product = catalogService.findProductById(sku.getProductId())
                    .orElseThrow(() -> new IllegalStateException("Product not found: " + sku.getProductId()));

            Money effectiveUnitPrice = sku.getPriceMillimes();
            boolean isOfferLocked = false;

            if (item.getOfferId() != null) {
                var offerOpt = hagglingService.findById(item.getOfferId());
                if (offerOpt.isPresent()) {
                    Offer offer = offerOpt.get();
                    if (offer.isLockValid()) {
                        isOfferLocked = true;
                        // Offer price for the negotiated qty
                        long unitMillimes = offer.getCurrentAmount().millimes() / Math.max(1, offer.getQty());
                        effectiveUnitPrice = Money.ofMillimes(unitMillimes);
                    }
                }
            }

            Money lineTotal = effectiveUnitPrice.multiply(item.getQty());
            grandTotal = grandTotal.plus(lineTotal);

            resolvedLines.add(new ResolvedCartLine(
                    item.getId(),
                    sku.getId(),
                    sku.getSkuCode(),
                    product.getVendorId(),
                    item.getQty(),
                    item.getOfferId(),
                    isOfferLocked,
                    effectiveUnitPrice,
                    lineTotal
            ));
        }

        return new ResolvedCart(buyerId, resolvedLines, grandTotal);
    }

    @Transactional
    public void clearCart(Long buyerId) {
        cartItemRepository.deleteByBuyerId(buyerId);
    }

    @Transactional
    public void removeItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }
}
