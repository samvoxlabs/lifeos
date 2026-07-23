# LifeOS Backend Architecture

LifeOS is a Gmail → AI → Calendar orchestration backend.

## Goals

- Gmail is read from Google.
- Events are inferred by LifeOS.
- Conflicts are owned by LifeOS.
- Calendar is published back to Google only after user approval.
- Controllers stay thin.
- All business logic lives in shared services.

## Layering

```text
Controller
  ↓
Service
  ↓
Domain / Repository / Integration
```

## API layers

### Developer APIs

Used for testing and demos. They expose the workflow step-by-step:

- Gmail load/reset/list
- Event extraction
- DB inspection
- Conflict resolution
- Calendar publish/list/reset
- System status

### Application APIs

Used by the React frontend:

- Mail sync
- Schedule review
- Conflict resolution
- Calendar publish
- Calendar events

Both layers call the same service classes.

## Core services

| Service | Responsibility |
|---|---|
| `GmailService` / Gmail adapter | Read and import Gmail messages |
| `MailboxService` | Orchestrate sync, review, and conflict flow |
| `EventExtractionService` | Convert emails into structured events |
| `ConflictService` | Detect overlaps and build resolution options |
| `CalendarService` | Publish approved events and read Google Calendar |
| `DemoGmailService` | Load and reset demo Gmail data |

## Data flow

```text
Gmail sync
  → normalize messages
  → rule engine filters relevant mail
  → LLM extracts events
  → conflicts are detected
  → user selects a resolution
  → event becomes ready_to_publish
  → calendar publish writes to Google Calendar
```

## Idempotency

- Gmail message ID is the mailbox idempotency key.
- Conflict resolution uses an idempotency header.
- Publishing skips already published events.

## Documentation

- `docs/demo/backend-testing.md`
- `docs/demo/frontend-integration.md`
- `docs/openapi.yaml`
- `postman/LifeOS-Backend-Testing.postman_collection.json`
- `postman/LifeOS-Frontend-Integration.postman_collection.json`
