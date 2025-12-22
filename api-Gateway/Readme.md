# API Gateway Service

## Purpose
Single entry point for all client requests with JWT authentication and RBAC enforcement.

## Technology Stack
- Spring Cloud Gateway
- Spring Security (JWT)
- Spring Boot 3.2.0
- Java 21

## Key Features
- JWT token validation on all requests
- Role-Based Access Control (RBAC):
    - `user` → `/events/**`
    - `auditor` → `/drift-check`
    - `admin` → `/correct/**`
- Adds `X-Trace-Id` header to all forwarded requests
- Routes requests to backend services

## Ports
- **8080** (HTTP)

## Endpoints
