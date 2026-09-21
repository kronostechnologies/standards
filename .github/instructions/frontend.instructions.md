---
description: TypeScript/JavaScript/React coding standards for frontend and Node.js projects.
applyTo: "**/*.js,**/*.jsx,**/*.mjs,**/*.cjs,**/*.ts,**/*.tsx"
---

# Frontend Standards

These rules apply to JavaScript, TypeScript and React (Yarn) projects, in addition to the org-wide
`asr.instructions.md` (installed alongside this package via the `org-standards` APM dependency).

Per ASR-05, **TypeScript is the preferred language** for new frontend/Node.js code — plain
JavaScript is supported for **legacy usages only** (existing `.js`/`.jsx`/`.mjs`/`.cjs` files);
do not start new modules in plain JavaScript.

## Tooling

| Concern | Tool |
|---|---|
| Code Style | ESLint, Stylelint |
| Code Quality | ESLint, Stylelint |
| Test Runner | Jest |
| Test Coverage | Jest (minimum 80%) |
| SAST | CodeQL |

## Prohibited Patterns

> Enforced by ESLint/Stylelint where possible; called out here because lint configuration alone
> does not catch every case.

- No `any` or unchecked type assertions
- No `console.log` for logging — use structured logging (key-value pairs)
- No silent exception catching (empty catch blocks or swallowed errors)
- No suppressing linter/compiler warnings without an explanatory comment

## Code Style Preferences

- Prefer `const`/`readonly`; prefer immutability
- Use named exports (avoid default exports)
- File naming: `kebab-case.ts`
- Prefer early returns to reduce nesting
- Use explicit error types or discriminated unions for expected failures

## Dependency Management

- **Yarn Berry** is the package manager; `yarn.lock` must be committed
- Exact versions in `package.json`; ranges allowed for published libraries if the lockfile is
  committed
- `@equisoft/*` / `@kronostechnologies/*` / `@wealthelements/*` internal packages are exempt from
  the 7-day stability rule (see org-wide `asr.instructions.md`)
