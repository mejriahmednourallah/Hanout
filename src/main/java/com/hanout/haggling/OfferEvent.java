package com.hanout.haggling;

import com.hanout.common.Money;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "offer_events")
public class OfferEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long offerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferActor actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferEventType type;

    @Column(nullable = false)
    private Money amount;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public OfferEvent() {}

    public OfferEvent(Long offerId, OfferActor actor, OfferEventType type, Money amount) {
        this.offerId = offerId;
        this.actor = actor;
        this.type = type;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOfferId() { return offerId; }
    public void setOfferId(Long offerId) { this.offerId = offerId; }

    public OfferActor getActor() { return actor; }
    public void setActor(OfferActor actor) { this.actor = actor; }

    public OfferEventType getType() { return type; }
    public void setType(OfferEventType type) { this.type = type; }

    public Money getAmount() { return amount; }
    public void setAmount(Money amount) { this.amount = amount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
