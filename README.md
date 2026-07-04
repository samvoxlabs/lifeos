# LifeOS

Short project overview and onboarding links for new contributors.

License
-------

This project is licensed under the Apache License 2.0 — see `LICENSE` for details.

Contents
--------

- [Checklist](#checklist)
- [Overview](#overview)
- [Quick start (local)](#quick-start-local)
- [Docker Setup](#docker-setup)
- [Documentation and useful links](#documentation-and-useful-links)
- [Phase 1 documentation](#phase-1-documentation)
- [Architecture docs](#architecture-docs)
- [Roadmap](#roadmap)
- [Development notes](#development-notes)
- [Contributing](#contributing)
- [Need help?](#need-help)

## Checklist

- Read this README
- Follow IDE setup (IntelliJ) → `docs/intellijSetup.md`
- Follow Docker setup → [Docker Setup](#docker-setup) section below
- Start PostgreSQL with Docker: `docker compose -f docker/compose.yml up -d`
- Run the app locally with the Maven wrapper
- Run tests locally before opening a PR: `./mvnw test`

## Overview

LifeOS (formerly FamilyOS) is a Spring Boot-based backend service. It uses Java 21 (see `pom.xml`) and is built with Maven. This repository contains the application code under `src/main/java` and resources under `src/main/resources`.

Runtime state is persisted in PostgreSQL, while canonical user data lives in Google Drive JSON snapshots managed through `/storage/*`.

### Technology Stack

- **Framework:** Spring Boot 3.5.15
- **Java Version:** 21 (LTS)
- **Database:** PostgreSQL 17
- **ORM:** Hibernate with JPA
- **Migrations:** Flyway
- **Authentication:** Spring Security + OAuth2 (Google) + JWT
- **Build:** Maven

### Authentication Flow

1. `GET /oauth2/authorization/google`
2. Google login completes through Spring OAuth2
3. `OAuthSuccessHandler` persists the user, Google account, and Google tokens
4. LifeOS issues a JWT to the browser
5. The frontend stores the JWT and sends it on every API request
6. Protected endpoints validate the JWT through `JwtAuthenticationFilter`
7. Google access tokens are refreshed transparently when Gmail or other Google APIs need them

### Protected Endpoints

These endpoints are public:

- `/oauth2/**`
- `/login/**`
- `/error`
- `/actuator/health`
- `/api/health`

Everything else requires a valid LifeOS JWT.

### LLM Email Extraction

- `POST /llm/email/extract` reads the current user's relevant Gmail messages, sends them through the configured LLM provider, and returns structured JSON.

### Storage Flow

Use the explicit storage APIs to move data between Drive and PostgreSQL:

- `POST /storage/bootstrap`
- `POST /storage/save`
- `GET /storage/status`

The app does not auto-sync on startup or shutdown. On first login, bootstrap creates the Drive folder structure and default JSON files automatically.

Drive stores each user under `LifeOS/users/{userId}/` with `manifest.json`, `profile.json`, `settings.json`, `configuration/email-rules.json`, `configuration/calendar-rules.json`, `configuration/prompts.json`, `knowledge/*.json`, and `integrations/*.json`. Business services continue reading and writing PostgreSQL only.

### Required Environment Variables

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `JWT_SECRET`
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`

## Docker Setup

### Prerequisites

- Docker and Docker Compose installed
- Port 5432 available (PostgreSQL)

### Initial Setup

1. **Copy environment configuration:**
   ```bash
   cp .env.example .env
   ```
   Edit `.env` if you need custom database credentials.

2. **Start PostgreSQL:**
   ```bash
   docker compose -f docker/compose.yml up -d
   ```

3. **Verify PostgreSQL is ready:**
   ```bash
   docker compose -f docker/compose.yml logs postgres
   ```
   Wait for: `database system is ready to accept connections`

4. **Verify the connection:**
   ```bash
   docker compose -f docker/compose.yml exec postgres psql -U lifeos -d lifeos -c "SELECT version();"
   ```

### Common Docker Commands

```bash
# View running containers
docker compose -f docker/compose.yml ps

# View logs
docker compose -f docker/compose.yml logs -f postgres

# Stop PostgreSQL
docker compose -f docker/compose.yml stop

# Start PostgreSQL
docker compose -f docker/compose.yml start

# Restart PostgreSQL (fresh start, preserves data)
docker compose -f docker/compose.yml restart

# Remove containers (keeps volume/data)
docker compose -f docker/compose.yml down

# Remove everything including data
docker compose -f docker/compose.yml down -v

# Access PostgreSQL CLI
docker compose -f docker/compose.yml exec postgres psql -U lifeos -d lifeos
```

### Database Details

- **Container Name:** `lifeos-postgres`
- **Database:** `lifeos`
- **User:** `lifeos`
- **Password:** `lifeos`
- **Port:** `5432` (internal), `5432` (external)
- **Volume:** `lifeos-db-volume` (persistent data)

### Troubleshooting

**Port already in use:**
```bash
# Change port in .env: DB_PORT=5433
# Update connection string accordingly
```

**Database connection refused:**
```bash
# Wait for PostgreSQL to be ready
docker compose -f docker/compose.yml logs postgres

# Verify health check
docker compose -f docker/compose.yml ps
```

**Need to reset database:**
```bash
# This removes all data and recreates the volume
docker compose -f docker/compose.yml down -v
docker compose -f docker/compose.yml up -d
```

## Quick start (local)

### Prerequisites

1. Make sure you have JDK 21 installed and `JAVA_HOME` set. See the IDE setup doc for recommended JDK distributions.
2. Ensure PostgreSQL is running via Docker: `docker compose -f docker/compose.yml up -d`

### Running the Application

From the project root, run:

```bash
./run.sh
```

This script will:
1. Check for any process running on port 8080
2. Kill it if found (prevents "port already in use" errors)
3. Start the Spring Boot application

**Alternative:** To run without the cleanup script:
```bash
./mvnw spring-boot:run
```

**Custom port:** Use the `SERVER_PORT` environment variable:
```bash
SERVER_PORT=9090 ./run.sh
```

The app will start on port 8080 by default (or your custom `SERVER_PORT`).

### JWT Secret Configuration

The JWT secret must be **at least 32 characters long** for HS256 algorithm compliance. It's configured in `.env`:

```bash
JWT_SECRET=your-super-secure-development-secret-key-that-is-at-least-32-chars-long
```

If you get a "WeakKeyException" error, your secret is too short. Update `.env` with a longer secret.

```bash
./mvnw test
```

## Documentation and useful links

- IntelliJ setup: `docs/intellijSetup.md`
- Docker & Database setup: See [Docker Setup](#docker-setup) section above
- Local environment & run instructions: `docs/localSetup.md`
- Getting started & reference: `docs/HELP.md`
- Contribution guide: `docs/CONTRIBUTING.md`
- Repository guide: `docs/REPO_GUIDE.md`
- Architecture docs: `docs/architecture/README.md`
- Roadmap: `docs/ROADMAP.md`
- **API Testing:** Import `docs/FamilyOS_API.postman_collection.json` into Postman for complete API documentation
- Example env file: `.env.example` (copy to `.env` locally)
- Runtime configuration: `src/main/resources/application.yml`
- Flyway migrations: `src/main/resources/db/migration/`
- Project `pom.xml`: contains dependencies and Java version (Java 21)
- Startup script: `run.sh` (auto-cleans port 8080 before starting)

## Phase 1 documentation

- Milestone summary: `docs/milestones/phase1-authentication.md`
- Developer guide: `docs/development/phase1-developer-guide.md`
- API testing guide: `docs/development/phase1-api-testing.md`
- Roadmap after Phase 1: `docs/ROADMAP.md`

## Architecture docs

See `docs/architecture/README.md` for the ADR index and architecture notes.

## Roadmap

See `docs/ROADMAP.md` for the remaining roadmap and `docs/roadmap/backlog.md` for the backlog.


## Code & community

- Contributing (short): `CONTRIBUTING.md`
- Contributing (detailed): `docs/CONTRIBUTING.md`
- Code of conduct: `CODE_OF_CONDUCT.md`
- License: Apache‑2.0 — see `LICENSE`

<!-- Optional CI badge: update the URL to your repo's workflow -->
[![CI](https://github.com/<ORG>/<REPO>/actions/workflows/ci.yml/badge.svg)](https://github.com/<ORG>/<REPO>/actions)

## Development notes

- Main application class: `com.familyos.familyos.FamilyosApplication` (`src/main/java/com/familyos/familyos/FamilyosApplication.java`).
- Configuration properties live under `com.familyos.familyos.config.properties`.
- `application.yml` is the single runtime config entrypoint and imports `.env` locally.
- Database migrations are managed by Flyway: `src/main/resources/db/migration/`
- Hibernate DDL is set to `validate` - schema changes must be made via Flyway migrations
- Tests can be run with:

```bash
./mvnw test
```

## Contributing

If you'd like to contribute, please:

1. Fork the repository and create a feature branch.
2. Ensure Docker PostgreSQL is running for integration tests.
3. Run tests locally and ensure code compiles with JDK 21.
4. Open a pull request with a clear description of changes.

## Need help?

Open an issue or contact the maintainers listed in `pom.xml` (if configured).
