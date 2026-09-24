---
description: CI/CD, code quality gating, and security scanning standards for GitHub Actions workflows.
applyTo: ".github/workflows/**,.github/actions/**"
---

# CI/CD & Security Scanning Standards

### Continuous Integration (ASR-06)

- **GitHub Actions** is the CI platform for all pipelines.
- CI runs on every pull request and on every version tag.
- Merges to `main` or new version tags produce a deployable build artifact.

### Code Style & Quality — Zero Tolerance (ASR-07, ASR-08)

- **SARIF reports** are generated and uploaded to GitHub Advanced Security (GHAS) for every pull
  request.
- The CI **fails** if any new style or quality violation is introduced by a pull request.
- **All violations must be resolved before a PR can be merged.** There are no exceptions.

### Static Application Security Testing / SAST (ASR-10)

- SAST scans run on every pull request **and** on every version tag.
- SARIF reports are uploaded to **GitHub Advanced Security (GHAS)**.
- **Critical or high-severity vulnerabilities block merge** and must be resolved before a PR can
  be merged.
- SAST results for default branches must be **retained in GHAS for a minimum of 15 months** (SOC 2
  compliance requirement).

### Software Composition Analysis / SCA (ASR-11)

- An **SBOM (Software Bill of Materials)** is generated on every pipeline run and published to the
  **GitHub Dependency Graph**.
- CI **blocks merge** if a pull request introduces:
  - A dependency with **critical or high-severity vulnerabilities**
  - A dependency with a **non-compliant license**
- **Secret scanning** is handled via **GHAS**; all commits are scanned automatically and potential
  leaks trigger an alert.

### GitHub Actions Pinning (ASR-14)

- All third-party GitHub Actions **must pin to an exact commit SHA**, not a floating tag — tags
  can be moved to point at compromised code.
- Example: `uses: actions/checkout@b4ffde65f46336ab88eb53be808477a3936bae11 # v4.1.1`
- Internal `equisoft-actions` are exempt from this pinning requirement and the 7-day stability
  rule.
