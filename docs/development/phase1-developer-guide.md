# Phase 1 Developer Guide

## What Phase 1 delivered

Phase 1 completes the authentication foundation for LifeOS:

- Google OAuth login
- normalized Google account/token persistence
- LifeOS JWT issuance
- JWT validation for protected APIs
- automatic Google token refresh
- PostgreSQL + Flyway schema management
- test coverage for the auth path

## End-to-end flow

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

## API reference

| Method | URL | Purpose | Auth | Request | Response |
|---|---|---|---|---|---|
| GET | `/` | App landing response | No | None | Plain text |
| GET | `/api/health` | Liveness check | No | None | JSON status |
| GET | `/oauth2/authorization/google` | Start Google OAuth login | No | None | Redirect |
| GET | `/user` | Current authenticated user | Yes | None | `AuthenticatedUser` |
| GET | `/api/test` | Protected sample endpoint | Yes | None | Plain text |
| GET | `/gmail/messages` | Read Gmail messages | Yes | None | `GmailMessageDto[]` |
| GET | `/llm/test` | Smoke-test the LLM layer | Yes | Optional `prompt` query param | `LlmResponse` |
| POST | `/llm/generate` | Generate an LLM response | Yes | `LlmGenerateRequest` | `LlmResponse` |

### Public endpoints

- `GET /`
- `GET /api/health`
- `GET /oauth2/authorization/google`
- `GET /login/**`
- `GET /error`

### Protected endpoints

- `GET /user`
- `GET /api/test`
- `GET /gmail/messages`
- `GET /llm/test`
- `POST /llm/generate`

### Example cURL

```bash
curl http://localhost:8080/api/health
curl -H "Authorization: Bearer $JWT" http://localhost:8080/api/test
curl -H "Authorization: Bearer $JWT" http://localhost:8080/gmail/messages
```

## Testing guide

### Browser flow

1. Open `/oauth2/authorization/google`.
2. Sign in with a test Google account.
3. Confirm a JWT is returned.
4. Use the JWT on protected endpoints.

### Postman

- See `docs/development/phase1-api-testing.md` for the full request list, cURL examples, and Postman setup.

### Recommended order

1. Follow `docs/development/phase1-api-testing.md`.
2. Review `docs/ROADMAP.md` for Phase 2 planning.

## Environment variables

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

## Local setup

1. Copy `.env.example` to `.env`.
2. Start PostgreSQL with Docker.
3. Run `./mvnw test`.
4. Run `./mvnw spring-boot:run`.
5. Open `/oauth2/authorization/google`.

## Notes

- Google tokens stay server-side only.
- JWTs are the only token returned to clients.
- Gmail and future Google APIs should reuse the token refresh service.
