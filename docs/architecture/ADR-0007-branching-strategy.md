# ADR-0007 Branching Strategy

## Status
Accepted

## Context
The repo needs a simple branching model that works for focused implementation work.

## Decision
Use short-lived feature branches from `main` or the active integration branch, keep changes scoped, and merge through reviewable pull requests.

## Consequences
- Easier review and rollback
- Lower risk of mixing unrelated changes
- Branch names should reflect the feature or fix
