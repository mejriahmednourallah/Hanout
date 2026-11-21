package com.hanout.vendor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendors")
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private boolean vatRegistered;

    private String taxId;

    private Long governorateId;

    @Column(nullable = false)
    private String status = "ACTIVE";

    private LocalDateTime createdAt = LocalDateTime.now();

    public Vendor() {}

    public Vendor(Long id, Long userId, String displayName, boolean vatRegistered, String taxId, Long governorateId) {
        this.id = id;
        this.userId = userId;
        this.displayName = displayName;
        this.vatRegistered = vatRegistered;
        this.taxId = taxId;
        this.governorateId = governorateId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public boolean isVatRegistered() { return vatRegistered; }
    public void setVatRegistered(boolean vatRegistered) { this.vatRegistered = vatRegistered; }

    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }

    public Long getGovernorateId() { return governorateId; }
    public void setGovernorateId(Long governorateId) { this.governorateId = governorateId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
