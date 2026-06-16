# Organization Standards — ASR Instructions

This file defines technical standards and constraints for AI coding agents working in any repository within the
Kronos/Equisoft organization. All guidelines are derived from Architecturally Significant Requirements (ASRs) and
engineering documentation. You **must** follow these rules when generating, modifying, or reviewing code.

---

## Languages & Stack

**Primary languages:** Kotlin (backend), TypeScript (frontend).  
**PHP** is **legacy only** — no new PHP projects or features shall be started in PHP.

### Language Selection Criteria (ASR-05)

A language must satisfy all the following to be adopted:

- **Compile-time** execution model
- **Strongly typed** with static analysis support
- **Null-safe** (built-in null safety preferred)
- Easy to learn with good documentation
- Reasonable public library ecosystem
- Strong, active community with regular updates
- Strong tooling support (IDE, IntelliSense, CLI)

### Per-Language Tooling

| Language / Ecosystem | Code Style | Code Quality | Test Runner | Test Coverage | SAST |
|---|---|---|---|---|---|
| **Kotlin (Gradle)** | Detekt | Detekt | JUnit | JaCoCo | CodeQL |
| **TypeScript / JS / CSS (Yarn)** | ESLint, Stylelint | ESLint, Stylelint | Jest | Jest | CodeQL |
| **PHP (Composer)** _(legacy)_ | PHP CodeSniffer | Psalm | PHPUnit | PHPUnit | Psalm Taint Analysis |
| **Python** | pycodestyle | pytype | pytest | — | CodeQL |

### Prohibited Patterns

> These conventions are enforced by the per-language tooling above (ESLint, Detekt, Stylelint, etc.) and are not defined in a standalone ASR.

- No `any` or unchecked type assertions in TypeScript
- No suppressing linter/compiler warnings without an explanatory comment
- No committing secrets, credentials, or API keys
- No `TODO` or `FIXME` without a Jira ticket identifier
- No disabling CI checks, CODEOWNERS, or branch protections
- No `System.out`/`println`/`console.log` for logging — use structured logging
- No silent exception catching (empty catch blocks or swallowed errors)

### Code Style Preferences

> These conventions are enforced by the per-language tooling above (ESLint, Detekt, Stylelint, etc.) and are not defined in a standalone ASR.

- Prefer immutability: use `val` in Kotlin, `const`/`readonly` in TypeScript
- Prefer `data class` for DTOs in Kotlin
- Use named exports in TypeScript (avoid default exports)
- File naming: `kebab-case.ts` for TypeScript, `PascalCase.kt` for Kotlin
- Prefer early returns to reduce nesting
- Use structured logging with key-value pairs
- Use sealed classes or `Result` types for expected failures in Kotlin
- Use explicit error types or discriminated unions for errors in TypeScript

---

## Project Setup & Conventions

### Required Tools (ASR-02)

Every developer workstation and CI environment must have exactly the following tools installed:

| Tool | Purpose |
|---|---|
| **Git** | Version control |
| **Docker** | Containerized dependencies and builds |
| **Make** | Standardized task runner |
| **asdf-vm** | Language/tool version management |

- Every repository **must** include a `.tool-versions` file specifying exact tool versions for asdf-vm.
- Every repository **must** include a `Makefile` with the standard targets below.

### Makefile Targets

All targets must be **idempotent**. If a target is not applicable (e.g., `compile` for PHP), it must still be present as a no-op.

| Target | Description |
|---|---|
| `all` | Default target. Runs `setup`, `check`, `test`, `compile`, and `package`. Run this after cloning to verify the repository is in a stable state. |
| `setup` | Brings the repository into a usable state: checks prerequisites, installs asdf-vm tool versions, installs dependencies. |
| `check` | Runs all static checks (linting, type-checking, style). Fails on any violation. |
| `test` | Runs all tests. Fails on any test failure. |
| `compile` | Runs all compilation and transpilation steps. |
| `package` | Creates all distributable packages. |
| `package.image` | Builds container images for distribution. |

---

## Repository & Versioning

### Repository Management (ASR-01)

- **VCS:** GitHub is the single source of truth for all code.
- The **`main` branch is protected**: merges are only allowed via pull requests after successful reviews and automated checks.
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
  - **Release Operators** — can create release branches, merge into release branches, and create version tags
  - **Leadership** — administrative access for repository governance

### Versioning (ASR-03)

- All projects follow **Semantic Versioning (SemVer)**: `MAJOR.MINOR.PATCH`.
  - `MAJOR`: incompatible API changes
  - `MINOR`: backward-compatible functionality or regular release cycle
  - `PATCH`: backward-compatible bug fixes
- Versions are tracked as **Git tags**, not in package manifests (`package.json`, `build.gradle`, `composer.json`).
- Only **Release Operators** may create version tags.
- Tags must be created against commits on `main` or a `release/` branch.
- Major releases are **only** for breaking public API changes; major visual changes or dependency updates without API impact are minor releases.

---

## Code Quality & CI

### Continuous Integration (ASR-06)

- **GitHub Actions** is the CI platform for all pipelines.
- CI runs on every pull request and on every version tag.
- Merges to `main` or new version tags produce a deployable build artifact.

### Code Style & Quality — Zero Tolerance (ASR-07, ASR-08)

- **SARIF reports** are generated and uploaded to GitHub Advanced Security (GHAS) for every pull request.
- The CI **fails** if any new style or quality violation is introduced by a pull request.
- **All violations must be resolved before a PR can be merged.** There are no exceptions.

### Unit Tests (ASR-09)

- All unit tests **must pass** on every pull request — no skipping or ignoring tests.
- **Minimum 80% code coverage** for all new code and significant changes.
- Tests must be **isolated** from external dependencies (databases, external services); use mocking/stubbing.
- Tests must follow the **Arrange–Act–Assert (AAA)** pattern and focus on a single aspect per test.
- **JaCoCo** coverage reports are generated and uploaded to GitHub by each pipeline run.

### Static Application Security Testing / SAST (ASR-10)

- SAST scans run on every pull request **and** on every version tag.
- SARIF reports are uploaded to **GitHub Advanced Security (GHAS)**.
- **Critical or high-severity vulnerabilities block merge** and must be resolved before a PR can be merged.
- SAST results for default branches must be **retained in GHAS for a minimum of 15 months** (SOC 2 compliance requirement).

### Software Composition Analysis / SCA (ASR-11)

- An **SBOM (Software Bill of Materials)** is generated on every pipeline run and published to **GitHub Dependency Graph**.
- CI **blocks merge** if a pull request introduces:
  - A dependency with **critical or high-severity vulnerabilities**
  - A dependency with a **non-compliant license**
- Secret scanning is handled via **GHAS**; all commits are scanned automatically. Potential leaks trigger an alert.

---

## Dependency Management

### Pinning & Stability (ASR-14)

- **All dependencies must be pinned to exact versions** (or commit SHAs for GitHub Actions).
- **Lockfiles must be committed** to version control and kept up to date.
- **Stability period:** Third-party dependencies must reach a minimum of **7 stability days** before adoption or update (enforced via Renovate `minimumReleaseAge`).
- **Renovate** is the organization-standard tool for automated dependency lifecycle management. It must be configured and running in every repository.

### Internal Package Exemptions

The following internal packages are **exempt** from pinning validations, SHA requirements, and the 7-day stability rule:

- `@equisoft/*`
- `@kronostechnologies/*`
- `equisoft-actions` (internal GitHub Actions)

### Per-Ecosystem Rules

| Ecosystem | Package Manager | Lockfile | Pinning Requirement |
|---|---|---|---|
| **JS / TS** | **Yarn Berry** | `yarn.lock` (must be committed) | Exact versions in `package.json`; ranges allowed for libraries if lockfile is committed |
| **PHP** _(legacy)_ | Composer | `composer.lock` (must be committed) | Exact versions in `composer.json`; if impossible, use branch + commit SHA |
| **Python** | **uv** | `uv.lock` (must be committed) | Exact versions; Renovate must be configured for the `uv` ecosystem |
| **Kotlin / Java** | Gradle | Gradle lockfiles (must be committed; currently recommended, will become mandatory) | Gradle Version Catalogs (`libs.versions.toml`) strongly recommended |
| **GitHub Actions** | — | — | **Must pin to exact commit SHA** (not a floating tag); e.g., `uses: actions/checkout@b4ffde65f46336ab88eb53be808477a3936bae11 # v4.1.1` |

---

## Feature Flags

### Split.io (ASR-12)

- **Split.io** is the organization-standard feature flag management system.
- Feature flags are preferred over feature branches and release branches for isolating in-progress work.
- All applications must integrate with the Split.io SDK to evaluate flags at runtime.

### Targeting Attributes

You must expose the following attributes to Split.io for flag targeting:

| Attribute | Format / Notes |
|---|---|
| **User ID** | Global user ID from account-service (not application-specific). Use `anonymous` for non-user contexts (e.g., scheduled tasks). |
| **appVersion** | SemVer string of the running application. |
| **env** | Runtime environment: `development`, `staging`, `production`, etc. |
| **site** | `ca`, `us`, or a dedicated customer identifier. |
| **lang** | User language preference in ISO 639-1 format (e.g., `fr`, `en`). |
| **organizations** | Comma-separated list of organization IDs the current user belongs to. |
