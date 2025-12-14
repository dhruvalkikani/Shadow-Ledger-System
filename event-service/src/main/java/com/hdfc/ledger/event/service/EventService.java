package com.hdfc.ledger.event.service;

import com.hdfc.ledger.event.dto.EventRequest;
import com.hdfc.ledger.event.entity.Event;
import com.hdfc.ledger.event.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);
    private static final String TOPIC = "transactions.raw";

    private final EventRepository eventRepository;
    private final KafkaTemplate<String, EventRequest> kafkaTemplate;

    public EventService(EventRepository eventRepository,
                        KafkaTemplate<String, EventRequest> kafkaTemplate) {
        this.eventRepository = eventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void processEvent(EventRequest request) {
        String traceId = MDC.get("traceId");

        // Check for duplicate eventId
        if (eventRepository.existsByEventId(request.getEventId())) {
            logger.warn("Duplicate event ID: {} - TraceId: {}", request.getEventId(), traceId);
            throw new IllegalArgumentException("Event ID already exists");
        }

        // Save to database
        Event event = new Event();
        event.setEventId(request.getEventId());
        event.setAccountId(request.getAccountId());
        event.setType(Event.EventType.valueOf(request.getType().toUpperCase()));
        event.setAmount(request.getAmount());
        event.setTimestamp(request.getTimestamp());

        eventRepository.save(event);
        logger.info("Event saved to database: {} - TraceId: {}", request.getEventId(), traceId);

        // Publish to Kafka
        kafkaTemplate.send(TOPIC, request.getAccountId(), request);
        logger.info("Event published to Kafka: {} - TraceId: {}", request.getEventId(), traceId);
    }
}
