package com.hanout.risk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanout.common.Money;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class RiskScoringService {

    private final RiskAssessmentRepository assessmentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${hanout.risk.first-order-score:20}")
    private int firstOrderScore;

    @Value("${hanout.risk.high-value-threshold-millimes:300000}")
    private long highValueThresholdMillimes;

    @Value("${hanout.risk.high-value-score:30}")
    private int highValueScore;

    @Value("${hanout.risk.repeat-refusal-score:50}")
    private int repeatRefusalScore;

    @Value("${hanout.risk.high-frequency-score:35}")
    private int highFrequencyScore;

    @Value("${hanout.risk.allow-cutoff:25}")
    private int allowCutoff;

    @Value("${hanout.risk.require-otp-cutoff:55}")
    private int requireOtpCutoff;

    @Value("${hanout.risk.require-deposit-cutoff:80}")
    private int requireDepositCutoff;

    public RiskScoringService(RiskAssessmentRepository assessmentRepository) {
        this.assessmentRepository = assessmentRepository;
    }

    public record RiskSignals(
            String phone,
            Money orderAmount,
            Long governorateId,
            boolean isFirstOrder,
            int previousRefusalsCount,
            int recentOrdersCount
    ) {}

    public record RiskEvaluationResult(
            int score,
            RiskDecision decision,
            Map<String, Object> featureVector
    ) {}

    public RiskEvaluationResult evaluate(RiskSignals signals) {
        int score = 0;
        Map<String, Object> features = new HashMap<>();

        features.put("phone", signals.phone());
        features.put("amountMillimes", signals.orderAmount().millimes());
        features.put("governorateId", signals.governorateId());
        features.put("isFirstOrder", signals.isFirstOrder());
        features.put("previousRefusals", signals.previousRefusalsCount());
        features.put("recentOrdersCount", signals.recentOrdersCount());

        if (signals.isFirstOrder()) {
            score += firstOrderScore;
        }

        if (signals.orderAmount().millimes() > highValueThresholdMillimes) {
            score += highValueScore;
        }

        if (signals.previousRefusalsCount() > 0) {
            score += repeatRefusalScore * signals.previousRefusalsCount();
        }

        if (signals.recentOrdersCount() >= 3) {
            score += highFrequencyScore;
        }

        // Selected governorates with elevated delivery risk in remote zones
        if (signals.governorateId() != null && signals.governorateId() >= 6) {
            score += 15;
            features.put("remoteRegionRisk", true);
        }

        RiskDecision decision;
        if (score <= allowCutoff) {
            decision = RiskDecision.ALLOW;
        } else if (score <= requireOtpCutoff) {
            decision = RiskDecision.REQUIRE_OTP;
        } else if (score <= requireDepositCutoff) {
            decision = RiskDecision.REQUIRE_DEPOSIT;
        } else {
            decision = RiskDecision.BLOCK;
        }

        features.put("finalScore", score);
        features.put("decision", decision.name());

        return new RiskEvaluationResult(score, decision, features);
    }

    @Transactional
    public RiskAssessment recordAssessment(Long orderId, RiskSignals signals) {
        RiskEvaluationResult eval = evaluate(signals);
        String signalsJson;
        try {
            signalsJson = objectMapper.writeValueAsString(eval.featureVector());
        } catch (JsonProcessingException e) {
            signalsJson = "{}";
        }

        RiskAssessment assessment = new RiskAssessment(orderId, eval.score(), signalsJson, eval.decision());
        return assessmentRepository.save(assessment);
    }
}
