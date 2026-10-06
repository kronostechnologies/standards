# Bulk and Asynchronous Operations (ASR-15 sections 3 and 6)

## Server-managed properties (all write methods)

A **server-managed property** is assigned or derived by the server (identifiers, audit timestamps such as
`createdAt`/`updatedAt`, computed values, server-owned state). No write body on any method may set one;
send `400` if a request tries. These rules apply to bulk operations exactly as to single-resource ones.

## Bulk operations

- **Bulk delete** is `DELETE` on the collection URL with a repeated `uuid` query parameter:
  `DELETE /v1/users?uuid=a&uuid=b`. No request body, no comma-separated lists.
- `DELETE` on a collection with no `uuid` parameter MUST be rejected with `400`; never delete everything.
- Document and enforce a **maximum number of identifiers**. Exceeding it is rejected with `400`, never
  truncated. Set it low enough to be hit before the ingress URI limit produces an undocumented `414`.
- De-duplicate repeated identifiers server-side; duplicates are not an error.
- Bulk `DELETE` stays idempotent: unknown or already-deleted identifiers do not fail the request.
- Bulk operations are **atomic** by default: if any part fails, none applies. Per-item partial success is
  allowed only where atomicity is genuinely impossible; it MUST be documented and return `200` with the
  outcome for every requested identifier. Never use `207 Multi-Status`.
- Fully successful atomic bulk delete returns `204` with no body; per-item reporting returns `200` with the result body.
- Delete by **predicate** (`DELETE /v1/sessions?expiredBefore=...`) is optional, requires an explicit
  `confirmBulkDelete=true` (else `400`), must document both, and MUST NOT be combined with `uuid`.
- Bulk **create** and **replace** MUST NOT post an array to the collection URL. Default to individual requests.
  A genuine batch is a `POST` to a collection-level verb sub-resource (`POST /v1/users/bulkCreate`) with a
  documented maximum batch size and the same atomicity rules.
- **Bulk `PATCH` is forbidden**: a merge patch is defined against one resource's state.
- A bulk operation MUST NOT accept `If-Match`; use the single-resource operation when optimistic concurrency is needed.
- A bulk operation that cannot finish within its request budget follows the asynchronous path below.

## Creation and update responses

- A `POST` to a verb sub-resource that creates nothing returns `200` with a body, `204`, or `202` for async work; NEVER `201`.
- A `PUT` that creates a resource follows the creating-`POST` rules: `201` plus `Location`.
- Responses to creating `POST`/`PUT` MUST NOT be cached (see `caching-concurrency-idempotency.md`).

## Asynchronous operations

Any `POST`, `PUT`, `PATCH` or `DELETE` that cannot complete within its request budget MUST return `202 Accepted`
with a relative `Location` pointing at an **operation resource**. `GET` MUST NOT return `202`.

- `202` means accepted, **not completed**: no target representation in the body, and do not assume the change took effect.
- The operation resource is the shared top-level collection `GET /v1/operations/{uuid}` (shared model per service).
- It exposes `uuid`, `status`, `createdAt`, `updatedAt` (formats in `pagination-and-data-formats.md`).
- `status` is `PENDING`, `RUNNING` (non-terminal), `SUCCEEDED` or `FAILED` (terminal, never change afterwards).
- On `SUCCEEDED`, include `resourceUrl`, a relative URL to the affected resource (what a synchronous `201` would
  return in `Location`). If no single resource is affected (e.g. async bulk delete), omit it and document that.
- On `FAILED`, the poll still returns `200`, with an `error` member holding a full RFC 9457 problem document
  (including `code` and, where applicable, `errors`). `error.status` is the status the synchronous equivalent
  would have returned, not `200`.
- Keep terminal operations retrievable for a documented minimum retention. Unknown or expired operations return
  `404`. If the operation accepts `Idempotency-Key`, retention MUST be at least the key retention (see
  `caching-concurrency-idempotency.md`).
- Authorize reads of an operation with the same rules as the request that created it.
- Non-terminal operation resources are served with `Cache-Control: no-store`.
- A retry with the same `Idempotency-Key` as an in-flight or completed async operation returns the original
  `202` referencing the **same** operation resource.
- Changing an operation from synchronous (`200`, `201`, `204`) to `202` is a **breaking change** (see `versioning.md`).
