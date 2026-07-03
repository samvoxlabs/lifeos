# ADR-0005 Gmail Integration

## Status
Accepted

## Context
Gmail access should not leak Google SDK details into controllers or business services.

## Decision
Place Gmail API code in `integrations/google/gmail`. The service layer resolves the user and token, then calls a provider-specific client.

## Consequences
- Gmail code stays isolated and replaceable
- Business services return DTOs only
- Future providers can reuse the same pattern
