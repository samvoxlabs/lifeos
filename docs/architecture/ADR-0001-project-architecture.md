# ADR-0001 Project Architecture

## Status
Accepted

## Context
LifeOS is a Spring Boot backend that combines OAuth login, internal JWT auth, PostgreSQL persistence, and provider-specific integrations.

## Decision
Use a layered architecture:
- `controller` for HTTP boundaries
- `service` for business rules
- `authentication` for auth-specific flow
- `integrations` for external APIs
- `repository` and `entity` for persistence
- `dto` for API payloads
- `config` for runtime configuration binding

## Consequences
- Clear dependency direction and easier testing
- External APIs stay isolated from business code
- New providers can be added without changing core services
