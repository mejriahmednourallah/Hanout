package com.hanout.payout;

import com.hanout.common.Money;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private LedgerAccount account;

    @Column(nullable = false)
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private LedgerDirection direction;

    @Column(nullable = false, length = 64)
    private String refType;

    @Column(nullable = false, length = 64)
    private String refId;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public LedgerEntry() {}

    public LedgerEntry(LedgerAccount account, Money amount, LedgerDirection direction, String refType, String refId) {
        this.account = account;
        this.amount = amount;
        this.direction = direction;
        this.refType = refType;
        this.refId = refId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LedgerAccount getAccount() { return account; }
    public void setAccount(LedgerAccount account) { this.account = account; }

    public Money getAmount() { return amount; }
    public void setAmount(Money amount) { this.amount = amount; }

    public LedgerDirection getDirection() { return direction; }
    public void setDirection(LedgerDirection direction) { this.direction = direction; }

    public String getRefType() { return refType; }
    public void setRefType(String refType) { this.refType = refType; }

    public String getRefId() { return refId; }
    public void setRefId(String refId) { this.refId = refId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
