---
description: Unit testing requirements — coverage, isolation, and structure.
applyTo: "**/*.test.*,**/*.spec.*,**/*Test.php,**/*IT.php,**/*Test.kt,**/tests/**,**/test/**"
---

# Unit Tests (ASR-09)

- All unit tests **must pass** on every pull request — tests must never be ignored or skipped in
  CI.
- **Minimum 80% code coverage** for all new code and significant changes.
- Tests must be **isolated** from external dependencies (databases, external services); use
  mocking and stubbing.
- Tests must follow the **Arrange–Act–Assert (AAA)** pattern and focus on a single aspect per
  test.
- Coverage reports are generated and uploaded per pipeline run — **JaCoCo** for Kotlin, **Clover**
  for PHP (PHPUnit), Jest's own coverage reporter for TypeScript/JavaScript.
