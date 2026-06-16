You are a technical writer generating an `asr.instructions.md` file for an organization's standards repository.
Your task is to produce a single, self-contained Markdown document that AI coding agents will follow
when working in any repository within the organization. Be concise but thorough, ensuring all relevant
ASRs and engineering documentation are synthesized into clear guidelines.

## Instructions

1. Read ALL of the ASR (Architecturally Significant Requirements) files in the `asr/` directory
   (all files matching `ASR-[0-9]*.md`, except `ASR-13_ai_features_constraints.md`) and the `docs/analysis-tooling.md` file.
2. Synthesize them into a single `asr.instructions.md` file using the structure and conventions described below.
3. Write the result to `docs/instructions/asr.instructions.md`. Overwrite the existing file.
4. Do NOT modify `AGENTS.md` at the repository root — it is manually maintained and references this generated file.

## Output Structure

The file MUST contain the following sections in this exact order:

### Header
- Title: `# Organization Standards — ASR Instructions`
- Brief intro stating this file defines technical standards and constraints for AI coding agents,
  derived from ASRs and engineering documentation.

### Languages & Stack
- Primary languages: Kotlin (backend), TypeScript (frontend).
- PHP is legacy only; no new PHP projects.
- Language selection criteria from ASR-05 (compile-time, strongly-typed, null-safe, community/tooling).
- **Per-Language Tooling** table with columns: Language/Ecosystem | Code Style | Code Quality | Test Runner | Test Coverage | SAST. Extract tools from the analysis-tooling doc. Do NOT include C# (unsupported for agents). Use em-dash (—) for unavailable tools.
- **Prohibited Patterns** subsection prefixed with:
  `> These conventions are enforced by the per-language tooling above (ESLint, Detekt, Stylelint, etc.) and are not defined in a standalone ASR.`
  Include these exact rules:
  - No `any` or unchecked type assertions in TypeScript
  - No suppressing linter/compiler warnings without explanatory comment
  - No committing secrets/credentials/API keys
  - No TODO/FIXME without Jira ticket identifier
  - No disabling CI checks, CODEOWNERS, or branch protections
  - No System.out/println/console.log for logging
  - No silent exception catching
- **Code Style Preferences** subsection with the same `>` prefix about tooling enforcement:
  - Prefer immutability (val/const)
  - Prefer data class for DTOs in Kotlin
  - Named exports in TypeScript
  - File naming: kebab-case.ts, PascalCase.kt
  - Prefer early returns
  - Structured logging (key-value pairs)
  - Sealed classes/Result for expected failures in Kotlin
  - Explicit error types/discriminated unions in TypeScript

### Project Setup & Conventions
- From ASR-02: Makefile, .tool-versions, required tools (Git, Docker, Make, asdf-vm).
- Makefile targets table: all, setup, check, test, compile, package, package.image.

### Repository & Versioning
- From ASR-01: GitHub as VCS, protected main branch, PR-only merges, Jira ticket in PR titles.
- Branching: feature/, release/, dev/ prefixes.
- From ASR-04: Code review requirements (one peer reviewer, no self-review, CODEOWNERS, approval invalidation).
- From ASR-03: SemVer, no version tracking in manifests, Release Operators for tags.
- Ownership & access roles.

### Code Quality & CI
- From ASR-06: GitHub Actions as CI platform.
- From ASR-07/ASR-08: Code style/quality — zero tolerance, SARIF reports, block merge.
- From ASR-09: Unit tests — all must pass, never skip, 80% coverage minimum (use the exact percentage from ASR-09), isolated tests, AAA pattern, JaCoCo reports.
- From ASR-10: SAST — scans on PRs and tags, SARIF to GHAS, critical/high block merge, retention period (use exact months from ASR-10).
- From ASR-11: SCA — SBOM on every run, published to GitHub Dependency Graph, block on vulnerable/non-compliant deps.
- Secret scanning via GHAS.

### Dependency Management
- From ASR-14: Pin exact versions, commit lockfiles, stability period (use exact days from ASR-14), Renovate.
- Internal package exemptions (@equisoft/*, @kronostechnologies/*, equisoft-actions).
- Per-ecosystem rules table: JS/TS (Yarn Berry), PHP (Composer), Python (uv), Kotlin/Java (Gradle), GitHub Actions (SHA pinning).

### Feature Flags
- From ASR-12: Split.io, targeting attributes table (User ID, appVersion, env, site, lang, organizations).

## Formatting Rules

- Separate major sections with `---` horizontal rules.
- Do NOT include `<!-- Customize: ... -->` HTML comments — this file is the canonical organization-wide reference and should not contain extension points.
- Use bold for emphasis on key terms.
- Use tables for structured data.
- Use bullet lists for rules and requirements.
- Keep the tone directive: address the AI agent as "you" where appropriate.
