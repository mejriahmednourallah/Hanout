package com.hanout.delivery;

import com.hanout.common.Money;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long vendorOrderId;

    @Column(nullable = false, length = 32)
    private String courierCode;

    @Column(nullable = false, length = 128)
    private String trackingNo;

    @Column(nullable = false)
    private Money codAmount = Money.ZERO;

    @Column(nullable = false)
    private boolean codCollected = false;

    @Column(nullable = false, length = 32)
    private String status = "BOOKED";

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Shipment() {}

    public Shipment(Long vendorOrderId, String courierCode, String trackingNo, Money codAmount) {
        this.vendorOrderId = vendorOrderId;
        this.courierCode = courierCode;
        this.trackingNo = trackingNo;
        this.codAmount = codAmount;
        this.status = "BOOKED";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVendorOrderId() { return vendorOrderId; }
    public void setVendorOrderId(Long vendorOrderId) { this.vendorOrderId = vendorOrderId; }

    public String getCourierCode() { return courierCode; }
    public void setCourierCode(String courierCode) { this.courierCode = courierCode; }

    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }

    public Money getCodAmount() { return codAmount; }
    public void setCodAmount(Money codAmount) { this.codAmount = codAmount; }

    public boolean isCodCollected() { return codCollected; }
    public void setCodCollected(boolean codCollected) { this.codCollected = codCollected; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
