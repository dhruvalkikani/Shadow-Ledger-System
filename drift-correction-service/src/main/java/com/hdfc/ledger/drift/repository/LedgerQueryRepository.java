package com.hdfc.ledger.drift.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public class LedgerQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public LedgerQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<BigDecimal> getShadowBalance(String accountId) {
        String sql = """
            SELECT COALESCE(SUM(
                CASE 
                    WHEN type = 'CREDIT' THEN amount
                    WHEN type = 'DEBIT' THEN -amount
                    ELSE 0
                END
            ), 0) as balance
            FROM ledger_entries
            WHERE account_id = ?
            """;

        try {
            BigDecimal balance = jdbcTemplate.queryForObject(sql, BigDecimal.class, accountId);
            return Optional.ofNullable(balance);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
