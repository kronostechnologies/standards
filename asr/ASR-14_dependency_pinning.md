# ASR-14: Dependency Pinning

Status: Accepted

## Context

Unpinned dependencies introduce significant security risks (such as supply chain attacks) and stability issues by making
builds non-deterministic. Without strict version constraints, a project can inadvertently pull in breaking changes or
compromised code. To mitigate these risks, dependencies across all projects and languages must be tightly controlled and
deterministically resolved. We are utilizing Renovate across the organization to automate the lifecycle and updates of
these pinned dependencies.

## Decision

All projects MUST pin their dependencies to exact versions or commit SHAs, and utilize package manager lockfiles to
ensure fully deterministic and reproducible builds.

### Dependency Stability and Exclusions

* **Stability Days:** All third-party dependencies MUST reach a minimum of 7 stability days before they can be adopted
  or updated. This is managed globally via the organization's Renovate configuration (e.g., using `minimumReleaseAge`).
* **Internal Exclusions:** Internal dependencies (e.g., `@equisoft/*`, `@kronostechnologies/*`) and internal GitHub
  Actions (e.g., `equisoft-actions`) are excluded from all pinning validations, SHA requirements, and the 7-day
  stability rule.

The following standards apply per ecosystem:

### GitHub Actions

* All third-party GitHub Actions MUST be pinned to an exact commit SHA rather than a tag (e.g., `@v2`), as tags can be
  moved to point to compromised code.
* Example: `uses: actions/checkout@b4ffde65f46336ab88eb53be808477a3936bae11 # v4.1.1`

### Javascript / Typescript (Yarn Berry)

* Projects MUST use Yarn Berry.
* The `yarn.lock` file MUST be committed to version control and kept up to date.
* When specifying dependencies in `package.json`, exact versions MUST be used.
    * Projects that expose libraries can use version ranges. However, they MUST ensure that the `yarn.lock` file is
      committed and up to date to guarantee deterministic builds for consumers.

### Gradle (Java / Kotlin)

* Projects MUST enable and configure dependency locking.
* Projects SHOULD use Gradle Version Catalogs (`libs.versions.toml`) to centralize dependency version declarations.
* Projects SHOULD generate Gradle lockfiles. The generated lockfiles MUST be committed to version control.
    * Note: This rule is currently recommended but not mandatory due to the higher complexity of managing Gradle
      lockfiles. This document will be updated to make lockfiles mandatory once there is enough tooling to support their
      management and maintenance.

### Python

* Python packages MUST be managed using the `uv` tool.
* The `uv.lock` file MUST be generated and committed to version control to guarantee deterministic environments.
* Projects MUST explicitly configure Renovate to monitor and update dependencies in the `uv` ecosystem.

### PHP (Composer)

* Projects MUST use Composer for managing PHP dependencies.
* The `composer.lock` file MUST be committed to version control and kept up to date.
* When specifying dependencies in `composer.json`, exact versions MUST be used.
    * If it is not possible, branch name and commit SHA MUST be used (e.g.,
      `dev-main#b4ffde65f46336ab88eb53be808477a3936bae11`).

## Consequences

* **Improved Security:** Reduces the risk of supply chain attacks by preventing sudden shifts to compromised dependency
  versions.
* **Delayed Adoption:** The 7-day stability rule means projects will intentionally not receive immediate updates for
  newly released third-party dependencies, sacrificing bleeding-edge access for stability.
* **Deterministic Builds:** Ensures development, CI, and production environments are functionally identical and
  reproducible over time.
* **Maintenance Overhead:** Without silent upgrades, dependencies rely on explicitly triggering updates.
* **Renovate Reliance:** Teams MUST ensure Renovate is configured and successfully running in all repositories to
  perform updates; otherwise, dependencies will quickly become stale and vulnerable.
