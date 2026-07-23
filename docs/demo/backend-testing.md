# LifeOS Backend Testing

This guide covers the demo backend flow end to end.

## Overview

The backend exposes the Gmail → AI → Calendar pipeline through developer APIs so each stage can be tested independently.

## Prerequisites

1. LifeOS running on `http://localhost:8080`
2. Valid JWT token
3. PostgreSQL running with Flyway migrations

## Authentication

```http
Authorization: Bearer {{jwt}}
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

## Demo flow

1. `POST /api/demo/gmail/reset`
   - clears the demo inbox
2. `POST /api/demo/gmail/load`
   - loads the chosen scenario
3. `POST /api/demo/gmail/messages`
   - inserts one email from the request body
4. `GET /api/demo/gmail/messages`
5. `POST /api/demo/events/extract`
   - returns extracted events and any conflicts found during the same pass
6. `GET /api/demo/db/events`
7. `GET /api/demo/db/conflicts`
8. `POST /api/demo/conflicts/{id}/resolve`
9. `POST /api/demo/calendar/publish`
   - only publishes events already resolved to `ready_to_publish`
10. `GET /api/demo/calendar/events`
11. `GET /api/demo/status`
12. `POST /api/demo/calendar/reset`
13. `POST /api/demo/gmail/reset`

## API coverage

- Gmail reset, load, and list
- Single-email Gmail insert from a request body
- Event extraction
- Database inspection
- Conflict resolution
- Calendar publish, list, and reset
- System status

## Curl examples

Prerequisite:

```text
http://localhost:8080/oauth2/authorization/google
```

In Postman, use **Authentication → Google OAuth Login** first, then reuse `{{token}}` for the demo requests.

```bash
curl -X POST "$BASE_URL/api/demo/gmail/reset" --oauth2-bearer "$TOKEN"
curl -X POST "$BASE_URL/api/demo/gmail/load" -H "Content-Type: application/json" -d '{"file":"airport-medical-conflict.json"}' --oauth2-bearer "$TOKEN"
curl -X POST "$BASE_URL/api/demo/gmail/messages" -H "Content-Type: application/json" -d '{"from":{"name":"City Medical Center","email":"appointments@citymedical.example"},"to":["parentosfamily@gmail.com"],"subject":"Appointment confirmed","snippet":"Your appointment is confirmed.","bodyText":"Your appointment is confirmed for July 28 at 3:30 PM.","receivedAt":"2026-07-26T14:29:00-05:00","labels":["INBOX"]}' --oauth2-bearer "$TOKEN"
curl -X GET "$BASE_URL/api/demo/gmail/messages" --oauth2-bearer "$TOKEN"
curl -X POST "$BASE_URL/api/demo/events/extract" -H "Content-Type: application/json" -d '{"mailbox":"parentosfamily@gmail.com","maxMessages":20}' --oauth2-bearer "$TOKEN"
curl -X POST "$BASE_URL/api/demo/conflicts/conflict-8359d702-3ba1-490f-a8cf-1060dc1d3e7e/resolve" -H "Content-Type: application/json" -d '{"optionId":"delay-pickup"}' --oauth2-bearer "$TOKEN"
curl -X POST "$BASE_URL/api/demo/calendar/publish" --oauth2-bearer "$TOKEN"
```

## Notes

- The backend collection is for validation and demos.
- It is safe to re-run reset/load steps; the workflow is idempotent where applicable.
- If `extract` returns no conflicts, load the `airport-medical-conflict` seed or add an overlapping calendar event first.
