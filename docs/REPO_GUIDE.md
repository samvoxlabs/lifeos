# Repository Guide — Why these files exist

This short guide explains the purpose of the small onboarding/automation files that were added to the repository and why they live under `docs/` or at the repository root. The goal is to help new contributors quickly understand what each file does and why it matters.

Quick checklist
- Where to find canonical docs: `docs/` (read `docs/intellijSetup.md`, `docs/localSetup.md`, `docs/HELP.md`, `docs/development/phase1-developer-guide.md`, `docs/ROADMAP.md`)
- Important contributor files (root): `README.md`, `CONTRIBUTING.md`, `.editorconfig`, `.gitattributes`
- Automation: CI workflow (`.github/workflows/ci.yml`) and Dependabot (`.github/dependabot.yml`)

Why keep docs in `docs/` (short)
- Discoverability: top-level `docs/` is immediately visible on Git host pages and is the expected place for human-facing guides.
- Separation of concerns: docs are separate from source code (`src/`) and won't be mistaken for code or packaged into builds.
- Reusability & publishing: easier to publish via GitHub Pages or reuse in other places.

Files and why they exist

- `README.md` (root)
  - Purpose: the primary entry point for the repository. Contains a short overview, quick-start commands, and links to the canonical docs.
  - Why important: Helps visitors understand the project quickly without digging into source code.

- `docs/` (directory)
  - Purpose: host user-focused guides and onboarding material (IDE setup, local setup, help/reference docs).
  - Why important: central place for guides; short relative links are easier to maintain.

- `CONTRIBUTING.md` (root)
  - Purpose: explains how to contribute, branch naming conventions, how to run and test locally, and the PR checklist.
  - Why important: reduces friction for new contributors and sets expectations for maintainers and contributors.

- `.editorconfig` (root)
  - Purpose: a lightweight cross-editor file to enforce line endings, indentation, trailing whitespace, and final newline rules.
  - Why important: produces consistent diffs and reduces style-related PR noise across platforms and editors.

- `.gitattributes` (root)
  - Purpose: control how Git treats files (line endings), mark large/generated folders as vendored, and help linguist on GitHub.
  - Why important: prevents accidental CRLF/LF issues and helps repository language/size signals on Git host.

- `.github/workflows/ci.yml` (GitHub Actions)
  - Purpose: basic CI that runs on push and pull requests, builds the project with JDK 21 and runs tests.
  - Why important: ensures PRs are verified, prevents regressions, and enforces that the project builds on the expected JDK.

- `.github/dependabot.yml`
  - Purpose: automatically open dependency update PRs for Maven dependencies on a schedule.
  - Why important: keeps dependencies up-to-date and reduces the manual work of dependency upgrades.

- `.env.example`
  - Purpose: example environment variable file with non-secret placeholders.
  - Why important: shows required environment variables and prevents accidental commits of real secrets. Developers copy it to `.env` locally.

Security and secrets
- Never commit real secrets or credentials. Use `.env.example` for placeholders and keep `.env` in `.gitignore` (already configured).
- Consider using secret scanners and repository-level secret policies in CI.

What to do next (suggested small steps)
1. Keep `docs/` as canonical documentation location.
2. Encourage contributors to read `CONTRIBUTING.md` and run `./mvnw test` locally before opening PRs.
3. Enable GitHub Actions (CI) by pushing these files and protecting `main` branch if desired.
4. Consider adding a short `CODE_OF_CONDUCT.md` and a license file if the project doesn't have one yet.

If you’d like, I can also:
- Add `CODE_OF_CONDUCT.md` and a license (tell me which).
- Move the run configuration under `.idea/runConfigurations/` (only if you confirm committing IDE configs is desired).
- Create a short `docs/ONBOARDING.md` that collects the most common first-day tasks for new team members.
