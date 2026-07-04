# Phase 6 - Domain and Persistence: API Testing Guide

## Overview

Phase 6 introduces a Persist First, Process Second backend flow. Every imported document is persisted as a `SourceDocument` first, then processed through Rule Engine + LLM, mapped to domain actions, and stored in PostgreSQL.

## What Phase 6 Covers

* SourceDocument as system of record
* Processing lifecycle: `NEW`, `PROCESSING`, `PROCESSED`, `FAILED`, `SKIPPED`
* Deduplication by `provider + sourceType + externalId`
* Extraction persistence and action mapping (`Task`, `Event`, `Reminder`)
* Retrieval endpoints for persisted actions

## Prerequisites

1. LifeOS running on `http://localhost:8080`
2. Valid JWT token
3. PostgreSQL available via `./run.sh`
4. Flyway migration `V6__domain_persistence.sql` applied

## Configuration

Datasource and Flyway use standard `application.yml` configuration.

Seed import support:

* Seed files can be placed under `src/main/resources/seed/*/*.json`
* Seed import runs only when `source_documents` is empty
* Imports remain idempotent due to deduplication constraints

## Authentication

All Phase 6 endpoints are protected. Include:

```http
Authorization: Bearer {{token}}
```

## Quick Start

### 1. Start the Application

```bash
./run.sh
```

The app will be available at `http://localhost:8080`.

### 2. Get Authentication Token

Phase 6 endpoints require JWT authentication.

Open in browser:

```text
http://localhost:8080/oauth2/authorization/google
```

After login, copy the `token` value from the JSON response.

### 3. Configure Environment Variables

```bash
export BASE_URL=http://localhost:8080
export TOKEN="<your JWT>"
```

### 4. Configure Postman (Optional)

Import the phase 6 Postman collection and set:

* `baseUrl = http://localhost:8080`
* `token = <your JWT>`

### 5. Test the Domain Pipeline

Use curl or Postman to validate process and retrieval endpoints.

## API Overview

### POST `/api/domain/process`

Input: `ProcessExtractionRequest`

Flow:

1. Persist SourceDocument first.
2. If `extractionResult` is provided, use it directly.
3. If `extractionResult` is omitted, run Rule Engine + LLM pipeline from persisted SourceDocument.
4. Persist Extraction and mapped actions.
5. Return DTO response.

### GET `/api/tasks`

Returns persisted task actions.

### GET `/api/events`

Returns persisted event actions.

### GET `/api/reminders`

Returns persisted reminder actions.

## Request Example

```json
{
  "sourceDocument": {
    "id": "doc-1",
    "sender": "school@example.com",
    "subject": "Parent teacher meeting",
    "content": "Meeting next week",
    "labels": ["school"],
    "priority": "1",
    "source": "email",
    "provider": "google",
    "sourceType": "gmail",
    "externalId": "msg-1",
    "rawContent": "Meeting next week",
    "metadata": {
      "model": "gemini-2.5-flash",
      "llmProvider": "google",
      "promptVersion": "v1"
    }
  },
  "extractionResult": {
    "summary": "Meeting and follow up",
    "confidence": 0.95,
    "actions": [
      {
        "type": "TASK",
        "title": "Confirm attendance",
        "description": "Reply to teacher"
      },
      {
        "type": "EVENT",
        "title": "Parent teacher meeting",
        "description": "At school"
      },
      {
        "type": "REMINDER",
        "title": "Bring documents",
        "description": "Bring report card"
      }
    ]
  }
}
```

## Success Response Example

```json
{
  "sourceDocument": {
    "id": "f9d5ef87-2b0e-4a70-986b-8cf6f3a68a1a",
    "externalId": "msg-1",
    "provider": "google",
    "sourceType": "gmail",
    "receivedAt": "2026-07-04T12:00:00",
    "createdAt": "2026-07-04T12:00:00",
    "updatedAt": "2026-07-04T12:00:01",
    "processingStatus": "PROCESSED"
  },
  "extraction": {
    "id": "2df6fbe8-e1b6-49f1-b9cf-b1cc6b7dc56f",
    "summary": "Meeting and follow up",
    "confidence": 0.95,
    "model": "gemini-2.5-flash",
    "provider": "google",
    "promptVersion": "v1"
  },
  "actions": [
    {
      "id": "f2aab8ca-7a6e-4f4f-a4cb-bf95b8f9f829",
      "type": "TASK",
      "title": "Confirm attendance",
      "description": "Reply to teacher",
      "status": "OPEN",
      "confidence": 0.95
    }
  ]
}
```

## Skipped Response Example

```json
{
  "sourceDocument": {
    "id": "f9d5ef87-2b0e-4a70-986b-8cf6f3a68a1a",
    "externalId": "msg-2",
    "provider": "google",
    "sourceType": "gmail",
    "receivedAt": "2026-07-04T12:10:00",
    "createdAt": "2026-07-04T12:10:00",
    "updatedAt": "2026-07-04T12:10:01",
    "processingStatus": "SKIPPED"
  },
  "extraction": null,
  "actions": []
}
```

## Error Response Example

```json
{
  "timestamp": "2026-01-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "sourceDocument.provider is required",
  "path": "/api/domain/process"
}
```

## Complete curl Commands

### Successful persistence with explicit extraction result

```bash
curl -X POST "$BASE_URL/api/domain/process" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceDocument": {
      "id": "doc-1",
      "sender": "school@example.com",
      "subject": "Parent teacher meeting",
      "content": "Meeting next week",
      "labels": ["school"],
      "priority": "1",
      "source": "email",
      "provider": "google",
      "sourceType": "gmail",
      "externalId": "msg-1",
      "rawContent": "Meeting next week",
      "metadata": {
        "model": "gemini-2.5-flash",
        "llmProvider": "google",
        "promptVersion": "v1"
      }
    },
    "extractionResult": {
      "summary": "Meeting and follow up",
      "confidence": 0.95,
      "actions": [
        {"type":"TASK","title":"Confirm attendance","description":"Reply to teacher"},
        {"type":"EVENT","title":"Parent teacher meeting","description":"At school"},
        {"type":"REMINDER","title":"Bring documents","description":"Bring report card"}
      ]
    }
  }'
```

### Automatic processing path (no extractionResult)

```bash
curl -X POST "$BASE_URL/api/domain/process" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceDocument": {
      "id": "doc-2",
      "sender": "important@school.org",
      "subject": "Meeting reminder",
      "content": "Meeting next Friday at 3 PM",
      "labels": ["school"],
      "priority": "high",
      "source": "email",
      "provider": "google",
      "sourceType": "gmail",
      "externalId": "msg-2",
      "rawContent": "Meeting next Friday at 3 PM",
      "metadata": {}
    }
  }'
```

### Retrieval endpoints

```bash
curl -X GET "$BASE_URL/api/tasks" -H "Authorization: Bearer YOUR_JWT_TOKEN"
curl -X GET "$BASE_URL/api/events" -H "Authorization: Bearer YOUR_JWT_TOKEN"
curl -X GET "$BASE_URL/api/reminders" -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Expected Responses

* `200 OK` for successful persistence and retrieval
* `200 OK` with `processingStatus = SKIPPED` when rules ignore
* `400 Bad Request` for invalid request payloads
* `500 Internal Server Error` for provider or persistence failures

## Error Scenarios

* Missing `provider`, `sourceType`, or `externalId`
* Missing extraction summary when `extractionResult` is provided
* Unsupported action type
* LLM/provider timeout or processing failure

## Troubleshooting

* If requests return `401`, confirm the JWT token is valid.
* If documents are unexpectedly skipped, inspect Rule Engine behavior for the source payload.
* If duplicate source records appear, verify `provider + sourceType + externalId` values are identical.
* If app startup fails, run `./run.sh` and verify Docker PostgreSQL/Flyway startup logs.

## Files Added/Modified

* `src/main/java/com/familyos/familyos/domain/`
* `src/main/resources/db/migration/V6__domain_persistence.sql`
* `src/test/java/com/familyos/familyos/domain/`
* `docs/development/phase6-api-testing.md`
* `docs/postman/phase6.postman_collection.json`
* `docs/postman/FamilyOS_API.postman_collection.json`
* `docs/roadmap/implementation-roadmap.md`

## Next Phase

Phase 7 - Frontend APIs
