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
        // Use the simple computeBalance method - returns BigDecimal directly
        BigDecimal currentBalance = ledgerRepository.computeBalance(accountId);
        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }

        BigDecimal transactionAmount = newEntry.getType() == LedgerEntry.TransactionType.CREDIT
                ? newEntry.getAmount()
                : newEntry.getAmount().negate();

        return currentBalance.add(transactionAmount);
    }

    public Map<String, Object> getShadowBalance(String accountId) {
        Map<String, Object> response = new HashMap<>();

        // Use two simple queries instead of one complex query with Object[]
        BigDecimal balance = ledgerRepository.computeBalance(accountId);
        String lastEventId = ledgerRepository.lastEvent(accountId);

        response.put("accountId", accountId);
        response.put("balance", balance != null ? balance : BigDecimal.ZERO);
        response.put("lastEvent", lastEventId);

        logger.info("Shadow balance for account: {} = {}, lastEvent: {}",
                accountId, balance, lastEventId);

        return response;
    }
}
