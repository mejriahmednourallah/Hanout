package com.hanout.ordering;

import com.hanout.common.Money;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_lines")
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long vendorOrderId;

    @Column(nullable = false)
    private Long skuId;

    private Long offerId; // optional reference to negotiated offer

    @Column(nullable = false)
    private int qty;

    @Column(name = "unit_price_millimes", nullable = false)
    private Money unitPriceMillimes; // SNAPSHOT price!

    @Column(nullable = false)
    private BigDecimal vatRate;

    @Column(name = "total_millimes", nullable = false)
    private Money totalMillimes;

    public OrderLine() {}

    public OrderLine(Long vendorOrderId, Long skuId, Long offerId, int qty, Money unitPriceMillimes, BigDecimal vatRate, Money totalMillimes) {
        this.vendorOrderId = vendorOrderId;
        this.skuId = skuId;
        this.offerId = offerId;
        this.qty = qty;
        this.unitPriceMillimes = unitPriceMillimes;
        this.vatRate = vatRate;
        this.totalMillimes = totalMillimes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVendorOrderId() { return vendorOrderId; }
    public void setVendorOrderId(Long vendorOrderId) { this.vendorOrderId = vendorOrderId; }

    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }

    public Long getOfferId() { return offerId; }
    public void setOfferId(Long offerId) { this.offerId = offerId; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public Money getUnitPriceMillimes() { return unitPriceMillimes; }
    public void setUnitPriceMillimes(Money unitPriceMillimes) { this.unitPriceMillimes = unitPriceMillimes; }

    public BigDecimal getVatRate() { return vatRate; }
    public void setVatRate(BigDecimal vatRate) { this.vatRate = vatRate; }

    public Money getTotalMillimes() { return totalMillimes; }
    public void setTotalMillimes(Money totalMillimes) { this.totalMillimes = totalMillimes; }
}
