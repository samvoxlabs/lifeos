# Phase 1 API Testing Guide

Use this guide to validate the Phase 1 authentication flow from a clean checkout.

## Prerequisites

- Java 21 JDK installed
- Docker and Docker Compose running
- PostgreSQL container started: `docker compose -f docker/compose.yml up -d`

## Quick Start

### 1. Start the Application

```bash
./run.sh
```

This script automatically:
- Cleans up any existing process on port 8080
- Starts the Spring Boot application on `http://localhost:8080`

**Alternative** (manual port):
```bash
SERVER_PORT=9090 ./run.sh
```

### 2. Environment Configuration

**JWT Secret** - Ensure your `.env` has a secure secret (≥32 characters):
```bash
JWT_SECRET=this-is-a-development-secret-key-that-is-long-enough-for-hs256
```

If you get a `WeakKeyException` error, your secret is too short.

## API Testing with Postman

### Import the Collection

1. In Postman, click **Import**
2. Select `docs/FamilyOS_API.postman_collection.json`
3. The collection and variables are imported together

### Set Variables

1. Open the collection's **Variables** tab
2. Set `token` = your JWT (from login response below)
3. `base_url` is pre-configured to `http://localhost:8080` (update if using different port)

## Manual Testing Flow

### Step 1: Test Public Endpoints

```bash
# Health check
curl http://localhost:8080/api/health

# Home page
curl http://localhost:8080/
```

### Step 2: Google OAuth Login

1. Open browser: `http://localhost:8080/oauth2/authorization/google`
2. Sign in with a test Google account
3. You'll be redirected with a JSON response containing:
   ```json
   {
     "token": "eyJhbGciOiJIUzUxMiJ9...",
     "user_id": "...",
     "email": "...",
     "name": "..."
   }
   ```
4. Copy the `token` value

### Step 3: Test Protected Endpoints

Replace `<JWT>` with the token from Step 2:

```bash
# Get current user
curl -H "Authorization: Bearer <JWT>" http://localhost:8080/user

# Get Gmail messages
curl -H "Authorization: Bearer <JWT>" http://localhost:8080/gmail/messages

# Test LLM endpoint
curl -H "Authorization: Bearer <JWT>" "http://localhost:8080/llm/test?prompt=Say%20hello"

# Generate LLM response
curl -X POST \
  -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json" \
  -d '{"provider":"gemini","useCase":"general","systemPrompt":"You are concise.","content":"Say hello","metadata":{}}' \
  http://localhost:8080/llm/generate
```

## Request Reference Table

| Endpoint | Method | Auth | Notes |
|---|---|---|---|
| `/api/health` | GET | None | Health check |
| `/` | GET | None | Home page |
| `/oauth2/authorization/google` | GET | None | Initiate Google OAuth |
| `/user` | GET | Bearer JWT | Get current authenticated user |
| `/api/test` | GET | Bearer JWT | Test protected endpoint |
| `/gmail/messages` | GET | Bearer JWT | Get Gmail messages (requires Google token) |
| `/llm/test` | GET | Bearer JWT | Quick LLM test with prompt parameter |
| `/llm/generate` | POST | Bearer JWT | Generate LLM response with custom settings |

## Expected Responses

| Scenario | Status | Response |
|---|---|---|
| Public endpoint (valid) | `200 OK` | Response data |
| Protected endpoint (valid JWT) | `200 OK` | Response data |
| Protected endpoint (no JWT) | `401 Unauthorized` | Error message |
| Protected endpoint (expired JWT) | `401 Unauthorized` | Error message |
| OAuth login success | `200 OK` | `{"token":"...", "user_id":"...", "email":"...", "name":"..."}` |

## Troubleshooting

### Port 8080 already in use

Use `./run.sh` - it automatically cleans the port before starting.

Or manually kill the process and use a different port:
```bash
SERVER_PORT=9090 ./mvnw spring-boot:run
```

### JWT Secret too weak

Error: `WeakKeyException: The specified key byte array is X bits...`

Fix: Update `JWT_SECRET` in `.env` to be at least 32 characters:
```bash
JWT_SECRET=your-super-secure-development-secret-key-that-is-at-least-32-chars-long
```

### Database connection refused

Ensure PostgreSQL is running:
```bash
docker compose -f docker/compose.yml up -d
docker compose -f docker/compose.yml logs postgres
```

### Gmail API errors

Gmail endpoints require a valid Google OAuth token. Ensure:
1. You completed OAuth login with a Google account
2. Gmail API is enabled in your Google Cloud project
3. The token is still valid (not expired)

## Files Reference

- Postman collection: `docs/FamilyOS_API.postman_collection.json`
- Startup script: `run.sh`
- Configuration: `.env` (copy from `.env.example`)
- Application config: `src/main/resources/application.yml`