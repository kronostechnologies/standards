# ASR-07: Code Style

Status: Accepted

## Context

In order to maintain a consistent and readable codebase, it is essential to establish and adhere to coding style
guidelines. A well-defined code style helps improve collaboration among team members, reduces the cognitive load when
reading and reviewing code, and minimizes the likelihood of introducing errors due to misunderstandings or
inconsistencies. Without a standardized approach to code style, the codebase can become fragmented, making it difficult
for developers to navigate and contribute effectively.

## Decision

We will adopt established coding style guides for each programming language used in our projects. These style guides
will serve as the foundation for our coding standards, ensuring that all team members follow a consistent approach to
writing code.

Validation of code style will be enforced through automated tools integrated into our development workflow. These tools
will check for adherence to the defined coding standards during the code review process, ensuring that any deviations
are identified and addressed before code is merged into the main branch.

A documentation of the code style tooling will be kept
in [Analysis Tooling](https://equisoft.atlassian.net/wiki/spaces/HRMI/pages/52232356/Analysis+tooling).

### CI

SARIF reports will be generated and uploaded to GitHub for each pull request. The CI will fail if there are any new
violations introduced by the pull request.

Violations will be required to be fixed before the pull request can be merged.

## Consequences

- Improved Readability: A consistent code style enhances the readability of the codebase, making it easier for
  developers to understand and maintain the code.
- Enhanced Collaboration: A shared understanding of coding standards fosters better collaboration among team members, as
  everyone is on the same page regarding how code should be written.
- Smaller Code Reviews: Automated style checks reduce the time spent on code reviews by minimizing discussions around
  formatting and style issues. Smaller diffs will mean more time can be spent on the actual code changes.
- Ongoing Maintenance: The coding style guidelines and validation tools will need to be periodically reviewed and
  updated to accommodate changes in programming languages and best practices.
