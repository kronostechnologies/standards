# ASR-10: Static Application Security Testing

Status: Accepted

## Context

In order to ensure the security of our applications, it is essential to implement Static Application Security Testing (
SAST) as part of our development process. SAST helps identify potential security vulnerabilities in the codebase early
in the development lifecycle, allowing developers to address these issues before they can be exploited. Without a robust
SAST process, our applications may be exposed to various security risks, including data breaches, unauthorized access,
and other cyber threats.

SAST scanning is also a requirement for our SOC 2 compliance.

## Decision

We will integrate SAST tools into our continuous integration (CI) pipeline to automatically scan the codebase for
security vulnerabilities on each pull request. The SAST tools will analyze the code for common security issues, such as
SQL injection, cross-site scripting (XSS), and insecure configurations.

SAST scans will be performed for each new versioning tags and findings will be published to a centralized dashboard for
easy tracking and management. Critical and high-severity vulnerabilities must be addressed before versions are released
to production.

A documentation of the SAST tooling will be kept
in [Analysis Tooling](https://equisoft.atlassian.net/wiki/spaces/HRMI/pages/52232356/Analysis+tooling).

### CI

SARIF reports will be generated and uploaded to GitHub for each pipeline run.

The CI will fail if there are any critical or high-severity vulnerabilities introduced by a pull request.

Violations will be required to be fixed before the pull request can be merged.

## Consequences

- Enhanced Security: Implementing SAST will help identify and mitigate security vulnerabilities early in the development
  process, reducing the risk of security incidents.
- Compliance: Integrating SAST into our development workflow will help us meet security compliance requirements, such as
  SOC 2.
- Ongoing Maintenance: The SAST tools and processes will need to be periodically reviewed and updated to accommodate
  changes in the codebase and emerging security threats.
