package com.hanout.risk;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_assessments")
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String signals; // JSON formatted feature vector

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskDecision decision;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public RiskAssessment() {}

    public RiskAssessment(Long orderId, int score, String signals, RiskDecision decision) {
        this.orderId = orderId;
        this.score = score;
        this.signals = signals;
        this.decision = decision;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getSignals() { return signals; }
    public void setSignals(String signals) { this.signals = signals; }

    public RiskDecision getDecision() { return decision; }
    public void setDecision(RiskDecision decision) { this.decision = decision; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
