package com.hdfc.ledger.drift.dto;

import java.math.BigDecimal;

public class CorrectionEvent {
    private String eventId;
    private String accountId;
    private String type;
    private BigDecimal amount;
    private Long timestamp;

    public CorrectionEvent() {}

    public CorrectionEvent(String eventId, String accountId, String type, BigDecimal amount) {
        this.eventId = eventId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
