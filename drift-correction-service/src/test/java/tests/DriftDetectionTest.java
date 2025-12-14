package tests;

import com.hdfc.ledger.drift.dto.CBSBalance;
import com.hdfc.ledger.drift.repository.LedgerQueryRepository;
import com.hdfc.ledger.drift.service.DriftDetectionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DriftDetectionTest {

    @Mock
    private LedgerQueryRepository ledgerQueryRepository;

    @Mock
    private KafkaTemplate kafkaTemplate;

    @InjectMocks
    private DriftDetectionService driftDetectionService;

    @Test
    public void testDriftDetection() {
        // Setup
        CBSBalance cbsBalance1 = new CBSBalance();
        cbsBalance1.setAccountId("A10");
        cbsBalance1.setReportedBalance(new BigDecimal("1000"));

        CBSBalance cbsBalance2 = new CBSBalance();
        cbsBalance2.setAccountId("A11");
        cbsBalance2.setReportedBalance(new BigDecimal("2000"));

        when(ledgerQueryRepository.getShadowBalance("A10"))
                .thenReturn(Optional.of(new BigDecimal("950")));
        when(ledgerQueryRepository.getShadowBalance("A11"))
                .thenReturn(Optional.of(new BigDecimal("2000")));

        // Execute
        List<CBSBalance> cbsBalances = Arrays.asList(cbsBalance1, cbsBalance2);
        Map<String, Object> result = driftDetectionService.detectDrift(cbsBalances);

        // Verify
        assertEquals(2, result.get("totalAccounts"));
        assertEquals(1, result.get("driftsDetected"));
        assertTrue((Integer) result.get("correctionsGenerated") >= 0);
    }

    @Test
    public void testNoDrift() {
        CBSBalance cbsBalance = new CBSBalance();
        cbsBalance.setAccountId("A10");
        cbsBalance.setReportedBalance(new BigDecimal("1000"));

        when(ledgerQueryRepository.getShadowBalance("A10"))
                .thenReturn(Optional.of(new BigDecimal("1000")));

        Map<String, Object> result = driftDetectionService.detectDrift(Arrays.asList(cbsBalance));

        assertEquals(1, result.get("totalAccounts"));
        assertEquals(0, result.get("driftsDetected"));
    }
}
