package com.hdfc.ledger.shadow.controller;

import com.hdfc.ledger.shadow.service.LedgerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/accounts")
public class LedgerController {

    private static final Logger logger = LoggerFactory.getLogger(LedgerController.class);
    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/{accountId}/shadow-balance")
    public ResponseEntity<Map<String, Object>> getShadowBalance(
            @PathVariable String accountId,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId) {

        MDC.put("traceId", traceId != null ? traceId : "no-trace-id");
        logger.info("Fetching shadow balance for account: {} - TraceId: {}", accountId, traceId);

        try {
            Map<String, Object> balance = ledgerService.getShadowBalance(accountId);
            return ResponseEntity.ok(balance);
        } finally {
            MDC.clear();
        }
    }
}
