# Phase 4 – Rule Engine: API Testing Guide

## Overview

This guide helps you test the Rule Engine implementation for Phase 4.

## What Phase 4 Covers

**Rule Engine** is a configurable decision-making engine that:

* Evaluates normalized documents
* Determines if a document should be processed by the LLM
* Applies priority-based rule evaluation
* Returns structured decisions without invoking the LLM

## Prerequisites

1. **Docker**: For PostgreSQL database (optional, auto-started by `run.sh`)
2. **Postman** or `curl`: For API testing
3. Valid JWT token (see [Authentication](#authentication) section)

## Quick Start

### Step 1: Start the Application

Run the startup script from the project root:

```bash
./run.sh
```

This script will:
- ✅ Check if PostgreSQL container is running (create/start if needed)
- ✅ Verify database connectivity
- ✅ Kill any existing process on port 8080
- ✅ Start the FamilyOS application

The app will be available at `http://localhost:8080`

### Step 2: Get Authentication Token

All `/api/rules/*` endpoints require JWT Bearer authentication.

**Option A: Via Browser (Recommended)**
```
http://localhost:8080/oauth2/authorization/google
```

After login, copy the `token` field from the JSON response. Use it as your JWT token.

**Option B: From test data**
If you have a valid token from previous sessions, reuse it (tokens don't expire instantly).

### Step 3: Test the Rule Engine

Use curl or Postman with your JWT token to test the Rule Engine endpoints.

---

## API Endpoints

### POST /api/rules/evaluate

Evaluates a normalized document against all registered rules.

**Authentication**: Required (Bearer token)

**Request Body**:
```json
{
  "id": "doc1",
  "sender": "Lincoln Elementary School",
  "subject": "Permission Slip Required",
  "content": "Please sign and return the permission slip",
  "labels": [],
  "priority": "high",
  "source": "email"
}
```

**Response**:
```json
{
  "decision": "PROCESS",
  "matchedRule": "SenderRule",
  "reason": "Matched important sender: school",
  "priorityScore": 95
}
```

---

## Manual Testing Flow

### Test Case 1: SenderRule Match

**Scenario**: Email from important sender (school, hospital, bank, etc.)

**Request**:
```bash
curl -X POST http://localhost:8080/api/rules/evaluate \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "doc1",
    "sender": "Lincoln Elementary School",
    "subject": "Permission Slip",
    "content": "Please sign and return immediately",
    "labels": [],
    "priority": "high",
    "source": "email"
  }'
```

**Expected Response**:
- `decision`: `PROCESS`
- `matchedRule`: `SenderRule`
- `priorityScore`: `95`

---

### Test Case 2: LabelRule Match

**Scenario**: Email with ignore label (promotions, social, spam)

**Request**:
```bash
curl -X POST http://localhost:8080/api/rules/evaluate \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "doc2",
    "sender": "Marketing Company",
    "subject": "Special Offer",
    "content": "Limited time discount",
    "labels": ["promotions"],
    "priority": "low",
    "source": "email"
  }'
```

**Expected Response**:
- `decision`: `IGNORE`
- `matchedRule`: `LabelRule`
- `priorityScore`: `10`

---

### Test Case 3: KeywordRule Match

**Scenario**: Email containing action keywords (appointment, payment, invoice, etc.)

**Request**:
```bash
curl -X POST http://localhost:8080/api/rules/evaluate \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "doc3",
    "sender": "Dr. Smith Medical Office",
    "subject": "Appointment Reminder",
    "content": "Your appointment is scheduled for Friday at 2 PM",
    "labels": [],
    "priority": "high",
    "source": "email"
  }'
```

**Expected Response**:
- `decision`: `PROCESS`
- `matchedRule`: `KeywordRule`
- `priorityScore`: `80`

---

### Test Case 4: Default Decision

**Scenario**: Document matches no rules

**Request**:
```bash
curl -X POST http://localhost:8080/api/rules/evaluate \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "doc4",
    "sender": "Random Person",
    "subject": "Just saying hi",
    "content": "How have you been lately?",
    "labels": [],
    "priority": "normal",
    "source": "email"
  }'
```

**Expected Response**:
- `decision`: `LOW_PRIORITY`
- `matchedRule`: `DefaultRule`
- `priorityScore`: `50`

---

## Rule Priority Order

Rules execute in ascending priority order. First match returns immediately.

| Priority | Rule | Decision | Score |
|----------|------|----------|-------|
| 10 | SenderRule | PROCESS | 95 |
| 20 | LabelRule | IGNORE | 10 |
| 30 | KeywordRule | PROCESS | 80 |
| — | DefaultRule | LOW_PRIORITY | 50 |

---

## RuleDecision Enum

```
PROCESS        → Process document through LLM
IGNORE         → Skip document
LOW_PRIORITY   → Process later
MANUAL_REVIEW  → Requires human review
```

---

## Using Postman

1. **Import Collection**: `docs/postman/phase4.postman_collection.json`

2. **Set Environment Variables**:
   - Right-click collection → Edit
   - Add variable: `token` = `<your-jwt-token>`

3. **Add Authorization Header**:
   - All requests include: `Authorization: Bearer {{token}}`

4. **Run Requests**: Execute each test case

---

## Troubleshooting

### 401 Unauthorized

**Problem**: Missing or invalid JWT token

**Solution**:
1. Authenticate at `http://localhost:8080/oauth2/authorization/google`
2. Copy token from response
3. Include in all requests: `-H "Authorization: Bearer <token>"`

### 400 Bad Request

**Problem**: Invalid request body

**Verify**:
- All required fields present: `id`, `sender`, `subject`, `content`, `labels`, `priority`, `source`
- Labels must be an array (can be empty: `[]`)
- No extra fields unless intentional

### Request Body Missing Content

**Ensure**:
```json
{
  "id": "unique-id",
  "sender": "sender@example.com or name",
  "subject": "email subject",
  "content": "email body",
  "labels": ["label1", "label2"],
  "priority": "high|normal|low",
  "source": "email"
}
```

---

## Files to Know

* **Implementation**: `src/main/java/com/familyos/familyos/ruleengine/`
* **Tests**: `src/test/java/com/familyos/familyos/ruleengine/`
* **Postman Collection**: `docs/postman/phase4.postman_collection.json`

---

## Next Steps

After Phase 4 testing:

1. Verify all four rule types work correctly
2. Confirm priority-based execution
3. Test default decision when no rules match
4. Ready for Phase 5 – AI Extraction (applies rules before LLM)
