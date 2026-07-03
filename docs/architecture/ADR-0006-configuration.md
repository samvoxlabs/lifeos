# ADR-0006 Configuration

## Status
Accepted

## Context
The project needs production-friendly, environment-driven configuration with minimal duplication.

## Decision
Keep runtime settings in `application.yml` and bind feature settings with `@ConfigurationProperties` under `com.familyos.familyos.config.properties`.

## Consequences
- No scattered `@Value` usage
- Config is type-safe and easier to validate
- Environment-specific values stay outside code
