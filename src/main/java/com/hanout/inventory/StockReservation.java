package com.hanout.inventory;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_reservations")
public class StockReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long skuId;

    @Column(nullable = false)
    private int qty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationReason reason;

    @Column(nullable = false)
    private String refId;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, RELEASED, COMMITTED

    private LocalDateTime createdAt = LocalDateTime.now();

    public StockReservation() {}

    public StockReservation(Long skuId, int qty, ReservationReason reason, String refId, LocalDateTime expiresAt) {
        this.skuId = skuId;
        this.qty = qty;
        this.reason = reason;
        this.refId = refId;
        this.expiresAt = expiresAt;
        this.status = "ACTIVE";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public ReservationReason getReason() { return reason; }
    public void setReason(ReservationReason reason) { this.reason = reason; }

    public String getRefId() { return refId; }
    public void setRefId(String refId) { this.refId = refId; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
