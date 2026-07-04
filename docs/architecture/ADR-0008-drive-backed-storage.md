# ADR-0008 Drive-Backed Storage

## Status
Accepted

## Context
LifeOS needs durable user-owned storage that is editable outside the app, while PostgreSQL remains the runtime database used by business services.
Google Drive is the canonical store for user snapshots; JSON files are the interchange format for load/save.

## Decision
Use Google Drive as the canonical store for JSON snapshots. Provide explicit `/storage/bootstrap`, `/storage/save`, and `/storage/status` APIs to move data between Drive and PostgreSQL. Do not auto-sync on startup or shutdown.
Keep the Drive layout under `LifeOS/users/{userId}/` with `manifest.json`, `profile.json`, `settings.json`, `configuration/email-rules.json`, `configuration/calendar-rules.json`, `configuration/prompts.json`, `knowledge/*.json`, and `integrations/*.json`.

## Consequences
- Drive becomes the canonical persistence layer
- PostgreSQL remains the runtime data store
- Synchronization is always explicit
- Storage import/export stays separate from business services
- Drive write access requires `drive.file`; users may need to re-consent after scope changes
