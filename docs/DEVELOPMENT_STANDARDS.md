# LifeOS Development Standards

This document establishes the standard workflow and deliverables for every feature branch in the LifeOS project.

## Feature Branch Lifecycle

Every feature branch must follow this workflow:

```
1. Implementation
   ↓
2. Unit Testing
   ↓
3. Integration Testing
   ↓
4. Documentation
   ↓
5. Pull Request
   ↓
6. Code Review
   ↓
7. Merge to Main
```

Documentation is part of the implementation, not a follow-up task.

---

## Definition of Done

A feature branch is considered complete only when all deliverables have been delivered.

See `COPILOT.md` → **Definition of Done** section.

---

## Documentation Structure

### Repository Layout

```
docs/

├── architecture/
│     [Architecture decision records]
│
├── development/
│     phase1-api-testing.md
│     phase2-api-testing.md
│     phase3-api-testing.md
│     phase4-api-testing.md
│     ...
│
├── postman/
│     phase1.postman_collection.json
│     phase2.postman_collection.json
│     phase3.postman_collection.json
│     phase4.postman_collection.json
│     ...
│
├── roadmap/
│     implementation-roadmap.md
│
├── templates/
│     phase-api-testing-template.md
│     postman-collection-template.json
│
└── copilot/
      [Copilot-specific guidance]
```

### API Testing Guide Format

Every phase includes: `docs/development/phaseX-api-testing.md`

**Template**: `docs/templates/phase-api-testing-template.md`

**Sections**:
1. Overview
2. What [Phase] Covers
3. Prerequisites
4. Quick Start
5. API Endpoints
6. Manual Testing Flow
7. Request Reference (table)
8. Using Postman
9. Troubleshooting
10. Files to Know
11. Next Steps

**Length**: 250-400 lines (concise but comprehensive)

**Style**:
- Clear, direct writing
- Code examples for every concept
- Complete curl commands
- Expected responses
- Testable scenarios

### Postman Collection Format

Every phase includes: `docs/postman/phaseX.postman_collection.json`

**Template**: `docs/templates/postman-collection-template.json`

**Requirements**:
- Include every endpoint introduced in the phase
- Use variables: `{{baseUrl}}` and `{{token}}`
- Include sample request bodies
- Include example responses where practical
- Organized into logical test groups (min 2-3 groups)
- At least 8-10 test requests per phase

**Variables**:
```
{{baseUrl}} = http://localhost:8080
{{token}} = <JWT bearer token>
```

---

## Code Quality Standards

### Implementation

- **Architecture**: Follow SOLID principles and Clean Architecture
- **Patterns**: Use appropriate design patterns (Strategy, Factory, Builder, etc.)
- **No Hardcoding**: All configuration via application.yml or environment variables
- **No Magic Numbers**: Use named constants
- **Naming**: Clear, descriptive variable and method names
- **Comments**: Only for non-obvious logic; code should be self-documenting

### Testing

- **Unit Tests**: Minimum 3+ tests per component
- **Coverage**: Focus on critical paths, not 100% coverage
- **Integration Tests**: Where appropriate (controllers, services)
- **Manual Testing**: Document test scenarios in API Testing Guide

### Exception Handling

- Centralized exception handling (use @ControllerAdvice)
- Never expose stack traces to clients
- Return meaningful error messages
- Log errors at appropriate levels (ERROR, WARN, INFO, DEBUG)

---

## Pull Request Standards

### Before Creating PR

1. ✅ Code compiles: `mvn clean compile`
2. ✅ Tests pass: `mvn test`
3. ✅ Package builds: `mvn clean package -DskipTests`
4. ✅ No SonarLint or SpotBugs violations
5. ✅ Documentation complete
6. ✅ Commit messages follow conventions

### PR Description Template

```markdown
## Overview
[What problem does this solve? What feature does this add?]

## Deliverables
- ✅ [Implementation deliverable]
- ✅ [Testing deliverable]
- ✅ [Documentation deliverable]

## How It Works
[Brief technical overview]

## In Scope
✅ [Feature 1]
✅ [Feature 2]

## Out of Scope
❌ [Future phase work 1]
❌ [Future phase work 2]

## Testing
[How to test this feature]

## Related
[Link to phase requirements or issues]
```

### PR Checklist

Every PR must verify:

- [ ] Scope aligns with current feature branch
- [ ] No out-of-scope features implemented
- [ ] Build succeeds
- [ ] All tests pass
- [ ] API Testing Guide created/updated
- [ ] Postman Collection created/updated
- [ ] Implementation Roadmap updated
- [ ] README updated (if required)
- [ ] Commit message explains *why*
- [ ] Co-authored-by trailer included

---

## Commit Message Standards

### Format

```
[Phase/Feature] Brief description of what changed

Longer explanation of:
- Why this change was necessary
- How it addresses the requirement
- Any important architectural decisions

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>
```

### Examples

✅ Good:
```
Phase 3 – LLM Foundation: Add multi-provider support

Implement provider-agnostic LLM layer supporting Gemini, Groq, and OpenRouter.
Uses Strategy pattern for provider abstraction and Factory for instantiation.
Configuration-driven provider selection via application.yml without code recompilation.

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>
```

❌ Poor:
```
Add LLM stuff
```

---

## Roadmap Updates

Every phase must update: `docs/roadmap/implementation-roadmap.md`

**Changes**:
1. Mark current phase as complete (✅)
2. Update "Current Status" section
3. Add completed deliverables list
4. Reference PR number
5. Update "Next Phase" with objectives

---

## Code Review Focus

Reviewers should verify:

1. **Scope**: Is this work in the current phase?
2. **Architecture**: Does it follow project patterns?
3. **Quality**: Is code clean and maintainable?
4. **Tests**: Are tests comprehensive?
5. **Documentation**: Is everything documented?
6. **Integration**: Does it integrate cleanly?

---

## When to Deviate

Deviations from these standards require explicit approval.

Examples:
- Adding dependencies
- Changing architectural patterns
- Skipping documentation
- Out-of-scope features

Ask first, implement second.

---

## Version History

| Date | Change |
|------|--------|
| 2026-07-04 | Initial standards document |
