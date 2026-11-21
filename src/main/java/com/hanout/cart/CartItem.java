package com.hanout.cart;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private Long skuId;

    @Column(nullable = false)
    private int qty = 1;

    private Long offerId; // optional locked-price reference

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public CartItem() {}

    public CartItem(Long buyerId, Long skuId, int qty, Long offerId) {
        this.buyerId = buyerId;
        this.skuId = skuId;
        this.qty = qty;
        this.offerId = offerId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }

    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public Long getOfferId() { return offerId; }
    public void setOfferId(Long offerId) { this.offerId = offerId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
