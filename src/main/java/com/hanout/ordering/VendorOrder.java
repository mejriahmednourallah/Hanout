package com.hanout.ordering;

import com.hanout.common.Money;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_orders")
public class VendorOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long vendorId;

    @Column(nullable = false)
    private Money subTotal;

    @Column(nullable = false)
    private Money commissionAmount = Money.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorOrderStatus status = VendorOrderStatus.PLACED;

    private String courierId;

    private String trackingNo;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public VendorOrder() {}

    public VendorOrder(Long orderId, Long vendorId, Money subTotal, Money commissionAmount) {
        this.orderId = orderId;
        this.vendorId = vendorId;
        this.subTotal = subTotal;
        this.commissionAmount = commissionAmount;
        this.status = VendorOrderStatus.PLACED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public Money getSubTotal() { return subTotal; }
    public void setSubTotal(Money subTotal) { this.subTotal = subTotal; }

    public Money getCommissionAmount() { return commissionAmount; }
    public void setCommissionAmount(Money commissionAmount) { this.commissionAmount = commissionAmount; }

    public VendorOrderStatus getStatus() { return status; }
    public void setStatus(VendorOrderStatus status) { this.status = status; }

    public String getCourierId() { return courierId; }
    public void setCourierId(String courierId) { this.courierId = courierId; }

    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
