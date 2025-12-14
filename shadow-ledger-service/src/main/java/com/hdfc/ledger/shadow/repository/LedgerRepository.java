package com.hdfc.ledger.shadow.repository;

import com.hdfc.ledger.shadow.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {

    boolean existsByEventId(String eventId);

    @Query(value = """
    WITH balance_calc AS (
        SELECT 
            :accountId as account_id,
            COALESCE(SUM(
                CASE
                    WHEN type = 'CREDIT' THEN amount
                    WHEN type = 'DEBIT' THEN -amount
                    ELSE 0
                END
            ), 0) as balance,
            MAX(event_id) as last_event
        FROM ledger_entries
        WHERE account_id = :accountId
    )
    SELECT 
        COALESCE(account_id, :accountId) as account_id,
        COALESCE(balance, 0) as balance,
        last_event
    FROM balance_calc
    """, nativeQuery = true)
    Optional<Object[]> calculateShadowBalance(@Param("accountId") String accountId);

    @Query(value = """
        SELECT
            event_id,
            account_id,
            type,
            amount,
            event_timestamp,
            SUM(
                CASE
                    WHEN type = 'CREDIT' THEN amount
                    WHEN type = 'DEBIT' THEN -amount
                    ELSE 0
                END
            ) OVER (
                PARTITION BY account_id
                ORDER BY event_timestamp, event_id
                ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
            ) as running_balance
        FROM ledger_entries
        WHERE account_id = :accountId
        ORDER BY event_timestamp, event_id
        """, nativeQuery = true)
    java.util.List<Object[]> getRunningBalance(@Param("accountId") String accountId);
}
