package com.hdfc.ledger.event.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class EventRequest {

    @NotBlank(message = "Event ID is required")
    private String eventId;

    @NotBlank(message = "Account ID is required")
    private String accountId;

    @NotNull(message = "Type is required")
    @Pattern(regexp = "credit|debit", message = "Type must be 'credit' or 'debit'")
    private String type;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Timestamp is required")
    private Long timestamp;

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
