# Phase X – [Feature Name]: API Testing Guide

> This is a template for Phase X API testing guides. Copy this file to `docs/development/phaseX-api-testing.md` and customize for your phase.

## Overview

[1-2 sentence overview of what this phase accomplishes]

## What Phase X Covers

**[Feature Name]** [brief description]:

* [Key feature 1]
* [Key feature 2]
* [Key feature 3]

## Prerequisites

1. **Running Application**: LifeOS backend must be running on `http://localhost:8080`
2. **JWT Token**: Valid authentication token (see OAuth section below)
3. **Postman** or `curl`: For API testing
4. **Database** (if required): PostgreSQL running and initialized

## Quick Start

### Step 1: Start the Application

```bash
cd /path/to/familyos
mvn spring-boot:run
```

### Step 2: Authenticate (if required)

**Via Browser:**
```
http://localhost:8080/oauth2/authorization/google
```

After login, copy the `token` field from the JSON response.

**Via curl:**
```bash
GOOGLE_AUTH_URL="http://localhost:8080/oauth2/authorization/google"
curl -v $GOOGLE_AUTH_URL 2>&1 | grep -i "set-cookie"
```

### Step 3: Test the Feature

Use curl or Postman to test endpoints. Examples below.

---

## API Endpoints

### Endpoint 1: [Name]

**HTTP Method**: [GET/POST/PUT/DELETE]

**URL**: `/api/[path]`

**Authentication**: [Required/Not required]

**Request Body** (if applicable):
```json
{
  "field1": "value1",
  "field2": "value2"
}
```

**Response** (Success):
```json
{
  "status": "success",
  "data": { }
}
```

**Status Code**: 200 OK

---

### Endpoint 2: [Name]

[Repeat pattern for each endpoint]

---

## Manual Testing Flow

### Test Case 1: [Scenario Description]

**Scenario**: [What should happen]

**Request**:
```bash
curl -X POST http://localhost:8080/api/[endpoint] \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "field1": "value1",
    "field2": "value2"
  }'
```

**Expected Response**:
```json
{
  "status": "success"
}
```

**Status**: 200 OK

---

### Test Case 2: [Another Scenario]

[Repeat for each test scenario]

---

## Request Reference

| Endpoint | Method | Auth | Purpose |
|----------|--------|------|---------|
| `/api/[endpoint1]` | POST | Yes | [Description] |
| `/api/[endpoint2]` | GET | Yes | [Description] |

---

## Using Postman

1. **Import Collection**: `docs/postman/phaseX.postman_collection.json`

2. **Set Environment Variables**:
   - Right-click collection → Edit
   - Add variable: `token` = `<your-jwt-token>`
   - Add variable: `baseUrl` = `http://localhost:8080`

3. **Add Authorization Header**:
   - All requests include: `Authorization: Bearer {{token}}`

4. **Run Requests**: Execute each test case in sequence

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

**Solution**:
- Verify all required fields are present
- Check field types and formats
- Validate no extra/unexpected fields

### 500 Internal Server Error

**Problem**: Server-side error

**Solution**:
- Check application logs: `mvn spring-boot:run`
- Verify database is running (if required)
- Ensure configuration is correct

---

## Files to Know

* **Implementation**: `src/main/java/com/familyos/familyos/[package]/`
* **Tests**: `src/test/java/com/familyos/familyos/[package]/`
* **Postman Collection**: `docs/postman/phaseX.postman_collection.json`
* **API Testing Guide**: This file

---

## Next Steps

After Phase X testing:

1. [Verification step 1]
2. [Verification step 2]
3. Ready for [Next Phase Name]
