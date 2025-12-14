package com.hdfc.ledger.shadow.service;

import com.hdfc.ledger.shadow.dto.TransactionEvent;
import com.hdfc.ledger.shadow.entity.LedgerEntry;
import com.hdfc.ledger.shadow.repository.LedgerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class LedgerService {

    private static final Logger logger = LoggerFactory.getLogger(LedgerService.class);
    private final LedgerRepository ledgerRepository;

    public LedgerService(LedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    @KafkaListener(topics = "transactions.raw", groupId = "shadow-ledger-group")
    @Transactional
    public void consumeRawTransaction(TransactionEvent event, Acknowledgment ack) {
        processTransaction(event, false);
        ack.acknowledge();
    }

    @KafkaListener(topics = "transactions.corrections", groupId = "shadow-ledger-group")
    @Transactional
    public void consumeCorrection(TransactionEvent event, Acknowledgment ack) {
        processTransaction(event, true);
        ack.acknowledge();
    }

    private void processTransaction(TransactionEvent event, boolean isCorrection) {
        // Deduplicate using eventId
        if (ledgerRepository.existsByEventId(event.getEventId())) {
            logger.warn("Duplicate event ignored: {}", event.getEventId());
            return;
        }

        LedgerEntry entry = new LedgerEntry();
        entry.setEventId(event.getEventId());
        entry.setAccountId(event.getAccountId());
        entry.setType(LedgerEntry.TransactionType.valueOf(event.getType().toUpperCase()));
        entry.setAmount(event.getAmount());
        entry.setEventTimestamp(event.getTimestamp());
        entry.setCorrection(isCorrection);

        // Check for negative balance
        BigDecimal newBalance = calculateBalanceAfterTransaction(event.getAccountId(), entry);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            logger.error("Transaction would result in negative balance for account: {}", event.getAccountId());
            throw new IllegalStateException("Insufficient balance");
        }

        ledgerRepository.save(entry);
        logger.info("Ledger entry created: {} for account: {} (correction: {})",
                event.getEventId(), event.getAccountId(), isCorrection);
    }

    private BigDecimal calculateBalanceAfterTransaction(String accountId, LedgerEntry newEntry) {
        Optional<Object[]> result = ledgerRepository.calculateShadowBalance(accountId);

        BigDecimal currentBalance = BigDecimal.ZERO;
        if (result.isPresent()) {
            Object[] data = result.get();
            // Handle different array lengths defensively
            if (data.length >= 2 && data[1] != null) {
                currentBalance = (BigDecimal) data[1];
            } else if (data.length == 1 && data[0] instanceof BigDecimal) {
                currentBalance = (BigDecimal) data[0];
            }
        }

        BigDecimal transactionAmount = newEntry.getType() == LedgerEntry.TransactionType.CREDIT
                ? newEntry.getAmount()
                : newEntry.getAmount().negate();

        return currentBalance.add(transactionAmount);
    }


    public Map<String, Object> getShadowBalance(String accountId) {
        Optional<Object[]> result = ledgerRepository.calculateShadowBalance(accountId);
        Map<String, Object> response = new HashMap<>();

        if (result.isPresent()) {
            Object[] data = result.get();

            logger.info("Query returned {} columns for account: {}", data.length, accountId);

            if (data.length >= 3) {
                response.put("accountId", data[0] != null ? data[0] : accountId);
                response.put("balance", data[1] != null ? data[1] : BigDecimal.ZERO);
                response.put("lastEvent", data[2]);
            } else if (data.length == 2) {

                response.put("accountId", accountId);
                response.put("balance", data[1] != null ? data[1] : BigDecimal.ZERO);
                response.put("lastEvent", null);
            } else {

                logger.warn("Unexpected result structure for account: {}, columns: {}", accountId, data.length);
                response.put("accountId", accountId);
                response.put("balance", BigDecimal.ZERO);
                response.put("lastEvent", null);
            }
        } else {

            response.put("accountId", accountId);
            response.put("balance", BigDecimal.ZERO);
            response.put("lastEvent", null);
        }

        return response;
    }

}
