# GitHub workflow

## Why `main` is protected

`main` is the stable branch. Protecting it prevents accidental pushes, force-pushes, and deletions that can break the shared history.

## Why Pull Requests are required

All changes should go through Pull Requests so GitHub Actions can validate them before merge. For now, approvals are not required because this is a solo-developer workflow.

## Why Squash Merge is used

Squash merge keeps `main` clean by turning each feature branch into one commit. That makes history easier to read and keeps rollbacks simpler.

## Expected workflow

Feature Branch

↓

Pull Request

↓

Automated CI Checks

↓

Squash Merge

↓

`main`

## Future readiness

When the team grows, approval requirements can be enabled later in the branch protection rule for `main` by setting required pull request reviews in GitHub branch protection settings.
