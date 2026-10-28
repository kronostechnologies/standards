# Status Codes and Error Handling (ASR-15 sections 5 and 7)

## Codes the application returns

Restrict deliberate responses to this set. Any other code needs an explicit, documented exception.

| Code | Use when |
|---|---|
| `200` | Successful request that returns a body. |
| `201` | A resource was created; MUST include `Location`. |
| `202` | Accepted for asynchronous processing, not yet complete. |
| `204` | Success with deliberately no body. |
| `304` | Conditional `GET`/`HEAD` whose `If-None-Match` matched the current `ETag`. |
| `400` | Malformed request: unparseable body, wrong type, missing required field, invalid enum. |
| `401` | No credentials, or invalid/expired ones. MUST include `WWW-Authenticate`. |
| `403` | Authenticated but not allowed. |
| `404` | Resource does not exist, or the caller must not know it exists. |
| `409` | Conflict with current state: duplicate key, impossible state transition. |
| `412` | A concurrency precondition was sent and did not hold. |
| `422` | Well-formed request that fails a business rule not expressible structurally. |
| `428` | Operation requires `If-Match` and the request had none. |
| `429` | Rate limit exceeded. MUST include `Retry-After`. |
| `500` | Unexpected server failure; never for anything the client could correct. |
| `503` | Dependency unavailable or load shedding. SHOULD include `Retry-After`. |

- Declare every code an operation can return in its OpenAPI operation.
- Every `4xx`/`5xx` in this group carries RFC 9457 problem details.
- `412`/`428` apply only to operations supporting optimistic concurrency; `304` only to operations emitting an `ETag`.

## Codes emitted around the application

`405`, `406`, `413`, `414`, `415`, `502`, `504` are produced by the framework, middleware, ingress or proxy.

- Do NOT implement them by hand and do NOT catch and remap them to a code from the table above.
- Render them as problem details wherever the framework allows. Consumers must tolerate a `4xx`/`5xx`
  whose body is not a problem document (`502`, `504`, and `413` enforced by ingress).
- Declare them once as common responses of the specification, not per operation.
- Configure content negotiation to serve `application/json` rather than emit `406`.

## Disambiguation

| Pair | Rule |
|---|---|
| `401` vs `403` | `401`: identity unknown. `403`: identity known, not allowed. |
| `400` vs `422` | `400`: structural/syntactic, detectable from the schema alone. `422`: semantic, needs business context. When in doubt, `400`. |
| `403` vs `404` | Return `404` when disclosing the resource's existence is itself a leak; otherwise `403`. |
| `409` vs `422` | `409`: current server state. `422`: content of the request. |
| `409` vs `412` | A failed concurrency precondition is always `412`, never `409`. |
| `412` vs `428` | `412`: precondition sent and failed. `428`: none sent, one required. |
| `404` vs `405` | `405` (path routed, method rejected) MUST NOT be masked as `404`. |
| `500` | Never for a condition the client caused; a `500` is always a defect. |

## Problem details (RFC 9457)

All application `4xx`/`5xx` responses use `application/problem+json`. The only exception is a failed
asynchronous operation, reported as a `200` poll response with the problem document embedded in `error`
(see `bulk-and-async.md`); the rules below apply equally to that embedded member.

| Member | Required | Description |
|---|---|---|
| `type` | Yes | Stable URI identifying the problem type; dereferenceable documentation preferred. |
| `title` | Yes | Short human-readable summary. |
| `status` | Yes | HTTP status code, duplicated in the body. |
| `detail` | Yes | Human-readable explanation specific to this occurrence. |
| `instance` | No | URI of this occurrence, typically the request path. |
| `code` | Yes | Extension: stable `SCREAMING_SNAKE_CASE` code consumers branch on. |
| `errors` | No | Extension: field-level details for validation failures. |
| `traceId` | Yes | Extension: W3C trace identifier of the request. |

```json
{
  "type": "https://errors.ca.equisoft.io/organization/user-not-found",
  "title": "User not found.",
  "status": 404,
  "detail": "No user exists with identifier e4c82d21-13e9-4186-988a-13940cdf191c.",
  "instance": "/v1/users/e4c82d21-13e9-4186-988a-13940cdf191c",
  "code": "USER_NOT_FOUND",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

### Validation `errors`

Validation failures MUST populate `errors`, one entry per invalid input:

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
    { "in": "query", "pointer": "pageSize", "code": "OUT_OF_RANGE", "detail": "Must be between 1 and 100." },
    { "in": "query", "pointer": "uuid[2]", "code": "INVALID_FORMAT", "detail": "Must be a UUID." }
  ]
}
```

| Member | Required | Description |
|---|---|---|
| `in` | No | `body` (default; SHOULD be omitted, consumers treat both forms as equal), `query`, `path` or `header`. No other value. |
| `pointer` | Yes | For `body`: a JSON Pointer (RFC 6901) into the request body (for `PATCH`, into the patch document). Otherwise the parameter/header name exactly as declared in OpenAPI, undecorated. |
| `code` | Yes | Stable `SCREAMING_SNAKE_CASE` code for this failure. |
| `detail` | Yes | Human-readable explanation of this failure. |

- For a repeated query parameter, `pointer` MUST carry a zero-based index suffix: `uuid[2]` is the third `uuid` value.

### Rules

- `code` is the contract: consumers branch on `code` alone; `title`/`detail` may be reworded.
  Adding a `code` is non-breaking; changing the meaning of one is breaking.
- `type` URIs are stable and MUST NOT be reused for a different problem. Use the shared `https://errors.ca.equisoft.io` namespace.
- `detail` MUST NOT contain stack traces, SQL, internal hostnames, class names or other implementation detail.
- Write messages in English; localization is the consumer's job, driven by `code` or structured extension members, never by parsing `detail`.
- A response contains exactly one problem document; report multiple failures through `errors`.
- `5xx` MUST NOT expose the underlying cause but MUST carry `traceId`.
- Use the shared problem-details model, exception mapper and type registry rather than local equivalents.
- Migrating legacy `{ "code", "message" }` errors: map each existing enum value to a `type` and `title`, keep the
  value as `code`, turn `message` into `detail`, add `errors`, and switch the content type only at a new major
  version (see `versioning.md`). Regenerate SDKs before migrating consumers. Unversioned services treat their
  existing surface as `v1` unchanged and introduce problem details in `v2`, unless all consumers can migrate at
  once with everyone's agreement recorded in the pull request. Do not defer past the next major API version.
