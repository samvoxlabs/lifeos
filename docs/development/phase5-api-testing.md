# Phase 5 – AI Extraction: API Testing Guide

## Overview

Phase 5 adds a provider-agnostic extraction pipeline that evaluates a `NormalizedDocument`, applies the Rule Engine first, and only then invokes the configured LLM when processing is allowed.

## What Phase 5 Covers

* Rule Engine gating before LLM invocation
* Prompt loading from external templates
* Structured JSON extraction into strongly typed DTOs
* One REST endpoint for document processing

## Prerequisites

1. LifeOS running on `http://localhost:8080`
2. Valid JWT token
3. Configured LLM provider in `application.yml`
4. PostgreSQL available via `./run.sh`

## Configuration

Use `application.yml` to select the active provider:

```yaml
llm:
  provider: gemini
```

Provider settings for API key, base URL, and model are read from configuration properties. No Java code changes are needed when switching providers.

## Authentication

The extraction endpoint is protected. Include:

```http
Authorization: Bearer {{token}}
```

## Quick Start

```bash
./run.sh
```

Then import the phase 5 Postman collection and set:

* `baseUrl = http://localhost:8080`
* `token = <your JWT>`

## API Overview

### POST `/api/extraction/process`

Input: `NormalizedDocument`

Flow:

1. Evaluate the Rule Engine.
2. If the decision is `PROCESS`, build the prompt and invoke the LLM.
3. Parse the JSON response into `ExtractionResult`.
4. Return a skipped response when the Rule Engine does not allow processing.

## Request Example

```json
{
  "id": "doc-1",
  "sender": "John Doe",
  "subject": "Parent-teacher meeting",
  "content": "The meeting is next Friday at 3 PM.",
  "labels": ["school"],
  "priority": "1",
  "source": "email"
}
```

## Success Response Example

```json
{
  "status": "SUCCESS",
  "result": {
    "summary": "Parent-teacher meeting next Friday.",
    "confidence": 0.96,
    "actions": [
      {
        "type": "EVENT",
        "title": "Parent-teacher meeting",
        "description": "Meet with teacher",
        "dueDate": "2026-08-15T15:00:00"
      }
    ]
  },
  "ruleResult": null,
  "message": null
}
```

## Skipped Response Example

```json
{
  "status": "SKIPPED",
  "result": null,
  "ruleResult": {
    "decision": "IGNORE",
    "matchedRule": "default",
    "reason": "Marked as spam",
    "priorityScore": 0
  },
  "message": "Document skipped by Rule Engine: IGNORE"
}
```

## Error Response Example

```json
{
  "status": "ERROR",
  "result": null,
  "ruleResult": null,
  "message": "Extraction failed: Failed to parse LLM response as JSON: ..."
}
```

## Complete curl Commands

### Successful extraction

```bash
curl -X POST http://localhost:8080/api/extraction/process \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "id":"doc-1",
    "sender":"John Doe",
    "subject":"Parent-teacher meeting",
    "content":"The meeting is next Friday at 3 PM.",
    "labels":["school"],
    "priority":"1",
    "source":"email"
  }'
```

### Skipped by Rule Engine

```bash
curl -X POST http://localhost:8080/api/extraction/process \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "id":"doc-2",
    "sender":"Spam Sender",
    "subject":"Buy now",
    "content":"Special offer just for you.",
    "labels":["spam"],
    "priority":"0",
    "source":"email"
  }'
```

### Error scenario

```bash
curl -X POST http://localhost:8080/api/extraction/process \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "id":"doc-3",
    "sender":"Test",
    "subject":"Test",
    "content":"Test",
    "labels":[],
    "priority":"1",
    "source":"email"
  }'
```

## Expected Responses

* `SUCCESS` when the Rule Engine allows processing and the LLM returns valid JSON
* `SKIPPED` when the Rule Engine does not allow processing
* `ERROR` when JSON parsing, provider errors, timeouts, or prompt loading fail

## Error Scenarios

* Invalid JSON from the provider
* Provider timeout
* Unsupported provider name
* Missing API key or base URL
* Prompt template loading failure

## Troubleshooting

* If requests return `401`, confirm the JWT token is valid.
* If requests return `SKIPPED`, inspect the Rule Engine decision.
* If requests return `ERROR`, check logs for provider or parsing issues.
* If the app does not start, run `./run.sh` and verify Docker is running.

## Files Added/Modified

* `src/main/java/com/familyos/familyos/extraction/`
* `src/main/resources/prompts/email-extraction.txt`
* `src/test/java/com/familyos/familyos/extraction/`
* `docs/development/phase5-api-testing.md`
* `docs/postman/phase5.postman_collection.json`
* `docs/FamilyOS_API.postman_collection.json`
* `docs/roadmap/implementation-roadmap.md`

## Next Phase

Phase 6 – Domain & Persistence
