# LifeOS Demo Docs

This folder is the demo entry point for local setup, backend testing, and frontend integration.

## Files

| File | Purpose |
|---|---|
| `local-setup.md` | Local environment, startup, and verification |
| `backend-testing.md` | Demo backend flow and test sequence |
| `frontend-integration.md` | Application API contract summary |
| `LifeOS-Backend-Testing.postman_collection.json` | Developer Postman collection |
| `LifeOS-Frontend-Integration.postman_collection.json` | Frontend Postman collection |

## Demo flow

1. Start the app locally.
2. Reset demo Gmail to clear the inbox.
3. Load a demo scenario.
4. Optionally insert a single email from a request body.
5. Extract events and detect conflicts in one pass.
6. Resolve a conflict.
7. Publish calendar events.
8. Verify the status summary.

## Notes

- Use the backend collection for the developer flow.
- Use the frontend collection for the app-facing API contract.
- Use `./start-app.sh` to launch the app; `./run.sh` is the lower-level script it wraps.
- `/api/demo/events/extract` returns both extracted events and any conflicts it detects.
- `/api/demo/calendar/publish` only publishes events that have already been resolved to `ready_to_publish`.
- Keep demo work in this folder going forward.
