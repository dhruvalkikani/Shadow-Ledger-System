package com.hdfc.ledger.shadow.repository;

import com.hdfc.ledger.shadow.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {

    boolean existsByEventId(String eventId);

    List<LedgerEntry> findByAccountId(String accountId);

    @Query(value = """
        SELECT COALESCE(SUM(
            CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END
        ), 0)
        FROM ledger_entries
        WHERE account_id = :accountId
        """, nativeQuery = true)
    BigDecimal computeBalance(@Param("accountId") String accountId);

    @Query(value = """
        SELECT event_id FROM ledger_entries
        WHERE account_id = :accountId
        ORDER BY event_timestamp DESC, event_id DESC
        LIMIT 1
        """, nativeQuery = true)
    String lastEvent(@Param("accountId") String accountId);

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
    List<Object[]> getRunningBalance(@Param("accountId") String accountId);
}
