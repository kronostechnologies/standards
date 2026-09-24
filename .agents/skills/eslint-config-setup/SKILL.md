---
name: eslint-config-setup
description: Set up or align a TypeScript/React project's ESLint and Stylelint configuration with the Equisoft AWT shared configs (@kronostechnologies/standards javascript packages).
---

# ESLint / Stylelint Config Setup

Use this skill when asked to introduce or align lint tooling in a TypeScript/React (Yarn Berry)
project with the organization's shared configs published from `kronostechnologies/standards`.

## Steps

1. Confirm the project uses **Yarn Berry** (`yarn.lock`, `.yarnrc.yml`); if not, flag this as an
   ASR-14 deviation before proceeding.
2. Add the relevant shared config package(s) as dev dependencies, matching the project type:
   - `@equisoft/eslint-config-typescript-react` for React apps
   - `@equisoft/eslint-config-typescript` for non-React TypeScript
   - `@equisoft/stylelint-config` for CSS/SCSS
3. Extend the project's `eslint.config.js`/`.eslintrc` from the installed shared config rather
   than redefining rules locally.
4. Run `yarn lint` (or the project's equivalent script) and fix newly surfaced violations —
   CI treats any new violation as a zero-tolerance failure (ASR-07/ASR-08).
5. Do not suppress a rule without an inline comment explaining why.

## Notes

- Prefer named exports, `kebab-case.ts` file naming, and explicit error types — these are
  enforced by the shared config, not re-implemented ad hoc.
