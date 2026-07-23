# Frontend Integration Guide

This guide helps frontend teams integrate with the LifeOS backend APIs.

## 1. What you need to install first

If you are setting this up for the first time, install these tools:

| Tool | Why it is needed | Install |
| --- | --- | --- |
| Docker Desktop | Runs the local PostgreSQL database used by the app | https://www.docker.com/products/docker-desktop/ |
| Java 21 (JDK) | Runs the Spring Boot backend | https://adoptium.net/temurin/releases/?version=21 |
| Git | Clone/pull the codebase | https://git-scm.com/downloads |

Notes:
- On macOS, you may need to allow Terminal access when prompted.
- On Windows, Docker Desktop may ask you to enable WSL2. Accept and restart if prompted.
- After install, restart your terminal.

## 2. Choose terminal by operating system

- **macOS:** Terminal or iTerm
- **Windows (recommended):** Git Bash
- **Windows (alternative):** WSL terminal (Ubuntu, etc.)

`start-app.sh` is a Bash script, so on Windows run it from **Git Bash** or **WSL**, not plain Command Prompt.

## 3. Verify your installation

Run these commands:

```bash
docker --version
java -version
git --version
```

Expected:
- Docker shows a version number.
- Java version should show `21`.
- Git shows a version number.

## 4. Get required API keys and credentials

The app needs Google OAuth credentials and (optionally) an LLM key.

### Google OAuth (required for login + Google sync)

1. Open Google Cloud Console: https://console.cloud.google.com/
2. Create or select a project.
3. Enable these APIs in **APIs & Services > Library**:
   - Gmail API
   - Google Calendar API
   - Google Drive API
   - Google Tasks API
   - People API
4. Go to **APIs & Services > OAuth consent screen**:
   - Choose **External** (or Internal if your org requires it)
   - Fill app name and required fields
   - Add required scopes (email/profile + Google read scopes)
   - Add your Google account as a test user
5. Go to **APIs & Services > Credentials > Create Credentials > OAuth client ID**
6. Application type: **Web application**
7. Add redirect URI:
   - `http://localhost:8080/login/oauth2/code/google`
8. Copy:
   - **Client ID**
   - **Client Secret**

### LLM key (optional, only for extraction/AI features)

- Gemini API key: https://aistudio.google.com/app/apikey
- Copy the key value.

## 5. Configure local environment

In the project root, create `.env` (or let `start-app.sh` create it), then update:

```env
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
JWT_SECRET=your-jwt-secret-at-least-32-bytes
GEMINI_API_KEY=your-gemini-api-key
```

You can leave database values as defaults for local setup.

## 6. Start the backend

### macOS

```bash
./start-app.sh
```

### Windows (Git Bash)

```bash
./start-app.sh
```

### Windows (WSL)

```bash
./start-app.sh
```

This script:
- Starts PostgreSQL in Docker
- Waits for DB readiness
- Runs the Spring Boot app on `http://localhost:8080`

If `.env` is missing, `start-app.sh` creates it from `.env.example` on first run.

## 7. Confirm the app is running

Open this URL in your browser:

`http://localhost:8080/actuator/health`

You should see a healthy response.

## 8. API integration

**Base URL:** `http://localhost:8080`

**Auth:** Most APIs require `Authorization: Bearer <jwt-token>`.

### Token simplification (no paste on every request)

For browser testing, you can avoid manually pasting JWT on each call:

1. Open: `http://localhost:8080/oauth2/authorization/google`
2. Complete Google sign-in once
3. In the same browser session, call API URLs directly (session cookie is reused)

For frontend apps, capture the JWT once after login and add it via a request interceptor/global HTTP client config.

### Step A: Validate keys and connections

Use this first after login to verify Google + LLM setup:

`GET /api/start`

It returns connection status for:
- Google OAuth + connected account/token for current user
- Configured LLM provider + health check

### Step B: Populate frontend data in one call

1. Populate from connected providers (Google sync + processing):
   - `GET /api/populate`
2. Prepopulate using bundled seed data (if DB is empty), then process pending docs:
   - `GET /api/populate?useSeedData=true`

## 9. Main endpoints for frontend integration

| Feature | Method | Path |
| --- | --- | --- |
| Start check (Google + LLM) | GET | `/api/start` |
| Populate (provider sync) | GET | `/api/populate` |
| Populate (seed mode) | GET | `/api/populate?useSeedData=true` |
| Dashboard | GET | `/api/dashboard` |
| Tasks | GET | `/api/tasks`, `/api/tasks/{id}` |
| Events | GET | `/api/events`, `/api/events/{id}` |
| Reminders | GET | `/api/reminders`, `/api/reminders/{id}` |
| Timeline | GET | `/api/timeline` |
| Search | GET | `/api/search?q=<text>` |
| Sync all | POST | `/api/sync` |
| Google sync | POST | `/api/google/sync/gmail`, `/api/google/sync/calendar`, `/api/google/sync/drive` |
| Source docs | GET | `/api/source-documents`, `/api/source-documents/{id}` |

Use `docs/postman/LifeOS_API.postman_collection.json` for request/response examples.
