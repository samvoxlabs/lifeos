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

## Implementation Roadmap

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
