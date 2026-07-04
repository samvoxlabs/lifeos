# LifeOS

## Project Overview

LifeOS is an AI-powered Personal Operating System built as a Proof of Concept (PoC).

Primary goals:

- Validate architecture
- Validate AI workflows
- Validate user experience
- Build a modular monolith
- Keep the design extensible

Technology:

- Java 21
- Spring Boot 3
- PostgreSQL
- Flyway
- Spring Security
- OAuth2
- JWT

---

## Engineering Principles

- Keep the implementation simple.
- Do not over-engineer.
- Follow SOLID.
- Follow Clean Architecture.
- Use constructor injection.
- Never expose JPA entities.
- Always return DTOs.
- Keep business logic out of controllers.
- Keep provider-specific logic isolated.

---

## Git Workflow

Never develop on main.

Always use:

feature/<feature-name>

Workflow:

Feature Branch
↓

Pull Request
↓

Squash Merge
↓

main

Delete merged feature branches.

---

## Scope Assessment

Before implementing:

1. Identify current milestone.
2. Determine whether the request belongs to the milestone.
3. If not:
    - Explain the architectural boundary.
    - Recommend separate feature branches.
    - Wait for approval.

---

## Proof of Concept Philosophy

This project is currently a PoC.

Optimize for:

- Simplicity
- Readability
- Modularity
- Fast iteration

Avoid production complexity unless requested.

---

## AI Philosophy

Treat AI as an extraction engine.

Never let AI implement business logic.

Pipeline:

Document

↓

Rule Engine

↓

LLM

↓

Structured Extraction

↓

Domain Mapping

↓

Persistence

---

## Before Every Implementation

Always explain:

- Design
- Trade-offs
- Why the implementation fits the current milestone

---

See docs/copilot for detailed guidance.

## Definition of Done

A feature branch is considered complete only when all of the following have been delivered.

### Implementation

- Feature implementation completed.
- Scope matches the current phase (verify with implementation-roadmap.md).
- No functionality from future phases has been introduced.
- Architecture remains consistent with project guidelines (SOLID, Clean Architecture).
- Code follows project coding standards (constructor injection, DTOs, clean controllers).
- Build succeeds: `mvn clean package -DskipTests`.
- All tests pass: `mvn test`.

### Testing

- Unit tests completed (minimum 3+ tests per component).
- Integration tests completed where appropriate.
- Manual API validation completed via curl or Postman.
- All test scenarios documented in API Testing Guide.

### Documentation

Every feature branch must include the following deliverables:

#### 1. API Testing Guide

Create: `docs/development/phaseX-api-testing.md`

Contents:
- Phase overview and objectives
- What was implemented in this phase
- Prerequisites and setup
- Authentication requirements (if applicable)
- Quick Start section
- API endpoint overview
- Complete curl command examples
- Expected request/response examples
- Error scenarios and troubleshooting
- Files added or modified
- Next phase recommendation

The guide should allow a new developer to validate the feature without reading source code.

#### 2. Postman Collection

Create: `docs/development/phaseX-postman_collection.json`

Requirements:
- Include every endpoint introduced in the phase
- Use project variables: `{{baseUrl}}` and `{{token}}`
- Include sample request bodies and example responses
- Organized into logical test groups
- All requests include proper authentication headers

#### 3. Implementation Roadmap

Update: `docs/roadmap/implementation-roadmap.md`

Changes:
- Mark completed phase with ✅
- Update "Current Phase" status
- Add completed deliverables list
- Reference PR number
- Update "Next Phase" section

#### 4. README (if required)

Update only when necessary.

Examples:
- New configuration requirements
- New dependencies
- Setup or workflow changes
- Breaking API changes

### Code Quality

- No hardcoded values (use configuration).
- No unnecessary abstractions.
- Meaningful variable and method names.
- Appropriate logging at INFO and DEBUG levels.
- Centralized exception handling.

### Pull Request Checklist

Every PR must verify:

- [ ] Scope matches current feature branch
- [ ] No out-of-scope features implemented
- [ ] Build succeeds (`mvn clean package -DskipTests`)
- [ ] All tests pass (`mvn test`)
- [ ] API Testing Guide created/updated
- [ ] Postman Collection created/updated
- [ ] Implementation Roadmap updated
- [ ] README updated (if required)
- [ ] Commit message explains *why*, not just *what*
- [ ] Co-authored-by trailer included in commit

Before proposing or implementing any feature, refer to:

`docs/roadmap/implementation-roadmap.md`

Determine:

- Current project phase
- Current feature branch
- Current implementation scope

If the requested work belongs to a future phase:

1. Stop implementation.
2. Explain the architectural boundary.
3. Recommend the appropriate feature branch.
4. Do not implement work outside the current phase unless explicitly approved.

Always keep Pull Requests aligned with the current feature branch and milestone.
