# Phase 7 - Frontend & Orchestration APIs: API Testing Guide

## Overview

Phase 7 exposes frontend-facing domain APIs and adds a single orchestration endpoint (`POST /api/sync`) that runs end-to-end synchronization and processing using existing services.

## Architecture

```text
Google
  -> NormalizedDocument
  -> SourceDocument
  -> Rule Engine
  -> LLM
  -> ExtractionResult
  -> ActionMapper
  -> Task/Event/Reminder
  -> REST APIs
  -> Frontend
```

## Sync Orchestration

Primary frontend sync endpoint:

* `POST /api/sync`

Internal partial sync endpoints:

* `POST /api/google/sync/gmail`
* `POST /api/google/sync/calendar`
* `POST /api/google/sync/drive`

## Prerequisites

1. LifeOS running on `http://localhost:8080`
2. Valid JWT token
3. PostgreSQL running with Flyway migrations

## Authentication

```http
Authorization: Bearer {{token}}
```

## Quick Start

### 1. Start app

```bash
./run.sh
```

### 2. Login

Open:

```text
http://localhost:8080/oauth2/authorization/google
```

### 3. Set variables

```bash
export BASE_URL=http://localhost:8080
export TOKEN="<your JWT>"
```

### 4. Frontend integration flow

```text
Login
  -> POST /api/sync
  -> GET /api/dashboard
  -> GET /api/tasks
  -> GET /api/events
  -> GET /api/reminders
  -> GET /api/timeline
  -> GET /api/search?q=...
```

## API Overview

* `POST /api/sync`
* `POST /api/google/sync/gmail`
* `POST /api/google/sync/calendar`
* `POST /api/google/sync/drive`
* `GET /api/dashboard`
* `GET /api/tasks`
* `GET /api/tasks/{id}`
* `GET /api/events`
* `GET /api/events/{id}`
* `GET /api/reminders`
* `GET /api/reminders/{id}`
* `GET /api/timeline`
* `GET /api/search?q=...`
* `GET /api/source-documents`
* `GET /api/source-documents/{id}`

## Curl Commands

### Sync

```bash
curl -X POST "$BASE_URL/api/sync" --oauth2-bearer "$TOKEN"
curl -X POST "$BASE_URL/api/google/sync/gmail" --oauth2-bearer "$TOKEN"
curl -X POST "$BASE_URL/api/google/sync/calendar" --oauth2-bearer "$TOKEN"
curl -X POST "$BASE_URL/api/google/sync/drive" --oauth2-bearer "$TOKEN"
```

### Frontend reads

```bash
curl -X GET "$BASE_URL/api/dashboard" --oauth2-bearer "$TOKEN"
curl -X GET "$BASE_URL/api/tasks?page=0&size=20&sort=createdAt,desc" --oauth2-bearer "$TOKEN"
curl -X GET "$BASE_URL/api/events?page=0&size=20&sort=createdAt,asc" --oauth2-bearer "$TOKEN"
curl -X GET "$BASE_URL/api/reminders?page=0&size=20&sort=createdAt,desc" --oauth2-bearer "$TOKEN"
curl -X GET "$BASE_URL/api/timeline" --oauth2-bearer "$TOKEN"
curl -X GET "$BASE_URL/api/search?q=meeting" --oauth2-bearer "$TOKEN"
curl -X GET "$BASE_URL/api/source-documents?page=0&size=20&sort=createdAt,desc" --oauth2-bearer "$TOKEN"
```

## Response Examples

### Sync summary

```json
{
  "status": "COMPLETED",
  "documentsRead": 42,
  "documentsImported": 6,
  "documentsSkipped": 36,
  "documentsProcessed": 5,
  "tasksCreated": 4,
  "eventsCreated": 1,
  "remindersCreated": 2,
  "processingTimeMs": 2150
}
```

### Paginated response

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "sort": "createdAt: DESC"
}
```

## Error Scenarios

* `401` invalid/missing JWT
* `404` entity not found
* `400` invalid filters or invalid date range
* `500` provider access failures or unexpected pipeline failures

## Troubleshooting

* If `/api/sync` imports nothing, verify connected Google account and permissions.
* If reads are empty, confirm `/api/sync` or `/api/domain/process` has persisted data.
* If filtering fails, verify ISO date format and valid filter values.

## Next Phase

Phase 8 - Frontend Application
