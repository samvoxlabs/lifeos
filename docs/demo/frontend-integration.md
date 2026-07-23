# LifeOS Frontend Integration

This guide summarizes the frontend-facing API contract.

## Application APIs

- `POST /api/mail/sync`
- `POST /api/mail/review`
- `POST /api/mail/conflicts/{id}/resolve`
- `POST /api/calendar/publish`
- `GET /api/calendar/events`
- `GET /auth/google/scopes`

## Request shapes

### Sync mail

```json
{
  "mailbox": "parentosfamily@gmail.com",
  "cursor": null,
  "maxResults": 20
}
```

### Review schedule

```json
{
  "mailbox": "parentosfamily@gmail.com",
  "maxMessages": 20
}
```

### Resolve a conflict

```json
{
  "optionId": "delay-pickup",
  "delegateToEmail": "dad@example.com",
  "notifyRecipientEmail": "child@example.com"
}
```

### Google auth details

`GET /auth/google/scopes` returns the connected Google client id, project id/name, registration id, redirect URI, and granted scopes.

## How the frontend should use them

1. Sync Gmail into LifeOS storage.
2. Review the schedule to get extracted events and conflicts.
3. Resolve a conflict with a selected option.
4. Publish ready events to Google Calendar.
5. Refresh the calendar view.

## Notes

- The frontend should not call the developer APIs.
- The frontend should not implement rule engine, extraction, or conflict logic itself.
- Use `LifeOS-Frontend-Integration.postman_collection.json` for request payloads and quick checks.
