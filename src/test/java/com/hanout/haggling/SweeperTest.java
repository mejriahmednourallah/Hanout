package com.hanout.haggling;

import com.hanout.catalog.Sku;
import com.hanout.catalog.SkuRepository;
import com.hanout.common.Money;
import com.hanout.inventory.InventoryService;
import com.hanout.inventory.ReservationReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SweeperTest {

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private SkuRepository skuRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private HagglingSweeper sweeper;

    @Test
    @DisplayName("Sweeper: Expired locked offer releases reserved stock and moves to EXPIRED")
    void testExpiredLockRelease() {
        // Find existing SKU and set initial stock
        Sku sku = skuRepository.findById(3L).orElseThrow();
        sku.setStockOnHand(10);
        sku.setReserved(0);
        sku = skuRepository.saveAndFlush(sku);

        // Create an ACCEPTED offer whose lock expired 5 minutes ago
        Offer offer = new Offer(sku.getId(), 1L, 1L, 2, Money.ofMillimes(100000), Money.ofMillimes(95000), LocalDateTime.now().plusHours(24));
        offer.setStatus(OfferStatus.ACCEPTED);
        offer.setLockExpiresAt(LocalDateTime.now().minusMinutes(5)); // expired!
        offer = offerRepository.save(offer);

        // Reserve 2 units of stock under this offer
        inventoryService.reserveStock(sku.getId(), 2, ReservationReason.OFFER, "OFFER-" + offer.getId(), LocalDateTime.now().plusMinutes(30));

        Sku reservedSku = skuRepository.findById(sku.getId()).orElseThrow();
        assertThat(reservedSku.getReserved()).isEqualTo(2);

        // Run Sweeper
        sweeper.sweepExpiredOffers();

        // Assert Offer status is EXPIRED
        Offer sweptOffer = offerRepository.findById(offer.getId()).orElseThrow();
        assertThat(sweptOffer.getStatus()).isEqualTo(OfferStatus.EXPIRED);

        // Assert Reserved stock was released
        Sku releasedSku = skuRepository.findById(sku.getId()).orElseThrow();
        assertThat(releasedSku.getReserved()).isEqualTo(0);
        assertThat(releasedSku.getAvailableStock()).isEqualTo(10);
    }
}
