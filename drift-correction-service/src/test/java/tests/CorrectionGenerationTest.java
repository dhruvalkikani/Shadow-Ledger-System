package tests;

import com.hdfc.ledger.drift.dto.CorrectionEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class CorrectionGenerationTest {

    @Test
    public void testCorrectionEventGeneration() {
        String eventId = "CORR-A10-1";
        String accountId = "A10";
        String type = "credit";
        BigDecimal amount = new BigDecimal("50");

        CorrectionEvent correction = new CorrectionEvent(eventId, accountId, type, amount);

        assertNotNull(correction);
        assertEquals(eventId, correction.getEventId());
        assertEquals(accountId, correction.getAccountId());
        assertEquals(type, correction.getType());
        assertEquals(amount, correction.getAmount());
        assertNotNull(correction.getTimestamp());
    }

    @Test
    public void testCorrectionForPositiveDrift() {
        BigDecimal cbsBalance = new BigDecimal("1000");
        BigDecimal shadowBalance = new BigDecimal("950");
        BigDecimal difference = cbsBalance.subtract(shadowBalance);

        assertTrue(difference.compareTo(BigDecimal.ZERO) > 0);

        CorrectionEvent correction = new CorrectionEvent(
                "CORR-A10-1",
                "A10",
                "credit",
                difference
        );

        assertEquals("credit", correction.getType());
        assertEquals(new BigDecimal("50"), correction.getAmount());
    }

    @Test
    public void testCorrectionForNegativeDrift() {
        BigDecimal cbsBalance = new BigDecimal("900");
        BigDecimal shadowBalance = new BigDecimal("950");
        BigDecimal difference = cbsBalance.subtract(shadowBalance);

        assertTrue(difference.compareTo(BigDecimal.ZERO) < 0);

        CorrectionEvent correction = new CorrectionEvent(
                "CORR-A10-2",
                "A10",
                "debit",
                difference.abs()
        );

        assertEquals("debit", correction.getType());
        assertEquals(new BigDecimal("50"), correction.getAmount());
    }
}
