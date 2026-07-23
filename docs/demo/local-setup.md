# LifeOS Local Setup

Use this guide to run LifeOS locally for demo work.

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
./start-app.sh
```

## Verify the app

- `GET /api/health` should return `UP`
- `GET /oauth2/authorization/google` should start the login flow
- `GET /api/test` should require a JWT
- `GET /auth/google/scopes` shows the connected Google scopes, client id, and project metadata

## Startup scripts

- `./start-app.sh` is the preferred launcher. It creates `.env` from `.env.example` if needed, then starts the app.
- `./run.sh` is the lower-level startup script that starts Docker, frees port 8080, and launches Spring Boot.

## Notes

- Secrets live only in `.env` or the environment.
- Flyway runs automatically on startup.
- Google tokens stay on the backend.
- Gmail reset now deletes actual Gmail inbox messages, so re-consent is required if your token predates the `gmail.modify` scope.
- Calendar publish now needs `https://www.googleapis.com/auth/calendar.events`, so re-consent is required if your token predates that scope.
- `prompt=consent` is forced on Google login so re-consent is easier when scopes change.
