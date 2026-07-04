# Phase 2 API Testing Guide

Use this guide to validate the Google services integrations added in Phase 2.

## What Phase 2 covers

- Gmail
- Calendar
- Drive
- Google Tasks
- Google People / Contacts

All endpoints reuse the same Google OAuth login and the same persisted token refresh path from Phase 1.

## Prerequisites

- Java 21 JDK installed
- Docker and Docker Compose running
- PostgreSQL container started: `docker compose -f docker/compose.yml up -d`
- Gemini billing is not required for these endpoints, but `/llm/*` will fail if Gemini credits are depleted

## Quick Start

### 1. Start the application

```bash
./run.sh
```

The script:
- frees port 8080 if needed
- starts the Spring Boot app

If you need another port:

```bash
SERVER_PORT=9090 ./run.sh
```

### 2. Make sure Google consent includes the new scopes

Because Phase 2 adds new Google scopes, sign out and run Google OAuth again if you already logged in on Phase 1.

## Postman setup

### Import the collection

Import:

```text
docs/postman/FamilyOS_API.postman_collection.json
```

The collection includes these variables:
- `baseUrl`
- `token`

### Set variables

1. Open the collection variables.
2. Set `token` to the JWT returned from Google login.
3. Update `baseUrl` only if you are not using port 8080.

## Manual testing flow

### 1. Public endpoints

```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/
```

### 2. Google OAuth login

Open:

```text
http://localhost:8080/oauth2/authorization/google
```

After login, copy the `token` field from the JSON response:

```json
{
  "token": "...",
  "user_id": "...",
  "email": "...",
  "name": "..."
}
```

### 3. Protected API calls

Replace `<JWT>` with the token from login.

```bash
curl -H "Authorization: Bearer <JWT>" http://localhost:8080/user
curl -H "Authorization: Bearer <JWT>" http://localhost:8080/gmail/messages
curl -H "Authorization: Bearer <JWT>" http://localhost:8080/gmail/allowed-messages
curl -H "Authorization: Bearer <JWT>" http://localhost:8080/gmail/allowlist
curl -H "Authorization: Bearer <JWT>" http://localhost:8080/storage/status
curl -X POST -H "Authorization: Bearer <JWT>" http://localhost:8080/storage/bootstrap
curl -X POST -H "Authorization: Bearer <JWT>" http://localhost:8080/storage/save
curl -H "Authorization: Bearer <JWT>" http://localhost:8080/calendar/events
curl -H "Authorization: Bearer <JWT>" http://localhost:8080/drive/files
curl -H "Authorization: Bearer <JWT>" http://localhost:8080/google-tasks/items
curl -H "Authorization: Bearer <JWT>" http://localhost:8080/people/contacts
```

## Request reference table

| Endpoint | Method | Auth | Purpose |
|---|---|---|---|
| `/api/health` | GET | No | Health check |
| `/` | GET | No | Home page |
| `/oauth2/authorization/google` | GET | No | Start Google OAuth |
| `/user` | GET | Bearer JWT | Current authenticated user |
| `/gmail/messages` | GET | Bearer JWT | Gmail message summaries |
| `/gmail/allowed-messages` | GET | Bearer JWT | Gmail messages filtered by configured sender/subject allowlist |
| `/gmail/allowlist` | GET | Bearer JWT | Read the Gmail sender/subject allowlist stored in the database |
| `/storage/status` | GET | Bearer JWT | Storage initialization state and current user |
| `/storage/bootstrap` | POST | Bearer JWT | Bootstrap Drive JSON into PostgreSQL runtime data |
| `/storage/save` | POST | Bearer JWT | Export PostgreSQL runtime data to Drive JSON |
| `/calendar/events` | GET | Bearer JWT | Upcoming calendar events |
| `/drive/files` | GET | Bearer JWT | Recent Drive files |
| `/google-tasks/items` | GET | Bearer JWT | Google Tasks items |
| `/people/contacts` | GET | Bearer JWT | Google contacts |

## Expected responses

- Public endpoints return `200 OK`
- OAuth login returns the JWT payload
- Protected endpoints return `200 OK` with a valid JWT
- Missing or expired JWT returns `401 Unauthorized`
- Gmail, Calendar, Drive, Tasks, and People endpoints return Google-backed DTOs when the Google token is valid
- Gmail allowlist endpoint only returns messages matching configured senders and/or subjects
- Storage is explicit only; use `/storage/bootstrap` and `/storage/save` to synchronize Drive JSON with PostgreSQL
- First bootstrap creates the Drive folder structure and default JSON files automatically
- Drive storage root: `LifeOS/users/{userId}/`
- Drive JSON files include `manifest.json`, `profile.json`, `settings.json`, `configuration/email-rules.json`, `configuration/calendar-rules.json`, `configuration/prompts.json`, `knowledge/*.json`, and `integrations/*.json`
- The app stores only LifeOS-owned data; it does not persist raw Gmail or Calendar payloads
- Example manifest:

```json
{
  "schemaVersion": 1,
  "lifeOSVersion": "0.1.0",
  "lastSaved": "2026-07-03T22:00:00Z",
  "modules": ["profile", "settings", "configuration", "knowledge", "integrations"]
}
```

## Troubleshooting

### Port 8080 already in use

Use `./run.sh` or set a custom port:

```bash
SERVER_PORT=9090 ./run.sh
```

### Google API permission errors

If Gmail, Calendar, Drive, Tasks, or Contacts fail with 403/401 errors:

1. Re-run Google OAuth consent
2. Make sure the app has the required scopes
3. Confirm the Google API is enabled in your Google Cloud project

Required scopes now include:

- `https://www.googleapis.com/auth/gmail.readonly`
- `https://www.googleapis.com/auth/calendar.readonly`
- `https://www.googleapis.com/auth/drive.file`
- `https://www.googleapis.com/auth/tasks.readonly`
- `https://www.googleapis.com/auth/contacts.readonly`

### Token refresh issues

The app refreshes Google access tokens automatically. If a request still fails, confirm the refresh token is stored and the Google account is still connected.

## Files to know

- Postman collection: `docs/postman/FamilyOS_API.postman_collection.json`
- Startup script: `run.sh`
- Environment sample: `.env.example`
- App config: `src/main/resources/application.yml`
