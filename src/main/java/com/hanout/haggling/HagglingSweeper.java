package com.hanout.haggling;

import com.hanout.inventory.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class HagglingSweeper {

    private static final Logger log = LoggerFactory.getLogger(HagglingSweeper.class);

    private final OfferRepository offerRepository;
    private final OfferEventRepository eventRepository;
    private final InventoryService inventoryService;

    public HagglingSweeper(OfferRepository offerRepository,
                           OfferEventRepository eventRepository,
                           InventoryService inventoryService) {
        this.offerRepository = offerRepository;
        this.eventRepository = eventRepository;
        this.inventoryService = inventoryService;
    }

    @Scheduled(fixedDelayString = "${hanout.haggling.sweeper.interval-ms:15000}")
    @Transactional
    public void sweepExpiredOffers() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Pending/Countered timeouts
        List<Offer> expiredNegotiations = offerRepository.findExpiredPendingOffers(now);
        for (Offer offer : expiredNegotiations) {
            offer.setStatus(OfferStatus.EXPIRED);
            offer.setUpdatedAt(now);
            offerRepository.save(offer);
            eventRepository.save(new OfferEvent(offer.getId(), OfferActor.SYSTEM, OfferEventType.EXPIRED, offer.getCurrentAmount()));
            log.info("Expired unaccepted offer {}", offer.getId());
        }

        // 2. Lock timeout for accepted offers: releases stock!
        List<Offer> expiredLocks = offerRepository.findExpiredLockedOffers(now);
        for (Offer offer : expiredLocks) {
            inventoryService.releaseReservation("OFFER-" + offer.getId());
            offer.setStatus(OfferStatus.EXPIRED);
            offer.setUpdatedAt(now);
            offerRepository.save(offer);
            eventRepository.save(new OfferEvent(offer.getId(), OfferActor.SYSTEM, OfferEventType.EXPIRED, offer.getCurrentAmount()));
            log.info("Lock expired for offer {}. Released held stock reservation.", offer.getId());
        }
    }
}
