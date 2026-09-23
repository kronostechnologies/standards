---
description: Organization-wide Architecturally Significant Requirements (ASRs) for all Equisoft AWT repositories.
applyTo: "**"
---

# Organization Standards — ASR Instructions

This file defines core technical standards and constraints for AI coding agents working in any
repository within **Equisoft AWT**. All guidelines are derived from Architecturally Significant
Requirements (ASRs) and engineering documentation. You **must** follow these rules when
generating, modifying, or reviewing code. Ecosystem-specific rules (per-language tooling, style,
and prohibited patterns) live in separate scoped instruction files that load automatically when
you're touching matching files — see "Where to find more" below.

## Languages & Stack

**Primary languages:** Kotlin (backend), TypeScript (frontend).
**PHP** is **legacy only** — do not start new PHP projects or add new PHP features.

### Language Selection Criteria (ASR-05)

A language must satisfy all of the following criteria to be adopted:

- **Compile-time** execution model
- **Strongly typed** with static analysis support
- **Null-safe** (built-in null safety preferred)
- Easy to learn with accessible, high-quality documentation
- Reasonable public library and project ecosystem
- Strong, active community with regular updates and support
- Rich tooling support (IDE, IntelliSense, CLI)

## Organization-Wide Prohibited Patterns

- No committing secrets, credentials, or API keys
- No `TODO` or `FIXME` without a Jira ticket identifier
- No disabling CI checks, CODEOWNERS, or branch protections
- No silent exception catching (empty catch blocks or swallowed errors)

## Where to find more

- **CI/CD, code quality gating, and security scanning** — `ci-workflows.instructions.md` (loads
  automatically when editing `.github/workflows/**` or `.github/actions/**`)
- **Dependency pinning, lockfiles, and stability rules** — `dependencies.instructions.md` (loads
  automatically when editing package manifests/lockfiles)
- **Unit testing requirements** — `testing.instructions.md` (loads automatically when editing test
  files)
- **Repository onboarding (required tools, Makefile targets)** — `project-setup.instructions.md`
  (loads automatically when editing `Makefile` or `.tool-versions`)
- **Feature flags (Split.io)** — the `feature-flags` skill (invoke when adding or reviewing a
  feature flag)
- **Branching, code review, release & versioning** — the `release-and-versioning` skill (invoke
  when opening a release or asking about branching/review conventions)
- **Ecosystem-specific tooling, style, and prohibited patterns** — the `kotlin-backend`,
  `frontend`, and `php-backend` instruction packages (each loads automatically for its own file
  types)
