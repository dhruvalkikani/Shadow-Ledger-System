package com.hdfc.ledger.drift.service;

import com.hdfc.ledger.drift.dto.CBSBalance;
import com.hdfc.ledger.drift.dto.CorrectionEvent;
import com.hdfc.ledger.drift.repository.LedgerQueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class DriftDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(DriftDetectionService.class);
    private static final String CORRECTION_TOPIC = "transactions.corrections";

    private final LedgerQueryRepository ledgerQueryRepository;
    private final KafkaTemplate<String, CorrectionEvent> kafkaTemplate;
    private int correctionCounter = 1;

    public DriftDetectionService(LedgerQueryRepository ledgerQueryRepository,
                                 KafkaTemplate<String, CorrectionEvent> kafkaTemplate) {
        this.ledgerQueryRepository = ledgerQueryRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Map<String, Object> detectDrift(List<CBSBalance> cbsBalances) {
        List<Map<String, Object>> drifts = new ArrayList<>();
        int correctionsGenerated = 0;

        for (CBSBalance cbsBalance : cbsBalances) {
            String accountId = cbsBalance.getAccountId();
            BigDecimal reportedBalance = cbsBalance.getReportedBalance();

            Optional<BigDecimal> shadowBalanceOpt = ledgerQueryRepository.getShadowBalance(accountId);
            BigDecimal shadowBalance = shadowBalanceOpt.orElse(BigDecimal.ZERO);

            BigDecimal difference = reportedBalance.subtract(shadowBalance);

            if (difference.compareTo(BigDecimal.ZERO) != 0) {
                logger.warn("Drift detected for account {}: CBS={}, Shadow={}, Diff={}",
                        accountId, reportedBalance, shadowBalance, difference);

                Map<String, Object> drift = new HashMap<>();
                drift.put("accountId", accountId);
                drift.put("cbsBalance", reportedBalance);
                drift.put("shadowBalance", shadowBalance);
                drift.put("difference", difference);
                drift.put("status", "drift_detected");

                // Generate correction if possible
                if (canAutoCorrect(difference)) {
                    CorrectionEvent correction = generateCorrection(accountId, difference);
                    publishCorrection(correction);
                    drift.put("correctionEventId", correction.getEventId());
                    drift.put("correctionAction", "auto_corrected");
                    correctionsGenerated++;
                } else {
                    drift.put("correctionAction", "manual_review_required");
                }

                drifts.add(drift);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalAccounts", cbsBalances.size());
        result.put("driftsDetected", drifts.size());
        result.put("correctionsGenerated", correctionsGenerated);
        result.put("drifts", drifts);

        return result;
    }

    private boolean canAutoCorrect(BigDecimal difference) {
        // Auto-correct if difference is reasonable (not too large)
        return difference.abs().compareTo(new BigDecimal("10000")) <= 0;
    }

    private CorrectionEvent generateCorrection(String accountId, BigDecimal difference) {
        String eventId = "CORR-" + accountId + "-" + (correctionCounter++);
        String type = difference.compareTo(BigDecimal.ZERO) > 0 ? "credit" : "debit";
        BigDecimal amount = difference.abs();

        return new CorrectionEvent(eventId, accountId, type, amount);
    }

    private void publishCorrection(CorrectionEvent correction) {
        kafkaTemplate.send(CORRECTION_TOPIC, correction.getAccountId(), correction);
        logger.info("Published correction event: {} for account: {}",
                correction.getEventId(), correction.getAccountId());
    }

    public void manualCorrection(String accountId, String type, BigDecimal amount) {
        String eventId = "MANUAL-CORR-" + accountId + "-" + System.currentTimeMillis();
        CorrectionEvent correction = new CorrectionEvent(eventId, accountId, type, amount);
        publishCorrection(correction);
    }
}
