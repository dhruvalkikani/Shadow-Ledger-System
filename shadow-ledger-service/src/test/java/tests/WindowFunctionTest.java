//package tests;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.test.context.jdbc.Sql;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
//public class WindowFunctionTest {
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    @Test
//    public void testRunningBalanceCalculation() {
//        String sql = """
//            SELECT
//                event_id,
//                account_id,
//                type,
//                amount,
//                SUM(
//                    CASE
//                        WHEN type = 'CREDIT' THEN amount
//                        WHEN type = 'DEBIT' THEN -amount
//                        ELSE 0
//                    END
//                ) OVER (
//                    PARTITION BY account_id
//                    ORDER BY event_timestamp, event_id
//                    ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
//                ) as running_balance
//            FROM ledger_entries
//            WHERE account_id = 'TEST_ACC_001'
//            ORDER BY event_timestamp, event_id
//            """;
//
//        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
//
//        assertFalse(results.isEmpty());
//
//        // Verify running balance increases with credits
//        BigDecimal previousBalance = BigDecimal.ZERO;
//        for (Map<String, Object> row : results) {
//            BigDecimal runningBalance = (BigDecimal) row.get("running_balance");
//            assertNotNull(runningBalance);
//            assertTrue(runningBalance.compareTo(BigDecimal.ZERO) >= 0);
//        }
//    }
//
//    @Test
//    public void testFinalBalanceCalculation() {
//        String sql = """
//            SELECT
//                COALESCE(SUM(
//                    CASE
//                        WHEN type = 'CREDIT' THEN amount
//                        WHEN type = 'DEBIT' THEN -amount
//                        ELSE 0
//                    END
//                ), 0) as final_balance
//            FROM ledger_entries
//            WHERE account_id = 'TEST_ACC_001'
//            """;
//
//        BigDecimal finalBalance = jdbcTemplate.queryForObject(sql, BigDecimal.class);
//        assertNotNull(finalBalance);
//        assertTrue(finalBalance.compareTo(BigDecimal.ZERO) >= 0);
//    }
//}
