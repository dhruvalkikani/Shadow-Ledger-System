# Event Service

## Purpose
Receives financial events, validates them, stores for traceability, and publishes to Kafka.

## Technology Stack
- Spring Boot 3.2.0
- Spring Kafka Producer
- PostgreSQL
- Java 21

## Key Features
- Validates eventId uniqueness (idempotency)
- Validates: eventId, accountId, amount > 0, type ∈ {credit, debit}
- Stores events in PostgreSQL for audit trail
- Publishes valid events to `transactions.raw` Kafka topic

## Ports
- **8081** (HTTP)

## Endpoints
