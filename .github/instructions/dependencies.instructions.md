---
description: Dependency pinning, lockfile, and stability rules for all package ecosystems.
applyTo: "**/package.json,**/yarn.lock,**/composer.json,**/composer.lock,**/*.gradle.kts,**/libs.versions.toml,**/pyproject.toml,**/uv.lock,**/renovate.json*"
---

# Dependency Management

### Pinning & Stability (ASR-14)

- **All dependencies must be pinned to exact versions** (or commit SHAs for GitHub Actions).
- **Lockfiles must be committed** to version control and kept up to date.
- **Stability period:** Third-party dependencies must reach a minimum of **7 stability days**
  before adoption or update (enforced via Renovate `minimumReleaseAge`).
- **Renovate** is the organization-standard tool for automated dependency lifecycle management and
  must be configured and running in every repository.

### Internal Package Exemptions

The following internal packages are **exempt** from pinning validations, SHA requirements, and the
7-day stability rule:

- `@equisoft/*`
- `@kronostechnologies/*`
- `@wealthelements/*`
- `equisoft-actions` (internal GitHub Actions)

### Per-Ecosystem Rules

| Ecosystem | Package Manager | Lockfile | Pinning Requirement |
|---|---|---|---|
| **JS / TS** | **Yarn Berry** | `yarn.lock` (must be committed) | Exact versions in `package.json`; ranges allowed for published libraries if lockfile is committed |
| **PHP** _(legacy)_ | Composer | `composer.lock` (must be committed) | Exact versions in `composer.json`; if not possible, use branch name + commit SHA |
| **Python** | **uv** | `uv.lock` (must be committed) | Exact versions; Renovate must be explicitly configured for the `uv` ecosystem |
| **Kotlin / Java** | Gradle | Gradle lockfiles (optional due to lack of proper tooling) | Gradle Version Catalogs (`libs.versions.toml`) strongly recommended |
| **GitHub Actions** | — | — | **Must pin to exact commit SHA**, not a floating tag; e.g., `uses: actions/checkout@b4ffde65f46336ab88eb53be808477a3936bae11 # v4.1.1` |
