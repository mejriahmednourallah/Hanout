package com.hanout.catalog;

import com.hanout.common.Money;
import jakarta.persistence.*;

@Entity
@Table(name = "skus")
public class Sku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, unique = true)
    private String skuCode;

    @Column(name = "price_millimes", nullable = false)
    private Money priceMillimes;

    @Column(nullable = false)
    private int stockOnHand = 0;

    @Column(nullable = false)
    private int reserved = 0;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    public Sku() {}

    public Sku(Long id, Long productId, String skuCode, Money priceMillimes, int stockOnHand, int reserved) {
        this.id = id;
        this.productId = productId;
        this.skuCode = skuCode;
        this.priceMillimes = priceMillimes;
        this.stockOnHand = stockOnHand;
        this.reserved = reserved;
    }

    public int getAvailableStock() {
        return Math.max(0, stockOnHand - reserved);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }

    public Money getPriceMillimes() { return priceMillimes; }
    public void setPriceMillimes(Money priceMillimes) { this.priceMillimes = priceMillimes; }

    public int getStockOnHand() { return stockOnHand; }
    public void setStockOnHand(int stockOnHand) { this.stockOnHand = stockOnHand; }

    public int getReserved() { return reserved; }
    public void setReserved(int reserved) { this.reserved = reserved; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
