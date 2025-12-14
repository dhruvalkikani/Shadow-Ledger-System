package com.hdfc.ledger.drift.dto;

import java.math.BigDecimal;

public class CBSBalance {
    private String accountId;
    private BigDecimal reportedBalance;

    // Getters and Setters
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getReportedBalance() {
        return reportedBalance;
    }

    public void setReportedBalance(BigDecimal reportedBalance) {
        this.reportedBalance = reportedBalance;
    }
}
