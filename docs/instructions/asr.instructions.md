# Organization Standards — ASR Instructions

This file defines technical standards and constraints for AI coding agents working in any repository within the
Kronos/Equisoft organization. All guidelines are derived from Architecturally Significant Requirements (ASRs) and
engineering documentation. You **must** follow these rules when generating, modifying, or reviewing code.

---

## Languages & Stack

**Primary languages:** Kotlin (backend), TypeScript (frontend).  
**PHP** is **legacy only** — do not start new PHP projects or add new PHP features.

### Language Selection Criteria (ASR-05)

A language must satisfy all of the following criteria to be adopted:

- **Compile-time** execution model
- **Strongly typed** with static analysis support
- **Null-safe** (built-in null safety preferred)
- Easy to learn with accessible, high-quality documentation
- Reasonable public library and project ecosystem
- Strong, active community with regular updates and support
- Rich tooling support (IDE, IntelliSense, CLI)

### Per-Language Tooling

| Language / Ecosystem | Code Style | Code Quality | Test Runner | Test Coverage | SAST |
|---|---|---|---|---|---|
| **Kotlin (Gradle)** | Detekt | Detekt | JUnit | JaCoCo | CodeQL |
| **TypeScript / JS / CSS (Yarn)** | ESLint, Stylelint | ESLint, Stylelint | Jest | Jest | CodeQL |
| **PHP (Composer)** _(legacy)_ | PHP CodeSniffer | Psalm | PHPUnit | PHPUnit | Psalm Taint Analysis |
| **Python** | pycodestyle | pytype | pytest | — | CodeQL |

### Prohibited Patterns

> These conventions are enforced by the per-language tooling above (ESLint, Detekt, Stylelint, etc.) and are not defined in a standalone ASR.

- No `any` or unchecked type assertions in TypeScript
- No suppressing linter/compiler warnings without an explanatory comment
- No committing secrets, credentials, or API keys
- No `TODO` or `FIXME` without a Jira ticket identifier
- No disabling CI checks, CODEOWNERS, or branch protections
- No `System.out`/`println`/`console.log` for logging — use structured logging
- No silent exception catching (empty catch blocks or swallowed errors)

### Code Style Preferences

> These conventions are enforced by the per-language tooling above (ESLint, Detekt, Stylelint, etc.) and are not defined in a standalone ASR.

- Prefer immutability: use `val` in Kotlin, `const`/`readonly` in TypeScript
- Prefer `data class` for DTOs in Kotlin
- Use named exports in TypeScript (avoid default exports)
- File naming: `kebab-case.ts` for TypeScript, `PascalCase.kt` for Kotlin
- Prefer early returns to reduce nesting
- Use structured logging with key-value pairs
- Use sealed classes or `Result` types for expected failures in Kotlin
- Use explicit error types or discriminated unions for errors in TypeScript

---

## Project Setup & Conventions

### Required Tools (ASR-02)

Every developer workstation and CI environment must have the following tools installed:

| Tool | Purpose |
|---|---|
| **Git** | Version control |
| **Docker** | Containerized dependencies and builds |
| **Make** | Standardized task runner |
| **asdf-vm** | Language and tool version management |

- Every repository **must** include a `.tool-versions` file specifying exact tool versions for asdf-vm.
- Every repository **must** include a `Makefile` with the standard targets below.

### Makefile Targets

All targets must be **idempotent**. If a target is not applicable (e.g., `compile` for PHP), it must still be present as a no-op.

| Target | Description |
|---|---|
| `all` | Default target. Runs `setup`, `check`, `test`, `compile`, and `package`. Run after cloning to verify the repository is in a stable state. |
| `setup` | Brings the repository into a usable state: checks prerequisites, installs asdf-vm tool versions, installs dependencies. |
| `check` | Runs all static checks (linting, type-checking, style). Fails on any violation. |
| `test` | Runs all tests. Fails on any test failure. |
| `compile` | Runs all compilation and transpilation steps. |
| `package` | Creates all distributable packages. |
| `package.image` | Builds container images for distribution. |

---

## Repository & Versioning

### Repository Management (ASR-01)

- **VCS:** GitHub is the single source of truth for all code.
- The **`main` branch is protected**: merges are only allowed via pull requests after successful reviews and automated checks.
- **PR titles must include a Jira ticket identifier** for traceability.

### Branching Strategy

| Prefix | Purpose |
|---|---|
| `feature/` | Specific features or bug fixes; merged via PR after code review. Prefer feature flags over long-lived feature branches. |
| `release/` | Protected release branches; merges via PR only. Prefer feature flags over release branches when possible. |
| `dev/` | Ongoing development work. |

### Code Review Requirements (ASR-04)

- All pull requests require **at least one peer reviewer** from the owning team.
- **Self-review is not permitted.**
- **CODEOWNERS** is mandatory and defines required reviewers per path.
- Approvals are **invalidated** when new changes are pushed after approval.
- Access roles:
  - **All team members** — peer review required on all PRs
  - **Release Operators** — can create release branches, merge into release branches, and create version tags
  - **Leadership** — administrative access for repository governance

### Versioning (ASR-03)

- All projects follow **Semantic Versioning (SemVer)**: `MAJOR.MINOR.PATCH`.
  - `MAJOR`: incompatible API changes
  - `MINOR`: backward-compatible functionality or regular release cycle
  - `PATCH`: backward-compatible bug fixes
- Versions are tracked as **Git tags**, not in package manifests (`package.json`, `build.gradle`, `composer.json`).
- Only **Release Operators** may create version tags.
- Tags must be created against commits on `main` or a `release/` branch.
- Major releases are **only** for breaking public API changes; major visual changes or dependency updates that do not affect the public API are minor releases.

---

## Code Quality & CI

### Continuous Integration (ASR-06)

- **GitHub Actions** is the CI platform for all pipelines.
- CI runs on every pull request and on every version tag.
- Merges to `main` or new version tags produce a deployable build artifact.

### Code Style & Quality — Zero Tolerance (ASR-07, ASR-08)

- **SARIF reports** are generated and uploaded to GitHub Advanced Security (GHAS) for every pull request.
- The CI **fails** if any new style or quality violation is introduced by a pull request.
- **All violations must be resolved before a PR can be merged.** There are no exceptions.

### Unit Tests (ASR-09)

- All unit tests **must pass** on every pull request — tests must never be ignored or skipped in CI.
- **Minimum 80% code coverage** for all new code and significant changes.
- Tests must be **isolated** from external dependencies (databases, external services); use mocking and stubbing.
- Tests must follow the **Arrange–Act–Assert (AAA)** pattern and focus on a single aspect per test.
- **JaCoCo** coverage reports are generated and uploaded to GitHub by each pipeline run.

### Static Application Security Testing / SAST (ASR-10)

- SAST scans run on every pull request **and** on every version tag.
- SARIF reports are uploaded to **GitHub Advanced Security (GHAS)**.
- **Critical or high-severity vulnerabilities block merge** and must be resolved before a PR can be merged.
- SAST results for default branches must be **retained in GHAS for a minimum of 15 months** (SOC 2 compliance requirement).

### Software Composition Analysis / SCA (ASR-11)

- An **SBOM (Software Bill of Materials)** is generated on every pipeline run and published to the **GitHub Dependency Graph**.
- CI **blocks merge** if a pull request introduces:
  - A dependency with **critical or high-severity vulnerabilities**
  - A dependency with a **non-compliant license**
- **Secret scanning** is handled via **GHAS**; all commits are scanned automatically and potential leaks trigger an alert.

---

## Dependency Management

### Pinning & Stability (ASR-14)

- **All dependencies must be pinned to exact versions** (or commit SHAs for GitHub Actions).
- **Lockfiles must be committed** to version control and kept up to date.
- **Stability period:** Third-party dependencies must reach a minimum of **7 stability days** before adoption or update (enforced via Renovate `minimumReleaseAge`).
- **Renovate** is the organization-standard tool for automated dependency lifecycle management and must be configured and running in every repository.

### Internal Package Exemptions

The following internal packages are **exempt** from pinning validations, SHA requirements, and the 7-day stability rule:

- `@equisoft/*`
- `@kronostechnologies/*`
- `@wealthelements/*`
- `equisoft-actions` (internal GitHub Actions)

### Per-Ecosystem Rules

| Ecosystem | Package Manager | Lockfile | Pinning Requirement |
|---|---|---|---|
| **JS / TS** | **Yarn Berry** | `yarn.lock` (must be committed) | Exact versions in `package.json`; ranges allowed for published libraries if lockfile is committed |
| **PHP** _(legacy)_ | Composer | `composer.lock` (must be committed) | Exact versions in `composer.json`; if not possible, use branch name + commit SHA |
| **Python** | **uv** | `uv.lock` (must be committed) | Exact versions; Renovate must be explicitly configured for the `uv` ecosystem |
| **Kotlin / Java** | Gradle | Gradle lockfiles (optional due to lack of proper tooling) | Gradle Version Catalogs (`libs.versions.toml`) strongly recommended |
| **GitHub Actions** | — | — | **Must pin to exact commit SHA**, not a floating tag; e.g., `uses: actions/checkout@b4ffde65f46336ab88eb53be808477a3936bae11 # v4.1.1` |

---

## Feature Flags

### Split.io (ASR-12)

- **Split.io** is the organization-standard feature flag management system.
- Feature flags are preferred over feature branches and release branches for isolating in-progress work.
- All applications must integrate with the **Split.io SDK** to evaluate flags at runtime.
- Feature flags must be periodically reviewed and removed when no longer relevant.

### Targeting Attributes

You must expose the following attributes to Split.io for flag targeting:

| Attribute | Format / Notes |
|---|---|
| **User ID** | Global user ID from account-service (not application-specific). Use `anonymous` for non-user contexts (e.g., scheduled tasks). |
| **appVersion** | SemVer string of the running application. |
| **env** | Runtime environment: `development`, `staging`, `production`, etc. |
| **site** | `ca`, `us`, or a dedicated customer identifier. |
| **lang** | User language preference in ISO 639-1 format (e.g., `fr`, `en`). |
| **organizations** | Comma-separated list of organization IDs the current user belongs to. |

---

## REST API Design (ASR-15)

Applies to **all new HTTP APIs**. Existing APIs migrate opportunistically, at the latest at their next major version.
Internal BFF endpoints (consumed only by their own frontend, deployed in lockstep) are exempt from **versioning** only.

### Paths & Resources

- Resources are **plural nouns**; the HTTP method carries the action — no verbs in CRUD paths (`GET /v1/users`, not `GET /v1/getUsers`).
- Nest only for true ownership, **max two resource levels**. Flatten deeper hierarchies into query parameters.
- No trailing slash, no file extension. Public identifiers are **UUIDs** — never expose database sequence IDs.
- Non-CRUD actions are a `POST` to a verb sub-resource, verb last: `POST /v1/users/{userUuid}/activate`.

### Casing — camelCase Everywhere

| Element | Convention | Example |
|---|---|---|
| Path segments | camelCase | `/v1/oauthClients` |
| Path parameters | camelCase | `/v1/files/{fileUuid}` |
| Query parameters | camelCase | `?includeDeleted=true&pageSize=50` |
| JSON properties | camelCase | `ownerOrganizationUuid` |
| Enum values | `SCREAMING_SNAKE_CASE` | `PENDING_ACTIVATION` |
| HTTP headers | `Kebab-Case` | `Retry-After` |

Acronyms are ordinary words: `oauthClientId`, not `OAuthClientID`. **No kebab-case or snake_case** in paths, parameters, or JSON.

### HTTP Methods

| Method | Purpose | Idempotent | Success |
|---|---|---|---|
| `GET` | Retrieve. No request body, no side effects. | Yes | `200` |
| `POST` | Create, or non-CRUD action. | No | `201` / `202` / `200` |
| `PUT` | **Full** replacement or upsert. Absent properties are cleared. | Yes | `200` / `204` |
| `PATCH` | Partial update of an **existing** resource — **JSON Merge Patch (RFC 7396)**, `application/merge-patch+json`. | Yes | `200` / `204` |
| `DELETE` | Remove. | Yes | `204` |

**`PUT` vs `PATCH`** is decided by what the *client* holds: the entire representation → `PUT`; a subset, or a resource with writable properties this client neither knows nor owns → `PATCH`.

**Merge patch semantics.** Absent leaves unchanged, `null` clears, any other value replaces. **Arrays are replaced wholesale** — there is no element-level operation, so model the element as an addressable sub-resource (`DELETE /v1/users/{uuid}/roles/{roleUuid}`) when you need one. **Nested objects merge recursively.**

### Status Codes — Restricted Set

The restriction covers what the **application** deliberately returns. Codes emitted by the framework, ingress or proxy are expected behaviour, not violations.

**Application-returned:** **`200`, `201`, `202`, `204`, `400`, `401`, `403`, `404`, `409`, `412`, `422`, `428`, `429`, `500`, `503`**.
Declared per operation in OpenAPI; every `4xx`/`5xx` carries problem details. `412`/`428` apply only where optimistic concurrency is supported.

**Framework-emitted:** **`304`** (conditional `GET`), **`405`**, **`406`**, **`413`**, **`414`**, **`415`**, **`502`**, **`504`**.
Never implemented by hand, never caught and remapped to an application code. Rendered as problem details where the framework allows it — responses generated by ingress (`502`, `504`, upstream `413`) cannot be, so consumers must tolerate a non-problem-details error body. Declared once as common responses, not per operation.

Any other code requires a documented exception.

- `401` = "we don't know who you are" (must include `WWW-Authenticate`); `403` = "we know, and you may not".
- `400` = structural/syntactic error detectable from the schema; `422` = business-rule violation. When in doubt, `400`.
- `403` vs `404`: return `404` when the resource's existence is itself confidential.
- `409` = server state conflict; `422` = request content problem.
- `412` = a precondition was sent and did not hold; `428` = none was sent and the operation requires one.
- `405` must not be masked as `404` — routing found the path and rejected only the method.
- `500` is always a defect — never returned for anything the client caused.
- `429` must include `Retry-After`; `503` should.

### Creation & Update Responses

- Creating `POST` (and creating `PUT`) → **`201` + relative `Location` header**, and should return the created representation in the body.
- Async work → **`202` + `Location`** pointing at a pollable status resource.
- Action `POST` that creates nothing → `200` with body or `204` without. Never `201`.
- An update returning `200` returns the **full updated representation**, not a fragment or an echo of the request. Prefer `200` over `204`, and never `204` when the operation changes properties the client did not send (recomputed totals, derived status, `updatedAt`).
- A successful `PUT` / `PATCH` returns the **new `ETag`**, including on `204`, so successive updates need no intervening `GET`.
- Creation and update responses are not cached; see caching below.

### Errors — RFC 9457 Problem Details

All `4xx`/`5xx` responses **returned by the application** use `application/problem+json` (framework- and ingress-generated errors are covered under status codes above):

```json
{
  "type": "https://errors.ca.equisoft.io/common/validation-failed",
  "title": "The request is invalid.",
  "status": 400,
  "detail": "The request body failed validation.",
  "instance": "/v1/users",
  "code": "VALIDATION_FAILED",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736",
  "errors": [
    { "pointer": "/emailAddress", "code": "INVALID_FORMAT", "detail": "Must be a valid email address." },
    { "in": "query", "pointer": "pageSize", "code": "OUT_OF_RANGE", "detail": "Must be between 1 and 100." }
  ]
}
```

- `code` (`SCREAMING_SNAKE_CASE`) is the machine-readable contract — consumers branch on it alone. `title`/`detail` are for humans and may be reworded.
- `traceId` is required on every error, including `5xx`. `errors[]` is required for validation failures, one entry per invalid input.
- In `errors[]`, `in` names the request part (`body` — the default, so omit it — `query`, `path` or `header`). `pointer` is a JSON Pointer for `body`, otherwise the bare parameter or header name.
- Never leak stack traces, SQL, class names, or hostnames in `detail`. Messages are English; localization is driven by `code`, never by parsing `detail`.
- One problem document per response. Application-returned error responses must be declared in OpenAPI.

### Content Types

- `application/json` for bodies; `application/problem+json` for errors; `application/merge-patch+json` for `PATCH`. A `PATCH` sent as `application/json` is rejected with `415` — never accepted as a merge patch.
- `application/octet-stream` or `multipart/form-data` for binary — do not base64 into JSON.
- Omit `charset` (always UTF-8). No vendor media types, no HAL/JSON:API.
- Top-level responses are always JSON **objects**, never bare arrays or scalars.

### Versioning

- **Major version as the first path segment, from the first release**: `/v1/users`. Never mid-path, in a query parameter, in a header, or in a media type.
- **Non-breaking** (make in place): new endpoint; new optional query parameter with a safe default; new response property; new optional request property; relaxed validation; new error `code`.
- **Breaking** (requires a new major version): removing/renaming anything; changing a type, format, or unit; adding a required request property or new constraint; adding a **server-returned** enum value (unless tolerance was documented up front); changing a status or error `code` for an existing condition; changing defaults, sort order, or pagination.
- New major versions run in parallel with the old one. Deprecated versions must return `Deprecation` (RFC 9745) and `Sunset` (RFC 8594) headers plus a `Link; rel="deprecation"`. A version must not be removed while it has active consumers.

### Collections & Pagination

Always an object with an `items` array — never a bare array:

```json
{ "items": [], "pagination": { "pageSize": 50, "nextPageToken": "...", "previousPageToken": null } }
```

- Paginate every unbounded collection **from its first release** (retrofitting is breaking).
- **Cursor pagination is the default**: `pageToken` (opaque — never parsed by clients) + `pageSize`; response returns `nextPageToken`/`previousPageToken`.
- Offset pagination (`page` 1-based + `pageSize`, returning `totalItems`/`totalPages`) only for small, stable datasets needing random page access.
- `pageSize` has a documented default and maximum; over-large requests are **clamped**, not rejected. Results must have a deterministic total order.
- Filters are individual camelCase query parameters named after the property; repeat the parameter for multiple values (no comma-separated lists). Use `<property>After`/`<property>Before` and `min<Property>`/`max<Property>` for ranges, `query` for free-text search.
- Sorting: a single `sort` parameter, comma-separated, `-` prefix for descending — `?sort=-createdAt,displayName`. Unknown sortable/filterable values → `400`.

### Data Formats

| Type | Format |
|---|---|
| Identifiers | Lowercase canonical **UUID** strings |
| Timestamps | RFC 3339 **UTC** with offset (`2025-01-15T14:32:07Z`); property names end in `At`. No epoch numbers. |
| Dates | `YYYY-MM-DD` strings — not timestamps |
| Durations | ISO 8601 (`P30D`) or an integer with the unit in the name (`timeoutSeconds`) |
| Money | `{ "amount": "1234.56", "currency": "CAD" }` — **decimal string** + ISO 4217. Never floats. |
| Language / Country | ISO 639-1 / ISO 3166-1 alpha-2 |

Absent and `null` must not mean different things in a **response** (they do in `PATCH`, per RFC 7396) — so a nullable property that is null is serialized explicitly as `null`, never omitted, or a client cannot build an unambiguous merge patch from what it read. Empty collections serialize as `[]`. Every string property has a documented maximum length.

### Security

- TLS only. `Authorization: Bearer <jwt>` (OAuth 2.0) — no custom auth headers, no API keys in query parameters.
- **Never put credentials, tokens, or personal data in URLs** (path or query) — they land in access logs and proxies.
- Authorization is enforced per operation on the server. CORS restricted to organization origins.

### Observability & Reliability

- Propagate W3C Trace Context (`traceparent`/`tracestate`); create it when absent. Surface it as `traceId` in errors. Do not introduce `X-Request-Id`-style custom correlation headers.
- Non-idempotent operations with significant side effects (payments, provisioning, outbound communications) must accept `Idempotency-Key`: replay the original response for an identical repeat, `409` when the key is reused with a different request, retained ≥ 24 h.
- Resources subject to concurrent modification should support `ETag` + `If-Match` → `412` on mismatch (or a body version property, also → `412`). Any resource accepting `PATCH` **must**. An operation may *require* `If-Match`, rejecting a request without one with `428`.

### Caching & Conditional Requests

- Every `GET` sets an explicit `Cache-Control`. Sensitive or personal data → `no-store`. Non-`GET` responses → `no-store`.
- Caller-dependent responses → `Cache-Control: private`, plus `Vary: Authorization` when cacheable at all.
- Single-resource `GET` should return a strong `ETag`. `ETag`s are opaque, change with the representation, and are scoped to the full URL including query parameters.
- `If-None-Match` match → **`304`**, no body, same `ETag` and `Cache-Control` the `200` would have carried. A `304` is not an error and carries no problem details.
- `Last-Modified` / `If-Modified-Since` may be supported additionally; `ETag` wins where both are present.

### Contract & Tooling

- **OpenAPI 3.x is the contract**, generated from or verified against the implementation.
- Every operation documents a `summary`, description, and all success responses and all error responses the application returns, with their `code` values; framework-emitted codes are declared once, not per operation. Every property documents type, format, constraints, and requiredness.
- Every `PATCH` operation declares a **dedicated patch schema**, separate from the resource schema and bound to `application/merge-patch+json`: all properties optional, clearable ones nullable, server-managed ones `readOnly: true`.
- Generated SDKs must preserve the three states of a merge-patch property — **absent, `null`, and a value**. The default optional-nullable field collapses "leave unchanged" into "clear" and corrupts data; the representation is fixed by the shared generator configuration, never decided per service.
- **Spectral** (`spectral:oas`) runs in CI and **fails the build** on violations (ASR-08 zero tolerance).
- SDKs are generated from the specification with the shared organization tooling — do not hand-write clients for internal APIs.
- Specification changes are reviewed as code, against the breaking-change rules above.
