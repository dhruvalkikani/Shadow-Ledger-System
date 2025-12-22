# Shadow Ledger Service

## Purpose
Consumes events from Kafka, maintains immutable ledger, computes running balances using SQL window functions.

## Technology Stack
- Spring Boot 3.2.0
- Spring Kafka Consumer
- PostgreSQL
- Java 21

## Key Features
- Consumes from `transactions.raw` and `transactions.corrections`
- Deduplicates using eventId
- Orders events by: 1) timestamp, 2) eventId
- Append-only ledger table in PostgreSQL
- **SQL Window Function** for running balance calculation
- Prevents negative balances
- Exposes shadow balance API

## Ports
- **8082** (HTTP)

## Endpoints
