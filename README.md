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

### Technology Stack

- **Framework:** Spring Boot 3.5.15
- **Java Version:** 21 (LTS)
- **Database:** PostgreSQL 17
- **ORM:** Hibernate with JPA
- **Migrations:** Flyway
- **Authentication:** Spring Security + OAuth2 (Google) + JWT
- **Build:** Maven

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
./mvnw spring-boot:run
```

The app will start on port 8080 by default (unless overridden in `src/main/resources/application.yml`).

### Running Tests

```bash
./mvnw test
```

## Documentation and useful links

- IntelliJ setup: `docs/intellijSetup.md`
- Docker & Database setup: See [Docker Setup](#docker-setup) section above
- Local environment & run instructions: `docs/localSetup.md`
- Getting started & reference: `docs/HELP.md`
- Contribution guide (detailed): `docs/CONTRIBUTING.md` (short pointer at repo root: `CONTRIBUTING.md`)
- Repository guide (what lives where): `docs/REPO_GUIDE.md`
- Example env file: `.env.example` (copy to `.env` locally)
- Runtime configuration: `src/main/resources/application.yml`
- Environment variables: `.env` (copy from `.env.example`)
- Flyway migrations: `src/main/resources/db/migration/`
- Project `pom.xml`: contains dependencies and Java version (Java 21)


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

