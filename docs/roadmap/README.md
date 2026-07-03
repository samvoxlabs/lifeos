# Roadmap After Phase 1

Phase 1 is complete. The next milestones build on the authenticated backend without changing the auth foundation.

## Phase 2 — Google Integrations

- **Goal:** Expand Google coverage beyond Gmail.
- **Scope:** Gmail, Calendar, Drive, OAuth reuse, Google service abstraction.
- **Major components:** integration adapters, provider-specific clients, token reuse.
- **APIs:** Gmail, Calendar, Drive feature endpoints.
- **Dependencies:** Phase 1 auth foundation.
- **Definition of done:** Google APIs share the same persisted OAuth model and token refresh path.
- **Complexity:** High
- **Effort:** Large
- **Key deliverables:** provider-agnostic Google integration layer, first Calendar/Drive endpoints.

## Phase 3 — AI Extraction

- **Goal:** Turn raw Google content into structured data.
- **Scope:** Email parsing, prompt engineering, structured JSON extraction, LLM abstraction, confidence scoring.
- **Major components:** parsing pipeline, LLM prompts, response validation.
- **APIs:** extraction and classification endpoints.
- **Dependencies:** Phase 2 integrations.
- **Definition of done:** Gmail and other sources can be converted into stable structured records.
- **Complexity:** High
- **Effort:** Large
- **Key deliverables:** extraction services, schema validation, confidence metadata.

## Phase 4 — Core Domain

- **Goal:** Persist real product entities.
- **Scope:** Tasks, events, reminders, action items, persistence, deduplication.
- **Major components:** domain models, repositories, write services, dedupe rules.
- **APIs:** task/event/reminder CRUD and ingestion endpoints.
- **Dependencies:** Phase 3 extraction output.
- **Definition of done:** extracted data can be stored and deduplicated as canonical records.
- **Complexity:** High
- **Effort:** Large
- **Key deliverables:** core domain schema, canonical records, dedupe rules.

## Phase 5 — Action Engine

- **Goal:** Convert core data into user-facing actions.
- **Scope:** Follow-up suggestions, deadlines, smart reminders, notifications, daily digest.
- **Major components:** rule engine, notification scheduling, digest generation.
- **APIs:** action feed, reminder, and digest endpoints.
- **Dependencies:** Phase 4 domain data.
- **Definition of done:** the system suggests actionable follow-ups from domain records.
- **Complexity:** Medium
- **Effort:** Medium-Large
- **Key deliverables:** action engine, reminder scheduling, daily summaries.

## Phase 6 — Dashboard APIs

- **Goal:** Expose read-friendly views for the frontend.
- **Scope:** Dashboard, timeline, tasks, events, search, summary endpoints.
- **Major components:** read models, query endpoints, search indexing.
- **APIs:** dashboard, timeline, search, summary endpoints.
- **Dependencies:** Phases 4 and 5.
- **Definition of done:** the frontend can render a complete product dashboard from API data.
- **Complexity:** Medium
- **Effort:** Medium
- **Key deliverables:** dashboard read APIs, timeline and search views.

## Phase 7 — Production Readiness

- **Goal:** Harden the platform for long-term use.
- **Scope:** CI/CD, monitoring, observability, security hardening, rate limiting, performance, deployment, release checklist.
- **Major components:** pipeline automation, metrics, alerts, security controls.
- **APIs:** operational and health endpoints as needed.
- **Dependencies:** prior delivery phases.
- **Definition of done:** the service is deployable, observable, and operationally safe.
- **Complexity:** Medium
- **Effort:** Medium-Large
- **Key deliverables:** release pipeline, monitoring, security review, deployment checklist.
