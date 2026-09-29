---
name: Publish org-standards package to agent-toolkit

on:
  push:
    branches:
      - master
    paths:
      - "asr/**"
      - "!asr/ASR-13_ai_features_constraints.md"
      - "!asr/ASR-XX_template.md"
      - "!asr/index.md"
      - "docs/**"
      - "!docs/instructions/**"
      - ".github/workflows/publish-asr-to-agent-toolkit.md"
  workflow_dispatch:

permissions:
  contents: read
  copilot-requests: write

network: defaults

checkout:
  - fetch-depth: 1
  - repository: kronostechnologies/agent-toolkit
    path: ./agent-toolkit
    github-app:
      client-id: ${{ vars.APM_APP_ID }}
      private-key: ${{ secrets.APM_APP_PRIVATE_KEY }}
      repositories:
        - agent-toolkit
    current: true

safe-outputs:
  github-app:
    client-id: ${{ vars.APM_APP_ID }}
    private-key: ${{ secrets.APM_APP_PRIVATE_KEY }}
    owner: kronostechnologies
    repositories:
      - standards
      - agent-toolkit
  create-pull-request:
    target-repo: "kronostechnologies/agent-toolkit"
    base-branch: "main"
    title-prefix: "QCTECH-5463 "
    labels: [automation, apm]

---

You are a technical writer generating and publishing the `org-standards` APM package for an
organization's standards repository. This repository (`kronostechnologies/standards`) is checked
out at the workspace root; the target repository (`kronostechnologies/agent-toolkit`, where the
package is consumed via APM as `plugins/org-standards`) is checked out at `./agent-toolkit` and is
your primary target for all GitHub operations, including the pull request you open at the end.

Your task is to produce a small set of self-contained Markdown files — an always-on core plus
several narrowly-scoped instruction files and skills — that AI coding agents will follow when
working in any repository within the organization, and to write them directly into
`./agent-toolkit/plugins/org-standards/.apm/`. Be concise but thorough, ensuring all relevant ASRs
and engineering documentation are synthesized into clear guidelines, and that every rule lives in
**exactly one** file so nothing is duplicated or dropped.

## Instructions

1. Read ALL of the ASR (Architecturally Significant Requirements) files in this repository's
   `asr/` directory (all files matching `ASR-[0-9]*.md`, except `ASR-13_ai_features_constraints.md`)
   and the `docs/analysis-tooling.md` file. In particular, use `asr/ASR-15_rest-api-design.md`
   as the source for REST API guidance. Synthesize its Decision sections 1-15 into the on-demand
   skill and references below; use its appendices for implementation context where relevant.
   Do not use the frozen `docs/instructions/` snapshot as the source.
2. Synthesize them into the file tree described below, under
   `./agent-toolkit/plugins/org-standards/.apm/`. Every file's frontmatter is part of the
   generated output — write it exactly as specified per file below (`description` and `applyTo`
   for instructions, `name` and `description` for skills); do not omit it or leave it unfilled.
3. Mirror the resulting tree into `./agent-toolkit/plugins/org-standards/.apm/` **exactly**:
   - **Add** any file described below that is missing from the target tree.
   - **Update** any file whose content differs from what you generated.
   - **Delete** any file present in the target tree under
     `./agent-toolkit/plugins/org-standards/.apm/` that is no longer part of this structure — in
     particular, if `instructions/asr.instructions.md` still exists there (a pre-migration
     artifact), delete it. This is a redistribution of the same rules across new files, not a
     removal of content.
4. Do NOT write to this repository's `docs/instructions/asr.instructions.md`. That file is a
   frozen, pre-APM artifact kept only so repositories still consuming it by raw URL don't 404; it
   is allowed to go stale and must not be regenerated or updated by this workflow.
5. Do NOT modify this repository's `AGENTS.md` — it is manually maintained.
6. If nothing changed in `./agent-toolkit/plugins/org-standards/.apm/`, make no edits under
   `./agent-toolkit/` and do not open a pull request. Otherwise, let agent-toolkit's release-please
   automation manage the package version: do NOT hand-edit any `apm.yml`, release manifest, or
   marketplace file. Signal the intended SemVer change with a conventional-commit PR title
   starting with `feat(org-standards):` for added rules or new files (including ASR-15),
   `fix(org-standards):` for corrections, or `feat(org-standards)!:` only for a fully reversed
   rule. Include `QCTECH-5463` in the title after the conventional-commit prefix. If the PR is
   squash-merged, use the same conventional-commit title for the squash commit so release-please
   can recognize it.
7. Do not modify anything else in `./agent-toolkit/` — not other packages, not the root `apm.yml`,
   not `apm.lock.yaml`, not `.claude-plugin/marketplace.json`.
8. Open a pull request in `kronostechnologies/agent-toolkit` with these file changes, with a
   description that references this workflow and this repository's triggering commit, and that
   explicitly lists which files were added/updated/deleted.

## Output tree

```text
plugins/org-standards/.apm/
  instructions/
    org-standards.instructions.md
    ci-workflows.instructions.md
    dependencies.instructions.md
    testing.instructions.md
    project-setup.instructions.md
    rest-api.instructions.md
  skills/
    feature-flags/SKILL.md
    release-and-versioning/SKILL.md
    rest-api-design/
      SKILL.md
      references/
        errors.md
        bulk-and-async.md
        versioning.md
        pagination-and-data-formats.md
        caching-concurrency-idempotency.md
        openapi-contract.md
```

(All paths above are relative to `./agent-toolkit/plugins/org-standards/.apm/`.)

Design intent: `org-standards.instructions.md` is the only file with `applyTo: "**"` — every
agent pays its cost on every request, so keep it short (target ~70-75 lines). Everything else is
scoped so it only loads when an agent is actually touching matching files, or (for skills and
their references) invoked on demand. Do not put the REST rules into the always-on instructions.

### `instructions/org-standards.instructions.md`

Frontmatter:
```yaml
---
description: Organization-wide Architecturally Significant Requirements (ASRs) for all Equisoft AWT repositories.
applyTo: "**"
---
```

Content, in this order:
- Title `# Organization Standards — ASR Instructions` and a brief intro stating this file defines
  core technical standards and constraints for AI coding agents across **Equisoft AWT**
  repositories (do not say "Kronos/Equisoft organization"), derived from ASRs and engineering
  documentation, and that ecosystem-specific rules live in separate scoped instruction files
  (listed at the end of this file) that load automatically when relevant.
- **Languages & Stack**: primary languages (Kotlin backend, TypeScript frontend), PHP legacy-only
  statement, and the ASR-05 language selection criteria (compile-time, strongly-typed, null-safe,
  community/tooling) as a short bullet list — no tooling table here (that duplication now lives in
  each ecosystem's own instruction package: `kotlin-backend`, `frontend`, `php-backend`).
- **Organization-Wide Prohibited Patterns**: only the patterns that are NOT ecosystem-specific
  (ecosystem-specific ones like `any`-typing, `console.log`/`System.out`, live in the ecosystem
  packages instead):
    - No committing secrets/credentials/API keys
    - No TODO/FIXME without a Jira ticket identifier
    - No disabling CI checks, CODEOWNERS, or branch protections
    - No silent exception catching (empty catch blocks or swallowed errors) — this one is
      language-agnostic enough to keep here even though ecosystem files also restate it
- **REST APIs (ASR-15)**: a 1-2 line digest: for designing, implementing, reviewing or modifying
  any HTTP API, OpenAPI specification, or generated client, load the `rest-api-design` skill.
  Do not copy its substantive rules into this always-on file.
- **Where to find more**: a short bullet list pointing at the other instruction files and skills
  by name and one-line purpose, e.g. "CI/CD and security scanning rules — see
  `ci-workflows.instructions.md` (loads automatically when editing `.github/workflows/**` or
  `.github/actions/**`)", one line per file/skill in the tree above. Include a pointer to
  `rest-api.instructions.md` (automatically loaded for OpenAPI/Spectral files) and the
  `rest-api-design` skill (invoked for HTTP API/SDK work).

### `instructions/ci-workflows.instructions.md`

Frontmatter:
```yaml
---
description: CI/CD, code quality gating, and security scanning standards for GitHub Actions workflows.
applyTo: ".github/workflows/**,.github/actions/**"
---
```

Content:
- From ASR-06: GitHub Actions as the CI platform; CI runs on every PR and every version tag;
  merges to `main` or new tags produce a deployable artifact.
- From ASR-07/ASR-08: zero tolerance for style/quality violations, SARIF reports uploaded to GHAS,
  all violations resolved before merge.
- From ASR-10: SAST scans on PRs and tags, SARIF to GHAS, critical/high severity blocks merge,
  exact retention period from ASR-10 (state it in months).
- From ASR-11: SBOM generated every run and published to GitHub Dependency Graph, merge blocked on
  critical/high vulnerabilities or non-compliant licenses, secret scanning via GHAS.
- GitHub Actions must be pinned to an exact commit SHA, not a floating tag (from ASR-14's
  GitHub-Actions ecosystem row) — include the same example as today's file:
  `uses: actions/checkout@b4ffde65f46336ab88eb53be808477a3936bae11 # v4.1.1`.

### `instructions/dependencies.instructions.md`

Frontmatter:
```yaml
---
description: Dependency pinning, lockfile, and stability rules for all package ecosystems.
applyTo: "**/package.json,**/yarn.lock,**/composer.json,**/composer.lock,**/*.gradle.kts,**/libs.versions.toml,**/pyproject.toml,**/uv.lock,**/renovate.json*"
---
```

Content, from ASR-14 in full:
- Pin exact versions (or commit SHAs for GitHub Actions); lockfiles must be committed.
- Stability period in exact days from ASR-14, enforced via Renovate `minimumReleaseAge`.
- Renovate is the organization-standard dependency lifecycle tool.
- Internal package exemptions: `@equisoft/*`, `@kronostechnologies/*`, `@wealthelements/*`,
  `equisoft-actions`.
- Per-ecosystem rules table: JS/TS (Yarn Berry), PHP (Composer), Python (uv), Kotlin/Java
  (Gradle), GitHub Actions (SHA pinning) — same columns as today's file.

### `instructions/testing.instructions.md`

Frontmatter:
```yaml
---
description: Unit testing requirements — coverage, isolation, and structure.
applyTo: "**/*.test.*,**/*.spec.*,**/*Test.php,**/*IT.php,**/*Test.kt,**/tests/**,**/test/**"
---
```

Content, from ASR-09 in full: all tests must pass and never be skipped in CI; exact minimum
coverage percentage from ASR-09; tests isolated from external dependencies via
mocking/stubbing; AAA (Arrange-Act-Assert) pattern, one aspect per test; coverage reports
generated and uploaded per pipeline run (JaCoCo for Kotlin, Clover for PHP, Jest's own reporter
for TS — pull the exact tool names from `docs/analysis-tooling.md`).

### `instructions/project-setup.instructions.md`

Frontmatter:
```yaml
---
description: Repository onboarding — required tools and Makefile conventions.
applyTo: "Makefile,.tool-versions"
---
```

Content, from ASR-02: required tools table (Git, Docker, Make, asdf-vm) and their purpose; every
repository must have a `.tool-versions` file and a `Makefile` with the standard targets; Makefile
targets table (all, setup, check, test, compile, package, package.image) with the same
idempotency note as today's file.

### `instructions/rest-api.instructions.md`

Frontmatter:
```yaml
---
description: Load REST API design guidance when editing OpenAPI contracts or Spectral configuration.
applyTo: "**/openapi*.yaml,**/openapi*.yml,**/openapi*.json,**/*.openapi.yaml,**/*.openapi.yml,**/*.openapi.json,**/swagger*.yaml,**/swagger*.yml,**/swagger*.json,**/.spectral*"
---
```

Keep the body to a short pointer: when editing an HTTP API contract or its lint configuration,
load the `rest-api-design` skill before making changes. Do not repeat ASR-15 rules here. These
globs intentionally target recognizable spec files, not all source code or all YAML/JSON.
The skill description and the always-on pointer cover endpoint and SDK work without loading
the full guidance for unrelated code edits.

### `skills/feature-flags/SKILL.md`

Frontmatter:
```yaml
---
name: feature-flags
description: Integrate Split.io feature flags and expose the required targeting attributes, per Equisoft AWT ASR-12.
---
```

Content, from ASR-12: Split.io is the org-standard feature flag system; flags preferred over
feature/release branches for isolating in-progress work; all applications integrate the Split.io
SDK; flags reviewed and removed when no longer relevant; the targeting attributes table (User ID,
appVersion, env, site, lang, organizations) with the same notes as today's file.

### `skills/release-and-versioning/SKILL.md`

Frontmatter:
```yaml
---
name: release-and-versioning
description: Branching, code review, and release/versioning conventions for Equisoft AWT repositories, per ASR-01/ASR-03/ASR-04.
---
```

Content:
- From ASR-01: GitHub as the single source of truth, protected `main` branch, PR-only merges,
  Jira ticket identifier required in PR titles, branching prefixes (`feature/`, `release/`,
  `dev/`) with the feature-flag preference note.
- From ASR-04: one peer reviewer minimum, no self-review, CODEOWNERS mandatory, approvals
  invalidated by new pushes, access roles (all team members, Release Operators, Leadership).
- From ASR-03: SemVer, versions tracked as Git tags rather than in manifests, only Release
  Operators create tags, tags only against `main` or `release/` branches, the major-vs-minor
  guidance for breaking vs. non-breaking changes.

### `skills/rest-api-design/SKILL.md` and `skills/rest-api-design/references/`

Frontmatter for `SKILL.md`:
```yaml
---
name: rest-api-design
description: Use when designing, implementing, reviewing or modifying an HTTP API, route, OpenAPI specification, or generated client in an Equisoft AWT repository, per ASR-15.
---
```

Make the skill a concise, actionable entry point (under 500 lines and 5000 tokens) synthesized
from `asr/ASR-15_rest-api-design.md`, not a copy of its rationale or alternatives. Include:
- Scope: all new HTTP APIs, redesigned/next-major existing APIs; existing APIs migrate
  opportunistically; internal BFFs consumed only by their own frontend and deployed in lockstep
  are exempt from **section 9 versioning and breaking-change rules only**. Non-HTTP interfaces
  are out of scope.
- Resource paths: plural nouns, no CRUD verbs, max two resource levels, `{uuid}` for the acted-on
  resource and qualified parent UUIDs, last-resort action ladder (state → outcome → relationship
  → verb sub-resource), no trailing slash/extension.
- Casing table from section 4 (camelCase paths/parameters/properties, SCREAMING_SNAKE_CASE
  enums, hyphen-separated headers, acronyms as ordinary words).
- Methods from section 3: GET/POST/PUT/PATCH/DELETE purposes, full writable PUT replacement,
  PATCH on an existing resource using `application/merge-patch+json` (absent/clear/replace,
  recursive objects, wholesale arrays, atomicity), no bulk PATCH, no client-writable
  server-managed properties; request properties that are unknown, read-only or immutable are
  rejected with `400`, not ignored.
- Restricted application status set from section 5, distinguished from framework/ingress codes;
  key success response requirements from section 6 (`Location`, `ETag`, full representation on
  `200`), RFC 9457 problem details for application errors with `code` and `traceId`.
- JSON object collection envelope with `items` and `pagination`, paginating unbounded lists
  from their first release; major version as the first path segment; OpenAPI 3.x as contract
  and Spectral `spectral:oas` in CI with zero tolerance.

For details, explicitly direct the agent to **load only the relevant reference file(s) when
the task touches that area**, before implementing or reviewing it. Do not eagerly load all
references. Each reference is derived from the named ASR-15 Decision sections:

| File | Source and when to load |
|---|---|
| `references/errors.md` | Sections 5 and 7: full status-code decision tree, framework-generated codes, RFC 9457 example, validation `errors[]` and `pointer`/`in`; load when changing errors, validation or status handling. |
| `references/bulk-and-async.md` | Sections 3 and 6: bounded bulk DELETE/create/replace, `202` operations lifecycle, async replay and retention; load for batch or long-running operations. |
| `references/versioning.md` | Sections 1 and 9: breaking versus non-breaking contract changes, versioned paths, deprecation and sunset; load for versioning, compatibility or deprecation work. |
| `references/pagination-and-data-formats.md` | Sections 8, 10 and 11: media types, page/cursor pagination, filtering and sorting, identifiers/timestamps/money/null serialization; load for collections, pagination or API data schemas. |
| `references/caching-concurrency-idempotency.md` | Sections 13 and 14: trace context, idempotency-key scope/replay, ETag/If-Match, caching and If-None-Match; load for writes, retries, conditional requests or caching. |
| `references/openapi-contract.md` | Sections 12 and 15: TLS/OAuth/security, OpenAPI schemas including patch/three-state SDK fields, Spectral and generated SDKs; load for endpoint, spec, SDK or auth/authorization changes. |

Cover every applicable normative requirement in ASR-15 Decision sections 1-15 in **one**
appropriate skill or reference file. Core rules above may be briefly named in the reference
loading directions, but do not duplicate their detailed text. Resolve cross-cutting sections
by placing each rule in one file and pointing to it elsewhere as needed. The workflow should
not publish the 900-line ASR itself, or the pre-APM raw-URL topic file.

## Formatting rules (all files)

- Do NOT include `<!-- Customize: ... -->` HTML comments — these are the canonical
  organization-wide reference and should not contain extension points.
- Use bold for emphasis on key terms; use tables for structured data; use bullet lists for rules.
- Keep the tone directive: address the AI agent as "you" where appropriate.
- Every exact number (coverage percentage, stability days, retention months) must be read from the
  relevant ASR file, never hardcoded from this prompt or copied from a previous run.
- Skill descriptions must lead with the task that triggers them and be at most 1024 characters.
  Put long-tail guidance in `references/`; load it explicitly on demand from the skill.
- Do not introduce hidden Unicode characters (such as zero-width spaces) into generated files.
