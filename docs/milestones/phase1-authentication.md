# Phase 1 — Authentication Foundation

## Overview

Phase 1 establishes the production-ready authentication base for LifeOS. It adds Google OAuth login, normalized persistence for Google accounts and tokens, internal JWT issuance, JWT-protected APIs, automatic Google access-token refresh, PostgreSQL/Flyway schema management, and the tests needed to keep the flow stable.

## Authentication Architecture

Browser

↓

Google OAuth Login

↓

OAuthSuccessHandler

↓

Persist User

↓

Persist Google Tokens

↓

Issue LifeOS JWT

↓

Protected APIs

↓

Automatic Google Token Refresh

## Components Added

- `authentication/filter/JwtAuthenticationFilter`
- `authentication/handler/RestAuthenticationEntryPoint`
- `authentication/handler/RestAccessDeniedHandler`
- `authentication/oauth/OAuthSuccessHandler`
- `authentication/service/AuthenticationService`
- `authentication/service/JwtService`
- `authentication/service/OAuthAccountService`
- `authentication/service/OAuthTokenService`
- `authentication/service/TokenRefreshService`
- `authentication/entity/OAuthAccount`
- `authentication/entity/OAuthToken`
- `authentication/repository/OAuthAccountRepository`
- `authentication/repository/OAuthTokenRepository`
- `config/GlobalExceptionHandler`
- `controller/AuthController`
- `controller/ServiceController`
- `service/GmailService`
- `integrations/google/gmail/GoogleGmailClient`
- `integrations/google/gmail/GoogleGmailClientImpl`

## Database Changes

- `users` — primary application user table.
- `oauth_accounts` — maps a user to a provider identity such as Google and stores the provider account id.
- `oauth_tokens` — stores Google access token, refresh token, expiry, token type, and scopes for an OAuth account.

## Security

- **OAuth:** Google login is handled by Spring OAuth2 and completed in `OAuthSuccessHandler`.
- **JWT:** LifeOS issues its own JWT after OAuth login; the JWT is what clients use for API calls.
- **Token persistence:** Google tokens are stored server-side only and never returned in REST responses.
- **Protected APIs:** `SecurityConfig` requires JWT authentication for all non-public endpoints.
- **Token refresh:** `TokenRefreshService` refreshes expired Google access tokens transparently before Gmail calls.

## Environment Variables

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `JWT_SECRET`
- `JWT_EXPIRATION_MS`
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `GOOGLE_GMAIL_BASE_URL`
- `GOOGLE_GMAIL_USER_ID`
- `GOOGLE_GMAIL_MAX_RESULTS`
- `LLM_DEFAULT_PROVIDER`
- `GEMINI_API_KEY`
- `GEMINI_MODEL`
- `GEMINI_BASE_URL`

## Local Setup

1. Copy `.env.example` to `.env`.
2. Start PostgreSQL with Docker.
3. Run `./mvnw test` to validate the schema and tests.
4. Run `./mvnw spring-boot:run` to start the application.
5. Open `GET /oauth2/authorization/google` to begin login.

## API Reference

| Method | URL | Purpose | Auth | Request | Response |
|---|---|---|---|---|---|
| GET | `/` | Health-style home response | No | None | Plain text |
| GET | `/api/health` | Liveness check | No | None | JSON status |
| GET | `/oauth2/authorization/google` | Start Google OAuth login | No | None | Redirect |
| GET | `/user` | Return current authenticated user | Yes | None | `AuthenticatedUser` |
| GET | `/api/test` | Protected sample endpoint | Yes | None | Plain text |
| GET | `/gmail/messages` | Read Gmail messages | Yes | None | `GmailMessageDto[]` |
| GET | `/llm/test` | Smoke-test the LLM layer | Yes | Optional `prompt` query param | `LlmResponse` |
| POST | `/llm/generate` | Generate an LLM response | Yes | `LlmGenerateRequest` | `LlmResponse` |

## Testing Guide

See `docs/development/phase1-api-testing.md` for the full browser flow, Postman setup, cURL examples, and recommended validation order.

## Known Limitations

- Phase 1 only covers Google OAuth, JWT auth, and Gmail access.
- Calendar, Drive, AI extraction, core domain objects, and dashboard APIs are reserved for later phases.
- Automatic Google token refresh is implemented only for the Gmail path today.
