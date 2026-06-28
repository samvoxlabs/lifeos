# FamilyOS

Short project overview and onboarding links for new contributors.

License
-------

This project is licensed under the Apache License 2.0 — see `LICENSE` for details.

Contents
--------

- [Checklist](#checklist)
- [Overview](#overview)
- [Quick start (local)](#quick-start-local)
- [Documentation and useful links](#documentation-and-useful-links)
- [Development notes](#development-notes)
- [Contributing](#contributing)
- [Need help?](#need-help)

## Checklist

- Read this README
- Follow IDE setup (IntelliJ) → `docs/intellijSetup.md`
- Follow local environment setup → `docs/localSetup.md`
- Run the app locally with the Maven wrapper
 - Run tests locally before opening a PR: `./mvnw test`

## Overview

FamilyOS is a Spring Boot-based backend service. It uses Java 21 (see `pom.xml`) and is built with Maven. This repository contains the application code under `src/main/java` and resources under `src/main/resources`.

## Quick start (local)

1. Make sure you have JDK 21 installed and `JAVA_HOME` set. See the IDE setup doc for recommended JDK distributions.
2. From the project root, run:

```bash
./mvnw spring-boot:run
```

3. The app will start on port 8080 by default (unless overridden in `src/main/resources/application.yml`).

## Documentation and useful links

- IntelliJ setup: `docs/intellijSetup.md`
- Local environment & run instructions: `docs/localSetup.md`
- Getting started & reference: `docs/HELP.md`
- Contribution guide (detailed): `docs/CONTRIBUTING.md` (short pointer at repo root: `CONTRIBUTING.md`)
- Repository guide (what lives where): `docs/REPO_GUIDE.md`
- Example env file (copy to `.env` locally): `docs/.env.example`
- Importable IntelliJ run configuration: `docs/FamilyosApplication_run_config.xml`
- Project `pom.xml`: contains dependencies and Java version (Java 21)

### Swagger (OpenAPI)

If you want to quickly view or share the API contract without opening the full IDE, use the auto-generated Swagger UI provided by springdoc-openapi.

- If you have a running instance (local or deployed), open:

  ```
  http://<host>:<port>/swagger-ui/index.html
  ```

- To run the packaged jar locally (no IDE required):

  ```bash
  ./mvnw -DskipTests package
  java -jar target/*.jar
  # then open http://localhost:8080/swagger-ui/index.html
  ```

- To fetch the raw OpenAPI JSON (useful for automated tools):

  ```bash
  curl http://localhost:8080/v3/api-docs
  ```

Note: Swagger UI is enabled as a local POC; secure or disable it in production as needed.

## Code & community

- Contributing (short): `CONTRIBUTING.md`
- Contributing (detailed): `docs/CONTRIBUTING.md`
- Code of conduct: `CODE_OF_CONDUCT.md`
- License: Apache‑2.0 — see `LICENSE`

<!-- Optional CI badge: update the URL to your repo's workflow -->
[![CI](https://github.com/<ORG>/<REPO>/actions/workflows/ci.yml/badge.svg)](https://github.com/<ORG>/<REPO>/actions)

## Development notes

- Main application class: `com.familyos.familyos.FamilyosApplication` (`src/main/java/com/familyos/familyos/FamilyosApplication.java`).
- Tests can be run with:

```bash
./mvnw test
```

## Contributing

If you'd like to contribute, please:

1. Fork the repository and create a feature branch.
2. Run tests locally and ensure code compiles with JDK 21.
3. Open a pull request with a clear description of changes.

## Need help?

Open an issue or contact the maintainers listed in `pom.xml` (if configured).

