# ASR-09: Unit tests

Status: Accepted

## Context

In order to ensure the reliability and maintainability of our codebase, it is essential to establish a robust unit
testing strategy. Unit tests help verify that individual components of the software function as intended, allowing
developers to catch bugs early in the development process. A well-defined unit testing approach also facilitates
refactoring and enhances code quality by providing a safety net that ensures existing functionality remains intact as
new features are added or changes are made. Without a consistent unit testing strategy, the codebase can become fragile,
making it difficult to maintain and evolve over time.

## Decision

We will adopt a comprehensive unit testing strategy that includes the following key practices:

1. **Test Coverage**: We will aim for a minimum of 80% code coverage for all new code and significant changes to
   existing code. While 100% coverage is ideal, it is not always practical or necessary. The focus will be on covering
   critical paths and edge cases to ensure robustness.
2. **Test Isolation**: Unit tests will be designed to be isolated from external dependencies, such as databases or
   external services. We will use mocking and stubbing techniques to simulate these dependencies, ensuring that tests
   remain fast and reliable.
3. **Continuous Integration**: Unit tests will be integrated into our continuous integration (CI) pipeline. Tests will
   be executed automatically on each pull request to ensure that new code does not introduce regressions or break
   existing functionality. Tests must not be ignored or skipped in the CI pipeline.
4. **Test Structure**: Unit tests will follow a consistent structure, including setup, execution, and teardown phases.
   Each test will focus on a single aspect of the functionality being tested, adhering to the principles of Arrange,
   Act, Assert (AAA).

### CI

Tests will be executed automatically on each pull request to ensure that new code does not introduce regressions or
break existing functionality.

All tests must pass before a pull request can be merged. Tests must not be ignored or skipped.

Test coverage reports in either JaCoCo format will be generated and uploaded to GitHub by each run of the pipeline.

## Consequences

- Improved Code Quality: A robust unit testing strategy will help identify and fix issues early in the development
  process, leading to a more reliable codebase.
- Easier Maintenance: With a comprehensive suite of unit tests, developers can refactor and enhance the codebase with
  confidence, knowing that existing functionality is protected.
- Ongoing Commitment: The unit testing strategy will require ongoing commitment from the development team to write and
  maintain tests as part of the development process.
