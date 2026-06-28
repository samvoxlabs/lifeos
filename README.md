# FamilyOS

Short project overview and onboarding links for new contributors.

Checklist
- Read this README
- Follow IDE setup (IntelliJ) -> `src/main/java/com/familyos/familyos/docs/intellijSetup.md`
- Follow local environment setup -> `src/main/java/com/familyos/familyos/docs/localSetup.md`
- Run the app locally with the Maven wrapper

Overview

FamilyOS is a Spring Boot-based backend service. It uses Java 21 (see `pom.xml`) and is built with Maven. This repository contains the application code under `src/main/java` and resources under `src/main/resources`.

Quick start (local)

1. Make sure you have JDK 21 installed and `JAVA_HOME` set. See the IDE setup doc for recommended JDK distributions.
2. From the project root, run:

```bash
./mvnw spring-boot:run
```

3. The app will start on port 8080 by default (unless overridden in `src/main/resources/application.yml`).

Documentation and useful links

- IntelliJ setup: `src/main/java/com/familyos/familyos/docs/intellijSetup.md`
- Local environment & run instructions: `src/main/java/com/familyos/familyos/docs/localSetup.md`
- Project `pom.xml`: contains dependencies and Java version (Java 21)

Development notes

- Main application class: `com.familyos.familyos.FamilyosApplication` (`src/main/java/com/familyos/familyos/FamilyosApplication.java`).
- Tests can be run with:

```bash
./mvnw test
```

Contributing

If you'd like to contribute, please:

1. Fork the repository and create a feature branch.
2. Run tests locally and ensure code compiles with JDK 21.
3. Open a pull request with a clear description of changes.

Need help?

Open an issue or contact the maintainers listed in `pom.xml` (if configured).

