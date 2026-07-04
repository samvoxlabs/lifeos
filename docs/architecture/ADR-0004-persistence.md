# ADR-0004 Persistence

## Status
Accepted

## Context
The app needs durable storage for users and backend-only OAuth tokens.

## Decision
Use Spring Data JPA with UUID primary keys and Flyway-managed schema changes. Keep Hibernate in `validate` mode.

This ADR covers PostgreSQL as the runtime database. Canonical Drive-backed storage is documented separately in ADR-0008.

## Consequences
- Schema changes are explicit and versioned
- The application fails fast if migrations drift
- Repositories stay thin and service logic owns persistence rules
