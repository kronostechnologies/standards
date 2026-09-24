---
description: PHP/Composer-specific coding standards for backend services.
applyTo: "**/*.php,**/phpcs.xml,**/psalm.xml,**/phpunit.xml"
---

# PHP Backend Standards

These rules apply to PHP/Composer backend projects, in addition to the org-wide instructions
installed alongside this package via the `org-standards` APM dependency.

Per ASR-05, **PHP is legacy only** — do not start new PHP projects or add new PHP features;
changes should be limited to maintenance, bug fixes, and incremental modernization of existing
code.

## Tooling

| Concern | Tool |
|---|---|
| Code Style | PHP CodeSniffer |
| Code Quality | Psalm |
| Test Runner | PHPUnit |
| Test Coverage | PHPUnit (Clover) |
| SAST | Psalm taint analysis |

## Coding Standard

- Extend the shared `kronostechnologies/php-coding-standard` ruleset from `phpcs.xml` rather than
  redefining rules locally:
  ```xml
  <?xml version="1.0"?>
  <ruleset>
      <rule ref="./vendor/kronostechnologies/php-coding-standard/phpcs.xml"/>
  </ruleset>
  ```
- The shared ruleset is PSR-12 compliant; only add project-specific rule overrides when the
  shared ruleset doesn't already cover a case.

## Prohibited Patterns

> Enforced by PHPCS/Psalm where possible; called out here because lint/static-analysis
> configuration alone does not catch every case.

- No silent exception catching (empty catch blocks or swallowed errors)
- No suppressing PHPCS/Psalm warnings without an explanatory comment
- No reusing an exception's message as the log message when logging a caught exception — it's
  already in the stack trace; write a message that contextualizes the try/catch instead
- No logging personally identifiable information (names, emails, addresses, phone numbers)

## Code Style Preferences

- Use native PHP type declarations on all new/modified code: parameter types, return types, and
  class field types
- Use Psalm-syntax generics for array types in DocBlocks, e.g. `array<int>` rather than `int[]`
- Prefer constructor property promotion when declaring fields
- Declare classes `readonly` when all fields are readonly
- Use domain-specific exceptions instead of throwing generic `Exception`
- Use comments sparingly — only when they clarify intent that the code itself does not; delete
  comments or DocBlocks that just restate what the code does
- When a `catch` block logs the exception, embed it in the log context under an `exception` key

## Dependency Management

- **Composer** is the package manager; `composer.lock` must be committed
- Exact versions in `composer.json`; if an exact version isn't possible, pin to a branch name
  plus commit SHA (see `dependencies.instructions.md`, ASR-14)
- Internal Equisoft/Kronos Composer packages (e.g. `kronostechnologies/*`, `equisoft/*` on
  Packagist) are exempt from the 7-day stability rule
