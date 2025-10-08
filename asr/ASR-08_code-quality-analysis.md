# ASR-08: Code Quality Analysis

Status: Accepted

## Context

In order to maintain a high-quality codebase, it is essential to establish and enforce code quality standards.
High-quality code is easier to read, maintain, and extend, which ultimately leads to more efficient development
processes and a more reliable product. Without clear code quality guidelines, the codebase can become inconsistent,
difficult to understand, and prone to bugs and technical debt.

## Decision

We will implement automated code quality analysis tools as part of our development workflow. These tools will analyze
the codebase for various quality metrics, such as code complexity, duplication and potential bugs before code is merged
into the main branch.

A documentation of the code quality tooling will be kept
in [Analysis Tooling](https://equisoft.atlassian.net/wiki/spaces/HRMI/pages/52232356/Analysis+tooling).

### CI

SARIF reports will be generated and uploaded to GitHub for each pull request. The CI will fail if there are any new
violations introduced by the pull request.

Violations will be required to be fixed before the pull request can be merged.

## Consequences

- Improved Code Quality: Automated analysis helps identify potential issues early in the development process, allowing
  developers to address them before they become more significant problems.
- Consistency: Enforcing code quality standards ensures that the codebase remains consistent, making it easier for
  developers to read and understand.
- Reduced Technical Debt: By regularly analyzing the codebase for quality issues, we can minimize the accumulation of
  technical debt and maintain a healthier codebase.
- Ongoing Maintenance: The code quality analysis tools and standards will need to be periodically reviewed and updated
  to accommodate changes in programming languages and best practices.
