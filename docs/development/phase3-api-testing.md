# Phase 3 API Testing Guide

Use this guide to validate the LLM Foundation (provider-agnostic AI layer) added in Phase 3.

## What Phase 3 covers

- Provider-agnostic LLM abstraction layer
- Google Gemini provider
- Groq provider (OpenAI-compatible)
- OpenRouter provider (OpenAI-compatible)
- Configuration-driven provider selection
- Health check endpoint for provider validation

All endpoints work with any configured provider without code changes.

## Prerequisites

- Java 21 JDK installed
- Docker installed (for PostgreSQL; auto-managed by `run.sh`)
- Google account for OAuth authentication (or valid JWT token)
- API keys for each LLM provider (optional; testing works with one):
  - Google Gemini: https://ai.google.dev/
  - Groq: https://console.groq.com/
  - OpenRouter: https://openrouter.ai/

## Quick Start

### 1. Configure API keys (optional)

Create a `.env` file in the project root with your API keys:

```env
# Select your primary provider (gemini, groq, or openrouter)
LLM_DEFAULT_PROVIDER=groq

# Gemini Configuration
GEMINI_API_KEY=your-gemini-api-key
GEMINI_MODEL=gemini-2.5-flash
GEMINI_BASE_URL=https://generativelanguage.googleapis.com/v1beta

# Groq Configuration (recommended for free testing)
GROQ_API_KEY=your-groq-api-key
GROQ_MODEL=llama-3.3-70b-versatile
GROQ_BASE_URL=https://api.groq.com/openai/v1

# OpenRouter Configuration (optional)
OPENROUTER_API_KEY=your-openrouter-api-key
OPENROUTER_MODEL=google/gemini-2.5-flash
OPENROUTER_BASE_URL=https://openrouter.ai/api/v1
```

### 2. Start the Application

Run the startup script from the project root:

```bash
./run.sh
```

This script automatically:
- ✅ Starts PostgreSQL database container (if not running)
- ✅ Verifies database connectivity
- ✅ Frees port 8080 (kills any existing process)
- ✅ Starts the FamilyOS application

The app will be available at `http://localhost:8080`

### 3. Get Authentication Token

Phase 3 LLM endpoints require JWT authentication:

**Step 1:** Open in your browser:
```
http://localhost:8080/oauth2/authorization/google
```

**Step 2:** Login with your Google account

**Step 3:** After redirect, you'll see a JSON response with your token:
```json
{
  "token": "<your-jwt-token>",
  "user_id": "unique-user-id",
  "email": "your-email@gmail.com",
  "name": "Your Name"
}
```

**Step 4:** Copy the entire `token` value (a long JWT string). You'll use this for all requests.

---

## Manual testing flow

### 1. Health check endpoint

Verify the configured provider is healthy and responding:

```bash
curl http://localhost:8080/api/llm/health \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

Replace `YOUR_JWT_TOKEN` with the token from OAuth (Step 3 above).

Expected response:

```json
{
  "provider": "gemini",
  "healthy": true,
  "message": "Provider is healthy and responding"
}
```

### 2. Generate content with default provider

Test LLM generation using the configured default provider:

```bash
curl -X POST http://localhost:8080/api/llm/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "systemPrompt": "Be concise.",
    "userPrompt": "What is the capital of France?"
  }'
```

Replace `YOUR_JWT_TOKEN` with the token from OAuth.

Expected response:

```json
{
  "provider": "gemini",
  "model": "gemini-2.5-flash",
  "response": "The capital of France is Paris."
}
```

### 3. Generate content with explicit provider

Override the default provider by specifying one in the request:

```bash
curl -X POST http://localhost:8080/api/llm/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "systemPrompt": "Be concise.",
    "userPrompt": "What is the capital of France?",
    "provider": "groq"
  }'
```

Expected response uses Groq:

```json
{
  "provider": "groq",
  "model": "llama-3.3-70b-versatile",
  "response": "The capital of France is Paris."
}
```

### 4. Test with OpenRouter

Similar to above, use provider: "openrouter":

```bash
curl -X POST http://localhost:8080/api/llm/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "systemPrompt": "Be concise.",
    "userPrompt": "What is the capital of France?",
    "provider": "openrouter"
  }'
```

## Request reference table

| Endpoint | Method | Purpose | Auth |
|---|---|---|---|
| `/api/llm/health` | GET | Check if configured provider is healthy and responding | No |
| `/api/llm/generate` | POST | Generate content using the configured or specified provider | No |

## Request/Response format

### POST /api/llm/generate

**Request JSON:**

```json
{
  "systemPrompt": "You are a concise assistant.",
  "userPrompt": "What is AI?",
  "provider": "gemini"
}
```

Fields:
- `systemPrompt` (required): Instructions for the LLM (e.g., tone, style)
- `userPrompt` (required): The question or content to generate
- `provider` (optional): Specify provider (gemini, groq, openrouter). If omitted, uses LLM_DEFAULT_PROVIDER

**Response JSON:**

```json
{
  "provider": "gemini",
  "model": "gemini-2.5-flash",
  "response": "AI (Artificial Intelligence) refers to computer systems designed to perform tasks that typically require human intelligence, such as learning, reasoning, and problem-solving."
}
```

Fields:
- `provider`: The provider that generated the response
- `model`: The specific model used
- `response`: The generated text content

## Provider switching

To switch providers without code changes:

1. Stop the running application (Ctrl+C)
2. Change `LLM_DEFAULT_PROVIDER` in `.env`:
   ```env
   LLM_DEFAULT_PROVIDER=groq
   ```
3. Restart the application:
   ```bash
   ./run.sh
   ```
4. Verify the new provider:
   ```bash
   curl http://localhost:8080/api/llm/health
   ```

Expected output shows the new provider:

```json
{
  "provider": "groq",
  "healthy": true,
  "message": "Provider is healthy and responding"
}
```

**Key point:** No Java code changes. No recompilation. Only configuration change.

## Expected responses

- Health check returns `200 OK` with provider status
- Generate endpoint returns `200 OK` with `{ provider, model, response }`
- Missing API key returns `500` with error message indicating which key is required
- Invalid provider name returns `400` with unsupported provider error
- Provider-specific responses vary in content but follow the same JSON structure

## Using Postman

All Phase 3 requests require authentication. To use Postman:

### Set Bearer Token

**Option A: Import the collection and set token as variable**

1. Import: `docs/postman/phase3.postman_collection.json` or `docs/postman/FamilyOS_API.postman_collection.json`
2. Go to **Environment** tab
3. Add or edit environment variable:
   - Key: `token`
   - Value: Your JWT token (from OAuth login)
4. In requests, the `Authorization` header automatically uses `{{token}}`

**Option B: Add token to each request manually**

1. Open a request (e.g., "Phase 3 – Health Check")
2. Go to **Headers** tab
3. Add header:
   - Key: `Authorization`
   - Value: `Bearer <YOUR_JWT_TOKEN>`
4. Click **Send**

### Example Postman Request

**Request:** Phase 3 – Generate (Default Provider)

Headers:
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI...
Content-Type: application/json
```

Body (raw JSON):
```json
{
  "systemPrompt": "You are a helpful assistant.",
  "userPrompt": "What is AI?"
}
```

Response:
```json
{
  "provider": "gemini",
  "model": "gemini-2.5-flash",
  "response": "AI (Artificial Intelligence) refers to computer systems designed to perform tasks..."
}
```

---

## Troubleshooting

### Missing or invalid authentication token

If you get a `401 Unauthorized` error:

1. **Get a token:**
   - Open: `http://localhost:8080/oauth2/authorization/google`
   - Login with Google
   - Copy the `token` value from the JSON response

2. **Use the token in requests:**
   - Curl: Add `-H "Authorization: Bearer YOUR_TOKEN"`
   - Postman: Add header with key `Authorization` and value `Bearer YOUR_TOKEN`
   - Or set `{{token}}` environment variable in Postman

3. **If token expired:**
   - Get a new token by logging in again at OAuth endpoint
   - Replace the old token

### Port 8080 already in use

Use `./run.sh` or set a custom port:

```bash
SERVER_PORT=9090 ./run.sh
```

### Missing or invalid API keys

If health check fails with "API key is required":

1. Verify the correct provider API key is set in `.env`
2. Make sure the key is not expired or revoked
3. Confirm the key has the necessary permissions/scopes with the provider
4. Restart the application after changing the key

Required API keys depend on which provider(s) you test:
- Gemini: https://ai.google.dev/
- Groq: https://console.groq.com/
- OpenRouter: https://openrouter.ai/

### Provider not found error

If you get "Unsupported LLM provider" error:

1. Check provider name spelling (must be lowercase):
   - Valid: `gemini`, `groq`, `openrouter`
2. Verify the provider is configured in `application.yml`
3. Confirm environment variables are loaded (restart app if you changed `.env`)

### Slow responses

LLM providers have varying response times:
- Groq is typically fastest (< 2 seconds)
- Gemini and OpenRouter may take 1-4 seconds
- First request after app startup may be slower

If consistently slow:
1. Check network connectivity
2. Verify no other heavy processes on the server
3. Check provider status page for outages
4. Review application logs

## Files to know

- API testing guide: `docs/development/phase3-api-testing.md`
- Startup script: `run.sh`
- Environment sample: `.env.example`
- App config: `src/main/resources/application.yml`
- LLM configuration: See `llm.*` properties in `application.yml`
