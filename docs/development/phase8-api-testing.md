# Phase 8 - Mailbox Sync and Conflict APIs

## Endpoints

* `POST /mail/sync`
* `GET /mail/messages?limit=20&cursor=<cursor>`
* `GET /mail/messages/{messageId}`
* `PATCH /mail/messages/{messageId}`
* `POST /mail/conflicts/{conflictId}/resolve`

All endpoints require:

```http
Authorization: Bearer <JWT>
```

## 1) Sync mailbox

```http
POST /mail/sync
Content-Type: application/json
```

Request:

```json
{
  "mailbox": "parentosfamily@gmail.com",
  "cursor": null,
  "maxResults": 20
}
```

Response shape:

```json
{
  "mailbox": "parentosfamily@gmail.com",
  "syncedAt": "2026-07-22T14:30:00-05:00",
  "nextCursor": "gmail-history-id-12345",
  "messages": [],
  "events": [],
  "conflicts": []
}
```

Behavior:

* Uses authenticated user’s Google OAuth credentials.
* Uses Gmail history cursor for incremental refresh.
* Deduplicates by Gmail message ID per account.
* Returns normalized message data only (no OAuth token data).
* If no actionable event exists, message is still returned with `processingStatus = no_actionable_event`.

## 2) List messages

```http
GET /mail/messages?limit=20&cursor=<cursor>
```

Response:

```json
{
  "items": [],
  "nextCursor": null
}
```

## 3) Get one message

```http
GET /mail/messages/{messageId}
```

Returns one normalized message object.

## 4) Mark read/unread

```http
PATCH /mail/messages/{messageId}
Content-Type: application/json
```

Request:

```json
{
  "read": true
}
```

## 5) Resolve conflict

```http
POST /mail/conflicts/{conflictId}/resolve
Content-Type: application/json
Idempotency-Key: conflict-001-delay-pickup
```

Request:

```json
{
  "optionId": "delay-pickup"
}
```

Response shape:

```json
{
  "conflictId": "conflict-<uuid>",
  "status": "resolved",
  "selectedOptionId": "delay-pickup",
  "updatedEvents": [],
  "notifications": [],
  "resolvedAt": "2026-07-22T14:37:00-05:00"
}
```

Behavior:

* Resolution is explicit user action only.
* Idempotency supported via `Idempotency-Key`.
* Duplicate idempotency key returns the same logical outcome.

## Error contract

All `/mail` endpoints return:

```json
{
  "error": {
    "code": "GMAIL_AUTH_EXPIRED",
    "message": "The Gmail connection has expired.",
    "retryable": false,
    "requestId": "request-123"
  }
}
```

Common codes:

* `GMAIL_NOT_CONNECTED`
* `GMAIL_AUTH_EXPIRED`
* `GMAIL_RATE_LIMITED`
* `MAIL_SYNC_FAILED`
* `MESSAGE_PARSE_FAILED`
* `CONFLICT_NOT_FOUND`
* `CONFLICT_ALREADY_RESOLVED`
* `INVALID_RESOLUTION_OPTION`
* `CALENDAR_UPDATE_FAILED`
* `NOTIFICATION_FAILED`
