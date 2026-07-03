# ADR-0003 Internal JWT

## Status
Accepted

## Context
The frontend should authenticate to LifeOS without needing Google credentials on every request.

## Decision
Issue a signed HS256 JWT after successful OAuth login. Keep JWT generation and validation in `JwtService`, independent of Spring Security and persistence details.

## Consequences
- API requests can use a single LifeOS bearer token
- Authentication logic is reusable and testable in isolation
- Secret and expiration stay configuration-driven
