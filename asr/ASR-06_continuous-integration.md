# ASR-06: Continuous Integration

Status: Accepted

## Context

In order to ensure a smooth and efficient development process, it is essential to implement a robust Continuous
Integration (CI) strategy. CI helps automate the integration of code changes from multiple contributors into a shared
repository, allowing for early detection of integration issues and reducing the risk of conflicts. By integrating code
frequently, we can identify and address bugs, improve code quality, and accelerate the delivery of new features. Without
a well-defined CI strategy, the development process can become fragmented, leading to delays, increased errors, and
reduced collaboration among team members.

## Decision

We will implement a Continuous Integration (CI) pipeline that automatically runs code analysis, unit tests, and other
validation checks on each pull request before code is merged into the main branch. The same checks will be performed for
each new versioning tags and failures will be reported to the development team for prompt resolution.

Merges into the main branch or new versioning tags will create a build artifact that can be deployed to various
environments, such as staging or production. This ensures that the code is always in a deployable state and allows for
rapid delivery of new features and bug fixes.

All future ASRs that cover validations of the code will include a section about their integration in the CI pipeline.

Github Actions will be used as the CI tool to implement and manage the CI pipeline.

## Consequences

- Improved Collaboration: A robust CI strategy will enhance collaboration among team members by providing a shared
  platform for integrating code changes.
- Early Issue Detection: Automated checks will help identify integration issues and bugs early in the development
  process, reducing the risk of conflicts and delays.
- Accelerated Delivery: By ensuring that the code is always in a deployable state, we can accelerate the delivery of new
  features and improvements to our users.
- Ongoing Maintenance: The CI pipeline will require ongoing maintenance and updates to accommodate changes in the
  codebase and development practices.
