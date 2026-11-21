package com.hanout.ordering;

import com.hanout.common.Money;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private Money totalAmount;

    @Column(nullable = false)
    private Money codAmount = Money.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PLACED;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(nullable = false, length = 32)
    private String phone;

    private Long governorateId;

    @Column(unique = true, length = 128)
    private String idempotencyKey;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Order() {}

    public Order(Long buyerId, Money totalAmount, Money codAmount, String shippingAddress, String phone, Long governorateId, String idempotencyKey) {
        this.buyerId = buyerId;
        this.totalAmount = totalAmount;
        this.codAmount = codAmount;
        this.shippingAddress = shippingAddress;
        this.phone = phone;
        this.governorateId = governorateId;
        this.idempotencyKey = idempotencyKey;
        this.status = OrderStatus.PLACED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }

    public Money getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Money totalAmount) { this.totalAmount = totalAmount; }

    public Money getCodAmount() { return codAmount; }
    public void setCodAmount(Money codAmount) { this.codAmount = codAmount; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Long getGovernorateId() { return governorateId; }
    public void setGovernorateId(Long governorateId) { this.governorateId = governorateId; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
