# ASR-05: Languages Selection and Libraries

Status: Accepted

## Context

As a software development team, we need to establish a clear strategy for selecting programming languages and libraries
for our projects. This strategy will help ensure that we use technologies that are well-suited to our needs,
maintainable, and supported by the community. Without a defined approach, we risk using outdated or unsupported
technologies, which can lead to increased technical debt and maintenance challenges.

The choice of programming languages and libraries can significantly impact the development process, code quality, and
overall project success. Therefore, it is essential to consider factors such as community support, documentation,
performance, and compatibility with existing systems when making these decisions.

Furthermore, too much diversity in languages and libraries can lead to increased complexity in the codebase, making it
harder for developers to switch between projects and maintain a consistent level of quality. By standardizing our
choices, we can streamline development processes, improve collaboration among team members and limit the maintenance
burden of shared libraries and tooling.

## Decision

We will establish a set of criteria for selecting programming languages and libraries for our projects. These criteria
will include:

- The language must be compile-time;
- The language must be strongly-typed;
- The language must include built-in null-safety, if possible;
- The language must be easy to learn and access to good documentation is necessary;
- The language must have access to a reasonable amount of public libraries and projects;
- The language must have a strong and active community, with regular updates and support;
- The language must be backed by appropriate tooling that makes the usage more efficient (IDE, Intellisense, CLI, ...)

Based on these criteria, we will limit our primary programming languages to Kotlin for backend and TypeScript for
frontend development. These languages have been chosen for their strong typing, null-safety features, and robust
ecosystems.

Legacy projects may continue to use other languages, such as PHP, but new projects should prioritize Kotlin and
TypeScript unless there is a compelling reason to choose otherwise.

## Consequences

- Improved Maintainability: By standardizing our programming languages and libraries, we can reduce the complexity of
  our codebase, making it easier to maintain and extend over time. It will be easier to maintain and document shared
  libraries and tooling.
- Enhanced Collaboration: A consistent set of technologies will facilitate collaboration among team members, as they
  will be more familiar with the tools and languages used across projects.
- Reduced Technical Debt: By avoiding outdated or unsupported technologies, we can minimize the risk of technical debt
  and ensure that our projects remain up-to-date with industry best practices.
- Reduction in Runtime Errors: The use of compile-time languages will help catch potential errors during development,
  leading to more robust and reliable code.
