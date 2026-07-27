# REST API Design — ASR-15

This is a **topic instruction file**, referenced on demand from
[`asr.instructions.md`](asr.instructions.md). It is derived from
[`asr/ASR-15_rest-api-design.md`](../../asr/ASR-15_rest-api-design.md), which is the canonical decision
record — consult it for rationale, alternatives considered, and migration guidance for existing APIs.

Applies to **all new HTTP APIs**. Existing APIs migrate opportunistically, at the latest at their next major version.
Internal BFF endpoints (consumed only by their own frontend, deployed in lockstep) are exempt from **versioning and breaking-change requirements** only.

## Paths & Resources

- Resources are **plural nouns**; the HTTP method carries the action — no verbs in CRUD paths (`GET /v1/users`, not `GET /v1/getUsers`).
- Nest only for true ownership, **max two resource levels**. Flatten deeper hierarchies into query parameters.
- No trailing slash, no file extension. Public identifiers are **UUIDs** — never expose database sequence IDs.
- A verb in a path is a **last resort**. Before adding one, check in order: (1) a state change → sub-resource write (`PUT /v1/users/{uuid}/status`); (2) an outcome worth addressing → model it as a resource (`POST /v1/gateways/{uuid}/runs`, not `.../run`); (3) a relationship change → sub-collection membership (`PUT`/`DELETE /v1/organizations/{uuid}/members/{userUuid}`). Only if none fit: `POST` to a verb sub-resource, verb last (`POST /v1/policies/{uuid}/recalculate`). Doesn't apply to bulk create/replace's verb sub-resource, which expresses batching, not an action.

## Casing — camelCase Everywhere

| Element | Convention | Example |
|---|---|---|
| Path segments | camelCase | `/v1/oauthClients` |
| Path parameters | camelCase | `/v1/organizations/{organizationUuid}/members/{uuid}` |
| Query parameters | camelCase | `?includeDeleted=true&pageSize=50` |
| JSON properties | camelCase | `ownerOrganizationUuid` |
| Enum values | `SCREAMING_SNAKE_CASE` | `PENDING_ACTIVATION` |
| HTTP headers | `Kebab-Case` | `Retry-After` |

Acronyms are ordinary words: `oauthClientUuid`, not `OAuthClientUUID`. **No kebab-case or snake_case** in paths, parameters, or JSON.

## HTTP Methods

| Method | Purpose | Idempotent | Success |
|---|---|---|---|
| `GET` | Retrieve. No request body, no side effects. | Yes | `200` |
| `POST` | Create, or non-CRUD action. | No | `201` / `202` / `200` / `204` |
| `PUT` | **Full** replacement or upsert of writable properties. Absent writable properties are cleared. | Yes | `200` / `201` / `202` / `204` |
| `PATCH` | Partial update of an **existing** resource — **JSON Merge Patch (RFC 7396)**, `application/merge-patch+json`. | Yes | `200` / `202` / `204` |
| `DELETE` | Remove a resource, or a bounded set of resources (bulk). | Yes | `204` / `202` / `200` |

**`PUT` vs `PATCH`** is decided by what the *client* holds: the entire representation → `PUT`; a subset, or a resource with writable properties this client neither knows nor owns → `PATCH`.

**Server-managed properties** (identifiers, audit timestamps, computed/derived values) MUST NOT be settable through any write body. A `POST`, `PUT` or `PATCH` body containing an unknown, read-only, or server-managed property is rejected with `400`. Each such property is either absent from the write request schema, or present and marked `readOnly: true`.

**Merge patch semantics.** Absent leaves unchanged, `null` clears, any other value replaces. **Arrays are replaced wholesale** — there is no element-level operation, so model the element as an addressable sub-resource (`DELETE /v1/users/{uuid}/roles/{roleUuid}`) when you need one. **Nested objects merge recursively.** Bulk `PATCH` on a collection URL stays forbidden — a merge patch targets one resource's current state, and a collection has no single such state.

## Bulk Operations

- Bulk `DELETE` is `DELETE` on the **collection URL** with a repeated `uuid` query parameter (`DELETE /v1/users?uuid=a&uuid=b`), never a body, never comma-separated. `DELETE` on the collection URL with no `uuid` MUST be `400` — never "delete everything".
- A documented **maximum identifier count** is enforced with `400` when exceeded, never silently clamped (unlike `pageSize`).
- Atomic by default: any failure leaves every named resource unchanged. Per-item partial success is allowed only when atomicity is genuinely impossible, and then returns `200` with a per-item result body. Otherwise a fully successful bulk delete returns `204`. `207 Multi-Status` is never used.
- Filter/criteria bulk delete (`DELETE /v1/sessions?expiredBefore=...`) MAY be offered but requires an explicit `confirmBulkDelete=true`; absent, it's `400`. Never combine a filter with `uuid`.
- Bulk create/replace is a `POST` to a collection-level verb sub-resource (`POST /v1/users/bulkCreate`), never an array posted to the collection URL.
- Bulk operations never accept `If-Match` — a single `ETag` can't express a precondition over N resources; use the single-resource operation when per-resource concurrency is required.

## Asynchronous Operations

Any `POST`, `PUT`, `PATCH` or `DELETE` — not just a creating `POST` — that can't complete within its request budget returns `202 Accepted` with `Location` pointing at a shared operation resource: `GET /v1/operations/{uuid}`.

- Status is one of `PENDING`, `RUNNING`, `SUCCEEDED`, `FAILED` (`PENDING`/`RUNNING` non-terminal, others terminal and immutable).
- On `SUCCEEDED`, the operation carries `resourceUrl` (absent when nothing addressable resulted, e.g. an async bulk delete).
- On `FAILED`, the operation returns `200` with an embedded RFC 9457 problem document in an `error` member — the poll itself succeeded, so a `4xx`/`5xx` on the envelope would be wrong; `error.status` carries what the synchronous equivalent would have returned.
- Operations stay retrievable for a documented retention period after reaching a terminal state; unknown/expired is `404`. When the operation accepts an `Idempotency-Key`, this retention MUST be at least as long as the key retention below, so a replay can never reference an already-expired operation. Access follows the authorization of the request that created the operation.
- Non-terminal operations are `Cache-Control: no-store`.
- A repeated `Idempotency-Key` for an async operation MUST return the original `202` referencing the **same** operation for as long as it remains retrievable, never starting a second one.
- Switching an operation between synchronous (`200`/`201`/`204`) and `202` is a **breaking change**.
- Cancellation is out of scope.

## Status Codes — Restricted Set

The restriction covers what the **application** deliberately returns. Codes emitted by the framework, ingress or proxy are expected behaviour, not violations.

**Application-returned:** **`200`, `201`, `202`, `204`, `304`, `400`, `401`, `403`, `404`, `409`, `412`, `422`, `428`, `429`, `500`, `503`**.
Declared per operation in OpenAPI; every `4xx`/`5xx` carries problem details. `412`/`428` apply only where optimistic concurrency is supported; `304` applies only where the operation emits an `ETag`.

**Framework-emitted:** **`405`**, **`406`**, **`413`**, **`414`**, **`415`**, **`502`**, **`504`**.
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

## Creation & Update Responses

- Creating `POST` (and creating `PUT`) → **`201` + relative `Location` header**, and should return the created representation in the body.
- Async work (any method) → **`202` + `Location`** pointing at the shared operation resource; see Bulk/Async sections above.
- Action `POST` that creates nothing → `200` with body or `204` without. Never `201`.
- An update returning `200` returns the **full updated representation**, not a fragment or an echo of the request. Prefer `200` over `204`, and never `204` when the operation changes properties the client did not send (recomputed totals, derived status, `updatedAt`).
- A successful `PUT` / `PATCH` on a resource that carries an `ETag` returns the **new `ETag`**, including on `204`, so successive updates need no intervening `GET`.
- Creation and update responses are not cached; see caching below.

## Errors — RFC 9457 Problem Details

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
- In `errors[]`, `in` names the request part (`body` — the default, so omit it — `query`, `path` or `header`). `pointer` is a JSON Pointer for `body`, otherwise the bare parameter or header name; a repeated query parameter (bulk `uuid`) adds a zero-based index suffix, e.g. `uuid[2]`.
- Never leak stack traces, SQL, class names, or hostnames in `detail`. Messages are English; localization is driven by `code`, never by parsing `detail`.
- One problem document per response — except a failed async operation, which embeds the problem document in the `error` member of its `200` poll response; see Asynchronous Operations above. Application-returned error responses must be declared in OpenAPI.

## Content Types

- `application/json` for bodies; `application/problem+json` for errors; `application/merge-patch+json` for `PATCH`. A `PATCH` sent as `application/json` is rejected with `415` — never accepted as a merge patch.
- `application/octet-stream` or `multipart/form-data` for binary. Do not base64-encode binary in JSON except for a documented small payload where a separate request is impractical.
- Omit `charset` (always UTF-8). No vendor media types, no HAL/JSON:API.
- Top-level responses are always JSON **objects**, never bare arrays or scalars.

## Versioning

- **Major version as the first path segment, from the first release**: `/v1/users`. Never mid-path, in a query parameter, in a header, or in a media type.
- **Non-breaking** (make in place): new endpoint; new optional query parameter with a safe default; new response property; new optional request property; relaxed validation; new error `code`.
- **Breaking** (requires a new major version): removing/renaming anything; changing a type, format, or unit; adding a required request property or new constraint; adding a **server-returned** enum value (unless tolerance was documented up front); changing a status or error `code` for an existing condition; changing defaults, sort order, or pagination; switching an operation between synchronous (`200`/`201`/`204`) and `202`+operation-resource.
- New major versions run in parallel with the old one. Deprecated versions must return `Deprecation` (RFC 9745) and `Sunset` (RFC 8594) headers; a `Link; rel="deprecation"` header should accompany them. A version must not be removed while it has active consumers.

## Collections & Pagination

Always an object with an `items` array — never a bare array:

```json
{ "items": [], "pagination": { "pageSize": 50, "nextPageToken": "...", "previousPageToken": null } }
```

- Paginate every unbounded collection **from its first release** (retrofitting is breaking).
- **Page pagination is the default**: `page` (1-based, defaulting to `1`) + `pageSize`; responses return `page`, `pageSize`, `totalItems`, and `totalPages`.
- Cursor pagination (`pageToken` + `pageSize`) MAY be used instead for large or high-churn datasets; tokens are opaque and responses return `nextPageToken`/`previousPageToken`.
- `pageSize` has a documented default and maximum; over-large requests are **clamped**, not rejected. Results must have a deterministic total order.
- Filters are individual camelCase query parameters named after the property; repeat the parameter for multiple values (no comma-separated lists). Use `<property>After`/`<property>Before` and `min<Property>`/`max<Property>` for ranges, `query` for free-text search.
- Sorting: a single `sort` parameter, comma-separated, `-` prefix for descending — `?sort=-createdAt,displayName`. Unknown sortable/filterable values → `400`.

## Data Formats

| Type | Format |
|---|---|
| Identifiers | Lowercase canonical **UUID** strings |
| Timestamps | RFC 3339 **UTC** with offset (`2025-01-15T14:32:07Z`); property names end in `At`. No epoch numbers. |
| Dates | `YYYY-MM-DD` strings — not timestamps |
| Durations | ISO 8601 (`P30D`) or an integer with the unit in the name (`timeoutSeconds`) |
| Money | `{ "amount": "1234.56", "currency": "CAD" }` — **decimal string** + ISO 4217. Never floats. |
| Language / Country | ISO 639-1 / ISO 3166-1 alpha-2 |

Absent and `null` must not mean different things in a **response** (they do in `PATCH`, per RFC 7396) — so a nullable property that is null is serialized explicitly as `null`, never omitted, or a client cannot build an unambiguous merge patch from what it read. Empty collections serialize as `[]`. Every string property has a documented maximum length.

## Security

- TLS only. `Authorization: Bearer <jwt>` (OAuth 2.0) — no custom auth headers, no API keys in query parameters.
- **Never put credentials or tokens in URLs**, and never put personal data in path segments. Personal data MAY appear in a documented query filter or search only when identified in OpenAPI and redacted from access logs.
- Authorization is enforced per operation on the server. CORS restricted to organization origins.

## Observability & Reliability

- Propagate W3C Trace Context (`traceparent`/`tracestate`); create it when absent. Surface it as `traceId` in errors. Do not introduce `X-Request-Id`-style custom correlation headers.
- Non-idempotent operations with significant side effects (payments, provisioning, outbound communications) must accept `Idempotency-Key`. Records MUST be scoped by authenticated principal (and tenant), HTTP method and target URI — never indexed by the raw key alone — so different callers or endpoints can never collide. Replay the original response for the same request; `409` (problem document) when the same key/scope is reused with a different request, or while an in-flight repeat's outcome is still indeterminate. Keys SHOULD be retained for at least 24 h, and MUST NOT be retained longer than the referenced operation resource stays retrievable. For an async operation, "the original response" is the `202` referencing the same operation resource — a repeat never starts a second operation.
- Resources subject to concurrent modification should support `ETag` + `If-Match` → `412` on mismatch (or a body version property, also → `412`). Any resource accepting `PATCH` **must**. An operation may *require* `If-Match`, rejecting a request without one with `428`. Bulk operations never accept `If-Match`.

## Caching & Conditional Requests

- Every `GET` sets an explicit `Cache-Control`. Sensitive or personal data → `no-store`. Non-`GET` responses → `no-store`. Non-terminal operation resources (`PENDING`/`RUNNING`) → `no-store`.
- Caller-dependent responses → `Cache-Control: private`, plus `Vary: Authorization` when cacheable at all.
- Single-resource `GET` should return a strong `ETag`. `ETag`s are opaque, change with the representation, and are scoped to the full URL including query parameters.
- `If-None-Match` match (`GET`/`HEAD`) → **`304`**, no body, same `ETag` and `Cache-Control` the `200` would have carried. A `304` is not an error and carries no problem details. Comparison is **weak** (RFC 9110 §13.1.2), unlike `If-Match`; `If-None-Match: *` matches any current representation.
- `Last-Modified` / `If-Modified-Since` may be supported additionally; when both are present, `If-None-Match` takes precedence and `If-Modified-Since` is ignored.

## Contract & Tooling

- **OpenAPI 3.x is the contract**, generated from or verified against the implementation.
- Every operation documents a `summary`, description, and all success responses and all error responses the application returns, with their `code` values; framework-emitted codes are declared once, not per operation. Every property documents type, format, constraints, and requiredness.
- Every `POST`, `PUT` and `PATCH` request schema keeps server-managed properties absent or `readOnly: true` — never settable by the consumer.
- Every `PATCH` operation additionally declares a **dedicated patch schema**, separate from the resource schema and bound to `application/merge-patch+json`: all properties optional, clearable ones nullable. Dedicated write schemas are named `<Resource>Patch`, `<Resource>CreateRequest`, `<Resource>ReplaceRequest`.
- Generated SDKs must preserve the three states of a merge-patch property — **absent, `null`, and a value**. The default optional-nullable field collapses "leave unchanged" into "clear" and corrupts data; the representation is fixed by the shared generator configuration, never decided per service.
- **Spectral** (`spectral:oas`) runs in CI and **fails the build** on violations (ASR-08 zero tolerance).
- SDKs are generated from the specification with the shared organization tooling — do not hand-write clients for internal APIs.
- Specification changes are reviewed as code, against the breaking-change rules above.
