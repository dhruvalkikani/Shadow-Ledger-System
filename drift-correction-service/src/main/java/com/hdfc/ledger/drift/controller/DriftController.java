package com.hdfc.ledger.drift.controller;

import com.hdfc.ledger.drift.dto.CBSBalance;
import com.hdfc.ledger.drift.service.DriftDetectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DriftController {

    private static final Logger logger = LoggerFactory.getLogger(DriftController.class);
    private final DriftDetectionService driftDetectionService;

    public DriftController(DriftDetectionService driftDetectionService) {
        this.driftDetectionService = driftDetectionService;
    }

    @PostMapping("/drift-check")
    public ResponseEntity<Map<String, Object>> checkDrift(
            @RequestBody List<CBSBalance> cbsBalances,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId) {

        MDC.put("traceId", traceId != null ? traceId : "no-trace-id");
        logger.info("Drift check requested for {} accounts - TraceId: {}", cbsBalances.size(), traceId);

        try {
            Map<String, Object> result = driftDetectionService.detectDrift(cbsBalances);
            return ResponseEntity.ok(result);
        } finally {
            MDC.clear();
        }
    }

    @PostMapping("/correct/{accountId}")
    public ResponseEntity<Map<String, String>> manualCorrection(
            @PathVariable String accountId,
            @RequestParam String type,
            @RequestParam BigDecimal amount,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId) {

        MDC.put("traceId", traceId != null ? traceId : "no-trace-id");
        logger.info("Manual correction for account: {} - TraceId: {}", accountId, traceId);

        try {
            driftDetectionService.manualCorrection(accountId, type, amount);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Correction event published");
            response.put("accountId", accountId);

            return ResponseEntity.ok(response);
        } finally {
            MDC.clear();
        }
    }
}
