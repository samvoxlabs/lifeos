# Local Setup

## Prerequisites

- JDK 21
- Maven wrapper
- Docker and Docker Compose

## Environment

Copy the root example file:

```bash
cp .env.example .env
```

Required values:

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `JWT_SECRET`
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`

Optional values:

- `JWT_EXPIRATION_MS`
- `GOOGLE_GMAIL_BASE_URL`
- `GOOGLE_GMAIL_USER_ID`
- `GOOGLE_GMAIL_MAX_RESULTS`
- `LLM_DEFAULT_PROVIDER`
- `GEMINI_API_KEY`
- `GEMINI_MODEL`
- `GEMINI_BASE_URL`

## Run locally

```bash
docker compose -f docker/compose.yml up -d
./mvnw test
./mvnw spring-boot:run
```

## Verify the app

- `GET /api/health` should return `UP`
- `GET /oauth2/authorization/google` should start the login flow
- `GET /api/test` should require a JWT

## Notes

- Secrets live only in `.env` or the environment.
- Flyway runs automatically on startup.
- Google tokens stay on the backend.
