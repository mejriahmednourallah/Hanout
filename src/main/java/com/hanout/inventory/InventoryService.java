package com.hanout.inventory;

import com.hanout.catalog.SkuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private final SkuRepository skuRepository;
    private final StockReservationRepository reservationRepository;

    public InventoryService(SkuRepository skuRepository, StockReservationRepository reservationRepository) {
        this.skuRepository = skuRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public StockReservation reserveStock(Long skuId, int qty, ReservationReason reason, String refId, LocalDateTime expiresAt) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + qty);
        }

        // Atomic guarded update: affects 1 row only if stock_on_hand - reserved >= qty
        int updated = skuRepository.reserveStockAtomic(skuId, qty);
        if (updated == 0) {
            throw new OutOfStockException("Insufficient stock to reserve " + qty + " units for SKU " + skuId);
        }

        StockReservation reservation = new StockReservation(skuId, qty, reason, refId, expiresAt);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public void releaseReservation(String refId) {
        List<StockReservation> reservations = reservationRepository.findByRefId(refId);
        for (StockReservation res : reservations) {
            if ("ACTIVE".equals(res.getStatus())) {
                skuRepository.releaseStockAtomic(res.getSkuId(), res.getQty());
                res.setStatus("RELEASED");
                reservationRepository.save(res);
            }
        }
    }

    @Transactional
    public void commitReservation(String refId) {
        List<StockReservation> reservations = reservationRepository.findByRefId(refId);
        for (StockReservation res : reservations) {
            if ("ACTIVE".equals(res.getStatus())) {
                skuRepository.commitStockAtomic(res.getSkuId(), res.getQty());
                res.setStatus("COMMITTED");
                reservationRepository.save(res);
            }
        }
    }

    public Optional<StockReservation> findActiveReservation(String refId) {
        return reservationRepository.findByRefIdAndStatus(refId, "ACTIVE");
    }

    @Transactional
    public int sweepExpiredReservations(LocalDateTime cutoff) {
        List<StockReservation> expired = reservationRepository.findExpiredReservations(cutoff);
        int count = 0;
        for (StockReservation res : expired) {
            skuRepository.releaseStockAtomic(res.getSkuId(), res.getQty());
            res.setStatus("RELEASED");
            reservationRepository.save(res);
            count++;
        }
        return count;
    }
}
