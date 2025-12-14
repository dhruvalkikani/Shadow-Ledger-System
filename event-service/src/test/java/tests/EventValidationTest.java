package tests;

import com.hdfc.ledger.event.dto.EventRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class EventValidationTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    public void testValidEvent() {
        EventRequest event = new EventRequest();
        event.setEventId("E1001");
        event.setAccountId("A10");
        event.setType("credit");
        event.setAmount(new BigDecimal("500"));
        event.setTimestamp(System.currentTimeMillis());

        Set<ConstraintViolation<EventRequest>> violations = validator.validate(event);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testInvalidEventMissingEventId() {
        EventRequest event = new EventRequest();
        event.setAccountId("A10");
        event.setType("credit");
        event.setAmount(new BigDecimal("500"));
        event.setTimestamp(System.currentTimeMillis());

        Set<ConstraintViolation<EventRequest>> violations = validator.validate(event);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testInvalidEventNegativeAmount() {
        EventRequest event = new EventRequest();
        event.setEventId("E1001");
        event.setAccountId("A10");
        event.setType("credit");
        event.setAmount(new BigDecimal("-100"));
        event.setTimestamp(System.currentTimeMillis());

        Set<ConstraintViolation<EventRequest>> violations = validator.validate(event);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testInvalidEventType() {
        EventRequest event = new EventRequest();
        event.setEventId("E1001");
        event.setAccountId("A10");
        event.setType("invalid_type");
        event.setAmount(new BigDecimal("500"));
        event.setTimestamp(System.currentTimeMillis());

        Set<ConstraintViolation<EventRequest>> violations = validator.validate(event);
        assertFalse(violations.isEmpty());
    }
}
