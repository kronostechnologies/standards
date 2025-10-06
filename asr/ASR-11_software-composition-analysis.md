# ASR-11: Software Composition Analysis

Status: Accepted

## Context

We need to ensure that the third-party libraries and components we use in our software are secure, up-to-date, and
compliant with licensing requirements. Software Composition Analysis (SCA) helps identify known vulnerabilities in
open-source and third-party components, allowing us to address potential security risks before they can be exploited.
Without a robust SCA process, our applications may be exposed to various security threats, including data breaches,
unauthorized access, and other cyber risks.

## Decision

We will integrate Software Composition Analysis (SCA) tools into our continuous integration (CI) pipeline to
automatically scan the codebase for vulnerabilities in third-party libraries and components on each pull request. The
SCA tools will analyze the dependencies for known security issues and licensing compliance.

SCA scans will be performed for each new versioning tags and Software Bills of Materials (SBOM) will be published to a
centralized dashboard for easy tracking and management. Dependencies with Critical and high-severity vulnerabilities
must be addressed before versions are released to production.

An automated process will be established to report and track vulnerabilities found in third-party components.

A documentation of the SCA tooling will be kept
in [Analysis Tooling](https://equisoft.atlassian.net/wiki/spaces/HRMI/pages/52232356/Analysis+tooling).

### CI

SBOMs will be generated and uploaded to GitHub for each pipeline run.

The CI will fail if:

- A dependency with critical or high-severity vulnerabilities is introduced by a pull request.
- A non-compliant license is introduced by a pull request.

Violations will be required to be fixed before the pull request can be merged.

## Consequences

- Enhanced Security: Implementing SCA will help identify and mitigate vulnerabilities in third-party components early in
  the development process, reducing the risk of security incidents.
- Compliance: Integrating SCA into our development workflow will help us meet security compliance requirements, such as
  SOC 2.
- Ongoing Maintenance: The SCA tools and processes will need to be periodically reviewed and updated to accommodate
  changes in the codebase, third-party components, and emerging security threats.
