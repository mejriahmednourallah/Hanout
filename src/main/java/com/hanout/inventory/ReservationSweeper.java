package com.hanout.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReservationSweeper {

    private static final Logger log = LoggerFactory.getLogger(ReservationSweeper.class);
    private final InventoryService inventoryService;

    public ReservationSweeper(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Scheduled(fixedDelayString = "${hanout.sweeper.interval-ms:15000}")
    public void sweep() {
        try {
            int swept = inventoryService.sweepExpiredReservations(LocalDateTime.now());
            if (swept > 0) {
                log.info("Sweeper released {} expired stock reservations", swept);
            }
        } catch (Exception e) {
            log.error("Error during reservation sweeping", e);
        }
    }
}
