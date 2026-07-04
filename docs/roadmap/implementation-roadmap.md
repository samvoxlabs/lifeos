# LifeOS Implementation Roadmap

This document tracks the implementation phases, feature branches, and current development status for the LifeOS project.

## Git Workflow

```text
main
    ↓
feature/<feature-name>
    ↓
Pull Request
    ↓
Squash Merge
    ↓
main
```

Rules:

* Never develop directly on `main`.
* One feature branch = one Pull Request.
* One Pull Request = one logical capability.
* Delete the feature branch after merge.
* Create the next feature branch from the latest `main`.

---

## Current Status

**Current Phase:** Phase 6 - Domain and Persistence

**Current Branch:** `feature/phase6-domain-persistence`

**Next Objective:** Phase 7 - Frontend APIs

Persist First, Process Second with SourceDocument as the operational system of record.

---

## Phase 1 – Authentication Foundation ✅

**Branch**

* `feature/oauth`

**Completed**

* Google OAuth2 Login
* Spring Security
* JWT Authentication
* User Persistence
* Google Token Persistence
* Protected APIs

---

## Phase 2 – Google Integrations

**Objective**

Connect LifeOS to Google services and retrieve normalized data.

**Feature Branches**

* `feature/google-core`
* `feature/google-gmail`
* `feature/google-calendar`
* `feature/google-drive`

---

## Phase 3 – LLM Foundation ✅

**Objective**

Introduce a provider-agnostic AI layer.

**Feature Branch**

* `feature/llm-core` (merged to main)

**Completed Deliverables**

* LLM provider abstraction (Strategy pattern)
* Multi-provider support (Gemini, Groq, OpenRouter)
* Provider-agnostic service layer (LlmService)
* Configuration-driven provider selection (application.yml)
* REST API endpoints (/api/llm/generate, /api/llm/health)
* JWT authentication integration
* Comprehensive testing guide (docs/development/phase3-api-testing.md)
* Postman collections (`docs/postman/phase3.postman_collection.json`, `docs/postman/FamilyOS_API.postman_collection.json`)

**PR:** #15 (merged)

---

## Phase 4 – Rule Engine ✅

**Objective**

Determine which documents should be processed by the LLM using configurable rules.

**Feature Branch**

* `feature/phase4-rule-engine` (merged to main, PR #16)

**Completed Deliverables**

* Rule abstraction (interface with priority-based execution)
* Three rule implementations (SenderRule, LabelRule, KeywordRule)
* Priority-based rule evaluation (Strategy pattern)
* Spring Bean auto-discovery for extensibility (Open/Closed Principle)
* RuleEngineService facade layer
* REST API endpoint (/api/rules/evaluate)
* Comprehensive testing guide (docs/development/phase4-api-testing.md)
* Postman collections (`docs/postman/phase4.postman_collection.json`)
* 33 unit tests (all passing)
* Clean, modular architecture suitable for future phases

**PR:** #16 (merged)

---

## Phase 5 – AI Extraction ✅

**Objective**

Apply the Rule Engine and extract structured information from documents.

**Feature Branch**

* `feature/phase5-ai-extraction`

**Completed Deliverables**

* Rule Engine pre-filtering before LLM invocation
* External prompt templates for extraction
* PromptBuilder and PromptLoader
* Structured JSON parsing into ExtractionResult
* Extraction controller: `POST /api/extraction/process`
* Unit tests for prompt building, parsing, and orchestration
* Integration test for the extraction endpoint
* Phase 5 API testing guide
* Phase 5 Postman collection
* Consolidated API collection updated

---

## Phase 6 - Domain and Persistence

**Objective**

Implement Persist First, Process Second so every imported document is persisted before Rule Engine + LLM processing.

**Feature Branch**

* `feature/phase6-domain-persistence`

**Deliverables**

* SourceDocument as canonical system of record
* Processing lifecycle: NEW, PROCESSING, PROCESSED, FAILED, SKIPPED
* Processing pipeline from persisted source documents only
* Idempotent deduplication by provider + external ID + source type
* Extraction and Action persistence model
* Task, Event, and Reminder domain action types
* ActionMapper for AI-to-domain mapping
* Flyway schema migration and PostgreSQL operational persistence
* Startup seed-import support from shared seed repository layout
* Testing endpoints:
  * `POST /api/domain/process`
  * `GET /api/tasks`
  * `GET /api/events`
  * `GET /api/reminders`

**Data Strategy**

* PostgreSQL is the operational database and processing queue.
* Google Drive is the shared seed repository (not operational storage).
* Seed imports are idempotent and run only when SourceDocument is empty.

---

## Phase 7 – Frontend APIs

**Objective**

Expose LifeOS data to the frontend.

**Feature Branches**

* `feature/dashboard-api`
* `feature/task-api`
* `feature/event-api`
* `feature/reminder-api`
* `feature/search-api`

**Deliverables**

* Dashboard API
* Task API
* Event API
* Reminder API
* Search API

---

## Scope Rule

Before implementing any new feature:

1. Verify the current phase.
2. Verify the current feature branch.
3. Confirm the requested work belongs to the current phase.
4. If it belongs to a future phase:

    * Explain the architectural boundary.
    * Recommend the appropriate feature branch.
    * Do not implement it without approval.

Keep every Pull Request focused on **one logical capability**.

---

## Development Standards

Every feature branch must follow the standards defined in:

* `docs/DEVELOPMENT_STANDARDS.md` – Complete workflow and requirements
* `COPILOT.md` → **Definition of Done** – Delivery checklist
* `docs/templates/` – Reusable templates for documentation

### Deliverables for Every Phase

1. **Feature Implementation** (Java code + tests)
2. **Unit Tests** (minimum 3+ per component)
3. **Integration Tests** (where appropriate)
4. **API Testing Guide** (`docs/development/phaseX-api-testing.md`)
5. **Postman Collection** (`docs/postman/phaseX.postman_collection.json`)
6. **Roadmap Update** (`docs/roadmap/implementation-roadmap.md`)
7. **README Update** (if required)

All deliverables are part of the feature implementation, not follow-up tasks.

### PR Checklist

Every Pull Request must verify:

- [ ] Scope matches current feature branch
- [ ] No out-of-scope features implemented
- [ ] Build succeeds: `mvn clean package -DskipTests`
- [ ] All tests pass: `mvn test`
- [ ] API Testing Guide created/updated
- [ ] Postman Collection created/updated
- [ ] Implementation Roadmap updated
- [ ] README updated (if required)
- [ ] Commit message explains *why*
- [ ] Co-authored-by trailer included

This ensures consistency across all phases and faster code review.
