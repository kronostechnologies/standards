---
description: Repository onboarding — required tools and Makefile conventions.
applyTo: "Makefile,.tool-versions"
---

# Project Setup & Conventions

### Required Tools (ASR-02)

Every developer workstation and CI environment must have the following tools installed:

| Tool | Purpose |
|---|---|
| **Git** | Version control |
| **Docker** | Containerized dependencies and builds |
| **Make** | Standardized task runner |
| **asdf-vm** | Language and tool version management |

- Every repository **must** include a `.tool-versions` file specifying exact tool versions for
  asdf-vm.
- Every repository **must** include a `Makefile` with the standard targets below.

### Makefile Targets

All targets must be **idempotent**. If a target is not applicable (e.g., `compile` for PHP), it
must still be present as a no-op.

| Target | Description |
|---|---|
| `all` | Default target. Runs `setup`, `check`, `test`, `compile`, and `package`. Run after cloning to verify the repository is in a stable state. |
| `setup` | Brings the repository into a usable state: checks prerequisites, installs asdf-vm tool versions, installs dependencies. |
| `check` | Runs all static checks (linting, type-checking, style). Fails on any violation. |
| `test` | Runs all tests. Fails on any test failure. |
| `compile` | Runs all compilation and transpilation steps. |
| `package` | Creates all distributable packages. |
| `package.image` | Builds container images for distribution. |
