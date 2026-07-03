# ADR-0002 Google OAuth

## Status
Accepted

## Context
Users sign in with Google and grant access to Gmail scopes.

## Decision
Use Spring Security OAuth2 client for Google login. Handle the success callback in `OAuthSuccessHandler`, persist the user and backend-only Google tokens, then issue a LifeOS JWT to the client.

## Consequences
- Google access tokens never leave the backend
- Login is federated, but API access uses LifeOS JWTs
- OAuth handling stays separate from Gmail integration
