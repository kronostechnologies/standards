---
name: rest-api-design
description: Use when designing, implementing, reviewing or modifying an HTTP API, route, OpenAPI specification, or generated client in an Equisoft AWT repository, per ASR-15.
---

# REST API Design (ASR-15)

MUST/SHOULD keywords follow RFC 2119. Rules below are the core; load the reference files for detail.

## Scope

- Applies to **all new HTTP APIs** and to existing APIs being redesigned or released under a new
  major version. Other existing APIs migrate opportunistically, at the latest at their next major
  version.
- **Internal BFFs** consumed only by their own frontend and deployed in lockstep with it are exempt
  from the **versioning and breaking-change rules (references/versioning.md) only**; every other rule applies.
- Non-HTTP interfaces (message broker contracts) are out of scope.

## Load references on demand

Load **only** the reference file(s) relevant to the task, **before** implementing or reviewing that
area. Do not load them all eagerly.

| File | Source and when to load |
|---|---|
| `references/errors.md` | Sections 5 and 7: full status-code decision tree, framework-generated codes, RFC 9457 example, validation `errors[]` and `pointer`/`in`; load when changing errors, validation or status handling. |
| `references/bulk-and-async.md` | Sections 3 and 6: bounded bulk DELETE/create/replace, `202` operations lifecycle, async replay and retention; load for batch or long-running operations. |
| `references/versioning.md` | Sections 1 and 9: breaking versus non-breaking contract changes, versioned paths, deprecation and sunset; load for versioning, compatibility or deprecation work. |
| `references/pagination-and-data-formats.md` | Sections 8, 10 and 11: media types, page/cursor pagination, filtering and sorting, identifiers/timestamps/money/null serialization; load for collections, pagination or API data schemas. |
| `references/caching-concurrency-idempotency.md` | Sections 13 and 14: trace context, idempotency-key scope/replay, ETag/If-Match, caching and If-None-Match; load for writes, retries, conditional requests or caching. |
| `references/openapi-contract.md` | Sections 12 and 15: TLS/OAuth/security, OpenAPI schemas including patch/three-state SDK fields, Spectral and generated SDKs; load for endpoint, spec, SDK or auth/authorization changes. |

## Resource paths

- Use **plural nouns** (`/v1/users`); no CRUD verbs in paths (`GET /v1/getUsers` is wrong).
- A single resource is `/v1/users/{uuid}`. Identifiers are opaque; never expose sequence IDs.
- Nest only when the child belongs to the parent: `/v1/organizations/{organizationUuid}/members`.
  Prefer also exposing a child with its own stable identifier at the top level.
- **Maximum two resource levels**; flatten deeper hierarchies or filter with query parameters.
- Name the path parameter for the acted-on resource `{uuid}`; qualify parents (`{organizationUuid}`).
  No suffix other than `Uuid`.
- No trailing slash and no file extension.

### Actions that are not CRUD

A verb is a **last resort**. Check, in order, and show that each does not fit:

1. **State change** — model a state sub-resource: `PUT /v1/users/{uuid}/status`.
2. **Outcome worth addressing** — create a resource: `POST /v1/gateways/{uuid}/runs`.
3. **Relationship change** — sub-collection membership: `PUT`/`DELETE /v1/organizations/{uuid}/members/{userUuid}`.
4. **Pure side effect** — `POST` to a **verb sub-resource**: `POST /v1/policies/{uuid}/recalculate`.

The verb MUST be the last segment and a single imperative verb. Avoid top-level verb paths
(`POST /v1/login`) unless no addressable resource is acted upon.

## Casing

| Element | Convention | Example |
|---|---|---|
| Path segments | camelCase | `/v1/oauthClients` |
| Path parameters | camelCase | `{organizationUuid}` |
| Query parameters | camelCase | `?includeDeleted=true&pageSize=50` |
| JSON properties | camelCase | `"createdAt"` |
| Enum values | `SCREAMING_SNAKE_CASE` | `"PENDING_ACTIVATION"` |
| HTTP header names | hyphen-separated | `Retry-After` |

- Treat acronyms as ordinary words: `oauthClientUuid`, `httpStatus`, `pdfDocument`.
- Never use kebab-case or snake_case in paths, parameters or JSON properties.
- Name booleans affirmatively (`includeDeleted`, not `excludeDeleted`).

## Methods

| Method | Purpose | Body | Success |
|---|---|---|---|
| `GET` | Retrieve; no side effects | MUST NOT | `200` |
| `POST` | Create, or non-CRUD action | Usually | `201`, `202`, `200`, `204` |
| `PUT` | Full replacement, or upsert at a client-controlled identifier | MUST | `200`, `201`, `202`, `204` |
| `PATCH` | Partial update of an existing resource | MUST | `200`, `202`, `204` |
| `DELETE` | Remove a resource or a bounded set | MUST NOT | `204`, `202`, `200` |

- `POST` is the only non-idempotent method; prefer `PUT` for safely retryable operations.
- `PUT` replaces **all writable properties**; absent ones are reset or cleared. Server-managed
  properties are never accepted and their absence never clears them.
- Use `PUT` when the client holds the entire representation, `PATCH` when it holds a subset; when in doubt, `PATCH`.
- `PATCH` uses **`application/merge-patch+json`** (RFC 7396), never JSON Patch:
  - Absent property = unchanged; `null` = clear (only for optional, nullable properties, otherwise `400`); other value = replace.
  - Nested objects merge recursively; **arrays are replaced wholesale**. Model element-level changes as addressable sub-resources.
  - `{}` is a valid no-op; `PATCH` never creates (`404` if missing); it is applied **atomically**.
  - No bulk `PATCH` on collection URLs.
- Requests with unknown, read-only, immutable or server-managed properties are **rejected with `400`**, never silently ignored.
- `DELETE` is idempotent: deleting an already-deleted resource returns `204`.
- Do not implement `HEAD` or `OPTIONS` by hand.

## Status codes and errors

- The application returns only: `200`, `201`, `202`, `204`, `304`, `400`, `401`, `403`, `404`,
  `409`, `412`, `422`, `428`, `429`, `500`, `503`. Framework/ingress-emitted codes (`405`, `406`,
  `413`, `414`, `415`, `502`, `504`) are expected and MUST NOT be hand-implemented or remapped.
  Load `references/errors.md` for the decision tree.
- All `4xx`/`5xx` application errors are **RFC 9457** `application/problem+json` documents carrying
  the `code` and `traceId` extensions.

## Success responses

- Creating `POST`/`PUT`: `201` with a **relative `Location`** header; return the created representation.
- `200` on update returns the **full updated representation**; use `204` only when nothing the client did not send changed.
- A successful `PUT`/`PATCH` on a resource with an `ETag` returns the **new `ETag`**.
- Every response with a body sets `Content-Type`; responses without a body do not.

## Collections and versioning

- Collections are a **JSON object** with an `items` array and a `pagination` object; never a bare array.
- Paginate any collection that can grow without bound **from its first release**.
- Every API carries a **major version as the first path segment** (`/v1/...`) from its first release.

## Contract

- Publish an **OpenAPI 3.x** specification generated from or verified against the implementation.
- Lint it in CI with **Spectral** (`spectral:oas` plus the shared organization ruleset); violations fail the build (zero tolerance).
