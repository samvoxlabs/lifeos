# Roadmap

Phase 1 is complete and merged. The remaining work is sequenced below.

## Phase 2 — Google Integrations

- **Goal:** Expand Google support beyond Gmail.
- **Deliverables:** Calendar and Drive integrations, Google service abstraction, OAuth reuse.
- **Major components:** integration adapters, provider-specific clients, token reuse layer.
- **Planned APIs:** Gmail, Calendar, Drive feature endpoints.
- **Definition of done:** all Google APIs share the same persisted OAuth model and refresh path.
- **Estimated complexity:** High
- **Dependencies:** Phase 1 auth baseline
- **Recommended order:** first harden shared Google service abstractions, then add Calendar, then Drive.

## Phase 3 — AI Extraction

- **Goal:** Turn raw Google content into structured data.
- **Deliverables:** parsing pipeline, prompt engineering, structured JSON extraction, confidence scoring.
- **Major components:** LLM abstraction, extraction services, validation rules.
- **Definition of done:** Gmail and related sources can be converted into stable structured records.
- **Estimated complexity:** High
- **Dependencies:** Phase 2 Google integrations
- **Recommended order:** define data contracts first, then extraction prompts, then confidence metadata.

## Phase 4 — Core Domain

- **Goal:** Persist the product’s canonical entities.
- **Deliverables:** tasks, events, reminders, action items, persistence, deduplication.
- **Major components:** domain models, repositories, write services, dedupe rules.
- **Definition of done:** extracted data can be stored and deduplicated as canonical records.
- **Estimated complexity:** High
- **Dependencies:** Phase 3 extraction output
- **Recommended order:** domain schema, write APIs, deduplication, then relationship handling.

## Phase 5 — Action Engine

- **Goal:** Convert core data into user-facing actions.
- **Deliverables:** follow-up suggestions, deadlines, smart reminders, notifications, daily digest.
- **Major components:** rule engine, scheduling, digest generation.
- **Definition of done:** the system proposes useful actions from persisted records.
- **Estimated complexity:** Medium
- **Dependencies:** Phase 4 core domain
- **Recommended order:** start with reminder and digest rules, then notifications.

## Phase 6 — Dashboard APIs

- **Goal:** Expose read-friendly views for the frontend.
- **Deliverables:** dashboard, timeline, tasks, events, search, summary endpoints.
- **Major components:** read models, query endpoints, search indexing.
- **Definition of done:** the frontend can render the product dashboard entirely from APIs.
- **Estimated complexity:** Medium
- **Dependencies:** Phases 4 and 5
- **Recommended order:** build read models first, then dashboard and search.

## Phase 7 — Production Readiness

- **Goal:** Harden the platform for long-term use.
- **Deliverables:** CI/CD, observability, security hardening, rate limiting, performance, deployment, release checklist.
- **Major components:** pipeline automation, metrics, alerts, security controls.
- **Definition of done:** the service is deployable, observable, and safe to operate.
- **Estimated complexity:** Medium
- **Dependencies:** all prior phases
- **Recommended order:** automate release flow, then monitoring, then hardening and performance.

## Backlog

See `docs/roadmap/backlog.md` for smaller follow-on items and future ideas.
