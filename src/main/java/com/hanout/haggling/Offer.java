package com.hanout.haggling;

import com.hanout.common.Money;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "offers")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long skuId;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private Long vendorId;

    @Column(nullable = false)
    private int qty = 1;

    @Column(name = "list_price", nullable = false)
    private Money listPrice;

    @Column(name = "current_amount", nullable = false)
    private Money currentAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status = OfferStatus.PENDING;

    @Column(nullable = false)
    private int round = 1;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime lockExpiresAt;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Offer() {}

    public Offer(Long skuId, Long buyerId, Long vendorId, int qty, Money listPrice, Money currentAmount, LocalDateTime expiresAt) {
        this.skuId = skuId;
        this.buyerId = buyerId;
        this.vendorId = vendorId;
        this.qty = qty;
        this.listPrice = listPrice;
        this.currentAmount = currentAmount;
        this.expiresAt = expiresAt;
        this.status = OfferStatus.PENDING;
        this.round = 1;
    }

    public boolean isLockValid() {
        return status == OfferStatus.ACCEPTED && lockExpiresAt != null && lockExpiresAt.isAfter(LocalDateTime.now());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }

    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public Money getListPrice() { return listPrice; }
    public void setListPrice(Money listPrice) { this.listPrice = listPrice; }

    public Money getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(Money currentAmount) { this.currentAmount = currentAmount; }

    public OfferStatus getStatus() { return status; }
    public void setStatus(OfferStatus status) { this.status = status; }

    public int getRound() { return round; }
    public void setRound(int round) { this.round = round; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getLockExpiresAt() { return lockExpiresAt; }
    public void setLockExpiresAt(LocalDateTime lockExpiresAt) { this.lockExpiresAt = lockExpiresAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
