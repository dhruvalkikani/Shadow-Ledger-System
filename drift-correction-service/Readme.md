# Drift & Correction Service

## Purpose
Compares CBS balances with shadow ledger, detects drift, generates correction events.

## Technology Stack
- Spring Boot 3.2.0
- Spring Kafka Producer
- PostgreSQL (read-only)
- Java 21

## Key Features
- Compares CBS reported balances vs shadow ledger balances
- Detects drift types: missing credit, incorrect debit, unknown
- Auto-generates correction events for simple mismatches
- Publishes corrections to `transactions.corrections` Kafka topic
- Manual correction endpoint for complex cases

## Ports
- **8083** (HTTP)

## Endpoints
