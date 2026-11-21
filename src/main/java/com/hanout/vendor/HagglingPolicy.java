package com.hanout.vendor;

import com.hanout.common.Money;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "haggling_policies")
public class HagglingPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long vendorId;

    private Long productId; // optional, null = vendor default policy

    @Column(nullable = false)
    private BigDecimal autoAcceptPct = BigDecimal.valueOf(95.0);

    @Column(nullable = false)
    private BigDecimal autoRejectPct = BigDecimal.valueOf(70.0);

    @Column(nullable = false)
    private int maxRounds = 3;

    @Column(nullable = false)
    private int offerTtlHours = 24;

    @Column(nullable = false)
    private int priceLockMinutes = 30;

    @Column(nullable = false)
    private Money floorMillimes = Money.ZERO;

    public HagglingPolicy() {}

    public HagglingPolicy(Long vendorId, Long productId, BigDecimal autoAcceptPct, BigDecimal autoRejectPct,
                          int maxRounds, int offerTtlHours, int priceLockMinutes, Money floorMillimes) {
        this.vendorId = vendorId;
        this.productId = productId;
        this.autoAcceptPct = autoAcceptPct;
        this.autoRejectPct = autoRejectPct;
        this.maxRounds = maxRounds;
        this.offerTtlHours = offerTtlHours;
        this.priceLockMinutes = priceLockMinutes;
        this.floorMillimes = floorMillimes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public BigDecimal getAutoAcceptPct() { return autoAcceptPct; }
    public void setAutoAcceptPct(BigDecimal autoAcceptPct) { this.autoAcceptPct = autoAcceptPct; }

    public BigDecimal getAutoRejectPct() { return autoRejectPct; }
    public void setAutoRejectPct(BigDecimal autoRejectPct) { this.autoRejectPct = autoRejectPct; }

    public int getMaxRounds() { return maxRounds; }
    public void setMaxRounds(int maxRounds) { this.maxRounds = maxRounds; }

    public int getOfferTtlHours() { return offerTtlHours; }
    public void setOfferTtlHours(int offerTtlHours) { this.offerTtlHours = offerTtlHours; }

    public int getPriceLockMinutes() { return priceLockMinutes; }
    public void setPriceLockMinutes(int priceLockMinutes) { this.priceLockMinutes = priceLockMinutes; }

    public Money getFloorMillimes() { return floorMillimes; }
    public void setFloorMillimes(Money floorMillimes) { this.floorMillimes = floorMillimes; }
}
