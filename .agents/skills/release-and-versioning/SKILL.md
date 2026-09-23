---
name: release-and-versioning
description: Branching, code review, and release/versioning conventions for Equisoft AWT repositories, per ASR-01/ASR-03/ASR-04.
---

# Release, Review & Versioning

### Repository Management (ASR-01)

- **VCS:** GitHub is the single source of truth for all code.
- The **`main` branch is protected**: merges are only allowed via pull requests after successful
  reviews and automated checks.
- **PR titles must include a Jira ticket identifier** for traceability.

### Branching Strategy

| Prefix | Purpose |
|---|---|
| `feature/` | Specific features or bug fixes; merged via PR after code review. Prefer feature flags over long-lived feature branches. |
| `release/` | Protected release branches; merges via PR only. Prefer feature flags over release branches when possible. |
| `dev/` | Ongoing development work. |

### Code Review Requirements (ASR-04)

- All pull requests require **at least one peer reviewer** from the owning team.
- **Self-review is not permitted.**
- **CODEOWNERS** is mandatory and defines required reviewers per path.
- Approvals are **invalidated** when new changes are pushed after approval.
- Access roles:
  - **All team members** — peer review required on all PRs
  - **Release Operators** — can create release branches, merge into release branches, and create
    version tags
  - **Leadership** — administrative access for repository governance

### Versioning (ASR-03)

- All projects follow **Semantic Versioning (SemVer)**: `MAJOR.MINOR.PATCH`.
  - `MAJOR`: incompatible API changes
  - `MINOR`: backward-compatible functionality or regular release cycle
  - `PATCH`: backward-compatible bug fixes
- Versions are tracked as **Git tags**, not in package manifests (`package.json`,
  `build.gradle`, `composer.json`).
- Only **Release Operators** may create version tags.
- Tags must be created against commits on `main` or a `release/` branch.
- Major releases are **only** for breaking public API changes; major visual changes or dependency
  updates that do not affect the public API are minor releases.
