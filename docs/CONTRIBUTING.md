# Contributing to FamilyOS

Thanks for your interest in contributing! This is a short, lightweight guide to help new contributors get started quickly.

Getting started
- Fork the repository and create a branch named `feature/<short-description>` or `fix/<short-description>`.
- Keep changes focused and open a Pull Request from your branch to `main`.

Running and testing locally
- Make sure you have JDK 21 installed and `JAVA_HOME` set.
- To run the app locally:

```bash
./mvnw spring-boot:run
```

- To run tests:

```bash
./mvnw test
```

Code style and quality
- Keep changes small and documented.
- If adding Java code, follow the existing project formatting and patterns.
- Run tests locally before opening a PR.

Pull request checklist
- [ ] The branch builds and tests pass locally.
- [ ] The PR description explains the change and the reason.
- [ ] No secrets or credentials are included (use environment variables or `docs/.env.example`).

Communication
- If the change is non-trivial, open an issue first to discuss design and scope.

Thanks — maintainers will review your PR and request changes if needed.
