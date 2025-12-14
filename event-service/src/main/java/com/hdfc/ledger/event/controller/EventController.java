package com.hdfc.ledger.event.controller;

import com.hdfc.ledger.event.dto.EventRequest;
import com.hdfc.ledger.event.service.EventService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/events")
public class EventController {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createEvent(
            @Valid @RequestBody EventRequest request,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId) {

        MDC.put("traceId", traceId != null ? traceId : "no-trace-id");

        try {
            eventService.processEvent(request);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("eventId", request.getEventId());
            response.put("message", "Event processed successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {} - TraceId: {}", e.getMessage(), traceId);
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Error processing event - TraceId: {}", traceId, e);
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } finally {
            MDC.clear();
        }
    }
}
