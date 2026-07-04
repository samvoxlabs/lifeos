# AI Development Guide

## Purpose

This document defines how Artificial Intelligence (LLM) should be integrated into LifeOS.

The goal is to ensure that AI remains a reusable, provider-agnostic component responsible only for **understanding information**, while the application remains responsible for **business logic and user experience**.

This project is currently a **Proof of Concept (PoC)**. Favor simplicity, readability, and modularity over production-level complexity.

---

# Core Principles

The AI layer is **not** the application's business logic.

The AI should:

* Understand unstructured data.
* Extract meaningful information.
* Return structured JSON.
* Never make business decisions.
* Never access the database.
* Never know about UI components.
* Never know about REST APIs.

Business logic always belongs to the backend.

---

# AI Processing Pipeline

```
External Source

        │

        ▼

Document Normalizer

        │

        ▼

Rule Engine

        │

        ▼

LLM Extraction

        │

        ▼

Structured Extraction

        │

        ▼

Domain Mapper

        │

        ▼

Persistence

        │

        ▼

REST APIs
```

Each component has a single responsibility.

---

# Responsibilities

## External Integrations

Examples:

* Gmail
* Google Calendar
* Google Drive
* Microsoft Graph

Responsibilities:

* Connect to external APIs.
* Retrieve data.
* Normalize data.
* No business logic.
* No AI logic.

---

## Document Normalizer

Convert provider-specific data into a common format.

Examples:

* Gmail Message
* Outlook Email
* Google Document
* Calendar Event

↓

NormalizedDocument

The LLM should never receive provider-specific objects.

---

## Rule Engine

Purpose:

Determine whether the document should be processed by AI.

Examples:

Ignore:

* Promotions
* Spam
* Social notifications
* OTP emails

Process:

* School emails
* Medical appointments
* Financial reminders
* Travel confirmations

The Rule Engine should be deterministic and inexpensive.

---

## LLM Extraction

The LLM receives:

* NormalizedDocument
* Prompt Template

The LLM returns:

* Structured JSON only

Never prose.

Never Markdown.

Never explanations.

---

# LLM Input

The input should be provider-independent.

Example:

```
NormalizedDocument

Source

Metadata

Content

Attachments
```

The LLM should never receive:

* OAuth tokens
* Database IDs
* Gmail API objects
* Internal entities

---

# LLM Output

The output represents extracted facts.

Example:

```
Summary

Actions

Confidence
```

Example actions:

* TASK
* EVENT
* REMINDER
* FOLLOW_UP
* NOTE

The LLM must not return application-specific entities.

---

# Domain Mapping

The backend converts extracted actions into domain objects.

Example:

```
LLM

↓

Action Candidate

↓

Task

↓

Reminder

↓

Dashboard
```

Business rules belong here.

Examples:

* Deduplication
* Priority
* User preferences
* Reminder scheduling

---

# Persistence Strategy

The system should persist three logical layers.

## Source Layer

Original document.

Examples:

* Gmail Message
* Calendar Event
* Drive File

This is the source of truth.

---

## Intelligence Layer

AI extraction result.

Stores:

* Summary
* Structured JSON
* Confidence
* Prompt version
* Model used

This allows future reprocessing.

---

## Domain Layer

Application entities.

Examples:

* Tasks
* Events
* Reminders
* Follow-ups

These are exposed to the frontend.

---

# Provider Agnostic Design

Never couple AI to a specific provider.

The AI should process:

```
NormalizedDocument
```

not

```
GmailMessage
```

Future providers should require only a new normalizer.

---

# LLM Abstraction

Always program against interfaces.

Example:

```
DocumentExtractionService

↓

LlmClient

↓

Gemini

OpenAI

Claude

Local Model
```

The application should never depend directly on a specific LLM provider.

---

# Prompt Management

Prompts should be stored separately from Java code.

Example:

```
resources/prompts/

email-extraction.txt

calendar-extraction.txt

generic-document.txt
```

Prompts should be versioned and easy to update.

---

# Error Handling

Handle gracefully:

* Invalid JSON
* Timeout
* Rate limiting
* Provider unavailable
* Low confidence responses

The application should continue functioning even if AI extraction fails.

---

# Logging

Log:

* Provider
* Model
* Request latency
* Processing time
* Success/failure

Never log:

* OAuth tokens
* API keys
* Full email contents at INFO level
* Personally sensitive information

---

# Testing

The AI layer should be testable independently.

Use:

* Mock LLM responses
* Sample documents
* Golden JSON responses

Avoid requiring live LLM calls for unit tests.

---

# Scope Boundaries

The AI layer should never:

* Save to the database.
* Create Tasks.
* Create Events.
* Create Reminders.
* Call repositories.
* Send notifications.
* Schedule jobs.

These responsibilities belong to the domain layer.

---

# Development Philosophy

When adding AI features:

1. Keep the design provider-agnostic.
2. Keep prompts separate from code.
3. Return structured JSON only.
4. Keep business logic out of the AI layer.
5. Optimize for maintainability and experimentation.
6. Avoid over-engineering while the project remains a PoC.

The AI should answer one question only:

> **"What information exists in this document?"**

The backend answers the next question:

> **"What should LifeOS do with that information?"**
