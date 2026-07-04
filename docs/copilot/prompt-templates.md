# Prompt Templates

This document contains reusable prompt templates for GitHub Copilot Chat.

The project-wide engineering rules are defined in `COPILOT.md`.

Before using these templates, ensure you are on the correct feature branch.

---

# 1. New Feature Implementation

Use when starting a new feature.

```text
Current Branch:
feature/<branch-name>

Current Milestone:
Phase X - <Milestone Name>

Objective:
<Describe the feature>

Please perform the mandatory Scope Assessment first.

If any part of this request belongs to another milestone, stop and explain the architectural boundary before proposing implementation.

Do not generate code until the architecture has been reviewed and approved.
```

---

# 2. Architecture Review

Use before implementing a major feature.

```text
Review the proposed architecture.

Please evaluate:

- SOLID principles
- Clean Architecture
- Layer separation
- Package organization
- Future extensibility
- Technical debt

Recommend improvements only if they provide meaningful long-term value.

Remember this project is currently a Proof of Concept.
Avoid unnecessary enterprise complexity.
```

---

# 3. Code Review

Use after implementation.

```text
Review this implementation.

Check for:

- SOLID violations
- Layer violations
- Business logic in controllers
- DTO usage
- Exception handling
- Logging
- Naming consistency
- Testability

Identify:

Critical

High

Medium

Low

Recommend improvements only if justified.
```

---

# 4. Pull Request Review

```text
Review this Pull Request.

Check:

- Scope matches the feature branch
- No feature creep
- No unrelated refactoring
- Architecture consistency
- Readability
- Maintainability

Would you approve this Pull Request?
Explain why.
```

---

# 5. Refactoring

```text
Review this code.

Refactor only if:

- It improves maintainability.
- It reduces technical debt.
- It simplifies the implementation.
- It benefits the current milestone.

Avoid cosmetic refactoring.

Explain each proposed change.
```

---

# 6. API Design

```text
Design the REST APIs.

Requirements:

- DTOs only
- Consistent naming
- Proper HTTP methods
- Validation
- Clear request/response models

Explain the reasoning before implementation.
```

---

# 7. Database Design

```text
Review the database design.

Evaluate:

- Entity relationships
- Constraints
- Indexes
- Future scalability
- Simplicity

Do not over-engineer.

Remember this is currently a PoC.
```

---

# 8. AI Feature

```text
Current Branch:
feature/llm

Review the proposed AI implementation.

Ensure:

- AI remains provider-agnostic.
- Business logic is not inside the AI layer.
- Prompts remain external.
- Responses are structured JSON.
- Responsibilities are clearly separated.

Do not implement Gmail or Task logic unless requested.
```

---

# 9. Integration Feature

```text
Current Branch:
feature/google-gmail

Review the implementation.

Ensure:

- No business logic inside integrations.
- Data is normalized.
- Provider-specific code is isolated.
- Future providers can be added easily.
```

---

# 10. Testing

```text
Generate unit tests.

Focus on:

- Business logic
- Edge cases
- Failure scenarios

Avoid unnecessary mocking.

Use readable test names.
```

---

# 11. Documentation

```text
Review this feature.

Identify documentation that should be updated.

Examples:

- README
- Architecture
- Milestones
- Configuration
- API documentation
```

---

# 12. End-of-Feature Checklist

Before opening a Pull Request, ask Copilot:

```text
Review this feature before I create the Pull Request.

Verify:

✓ Scope is correct

✓ Architecture is consistent

✓ Build should succeed

✓ No unnecessary complexity

✓ No unrelated refactoring

✓ Documentation updated

✓ Ready for review
```

---

# General Reminder

Before every implementation:

1. Identify the current milestone.
2. Perform a Scope Assessment.
3. Explain the proposed design.
4. Wait for approval before implementing significant architectural changes.
5. Keep the implementation focused on the current feature branch.

Small, focused Pull Requests are preferred over large, multi-feature implementations.
