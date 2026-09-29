# ASR-15: REST API Design

Status: Accepted

## Context

Our systems are built as a set of HTTP services that communicate with each other and with our frontends. Client SDKs
(TypeScript, PHP, Kotlin) are generated from each service's OpenAPI specification, so every inconsistency in an API
surface propagates directly into the code of every consumer.

No written REST API design standard existed until now. Each service defined its own conventions, and a review of our
existing services shows meaningful divergence:

* Error payloads are mostly `{ "code": "NOT_FOUND", "message": "..." }`, but at least one service differs, and none
  expose field-level validation details.
* Collections are returned in three incompatible shapes: a named array with cursor tokens, a paginated envelope with
  page counters, and bare JSON arrays — with pagination itself split between cursors, `page`/`size`, and
  `offset`/`limit` across services.
* Almost no service is versioned. One uses a `/v1` path prefix, another embeds a version mid-path; breaking changes
  are handled ad hoc by creating a new endpoint and deprecating the old one in its description text.
* Path segments are usually camelCase, but not always, and query parameter naming is inconsistent.
* `PUT` is used for virtually every update; `PATCH` appears once across the entire organization.
* There is no convention for deprecation signalling, concurrency control, idempotency, or retry hints.

The cost is paid by consumers: every integration requires learning a new set of conventions, and generated SDKs cannot
offer a uniform developer experience. A single, explicit standard removes that cost and makes API review objective
rather than a matter of taste.

## Decision

This ASR defines how HTTP APIs are designed across the organization. The keywords MUST, MUST NOT, SHOULD, SHOULD NOT
and MAY are to be interpreted as described in RFC 2119 and RFC 8174.

### 1. Scope

* This ASR applies to **all new HTTP APIs**, and to any existing API that is being redesigned or released under a new
  major version.
* **Existing APIs** are not required to be remediated immediately. They MUST be migrated opportunistically, at the
  latest when they publish their next major version.
* **Internal backend-for-frontend (BFF) endpoints** — endpoints consumed exclusively by their own frontend, deployed in
  lockstep with it, and not exposed to any other consumer — are **exempt from the versioning and breaking requirements**
  in section 9. All other sections apply to them.
* Non-REST interfaces (message broker contracts) are out of scope.

### 2. Resource modelling and path structure

* Resources MUST be named with **plural nouns**: `/users`, `/organizations`, `/files`.
* Paths MUST NOT contain verbs for CRUD operations — the HTTP method carries that meaning (`GET /v1/getUsers` is
  incorrect; `GET /v1/users` is correct).
* A single resource is addressed by appending its identifier: `/v1/users/{uuid}`.
* Resources SHOULD be nested only when the child genuinely belongs to the parent and cannot be addressed without it:
  `/v1/organizations/{organizationUuid}/members`. When a child has its own stable identifier, it SHOULD also be
  addressable at the top level.
* Nesting MUST NOT exceed **two resource levels** (`/v1/{parents}/{parentUuid}/{children}/{uuid}`). Deeper hierarchies
  MUST be flattened, using a stable resource identifier, or query parameters for filtering instead if necessary.
* Paths MUST NOT end with a trailing slash, and MUST NOT include a file extension.
* Identifiers in paths MUST be opaque to the consumer. Database sequence identifiers MUST NOT be exposed; see
  section 11.
* The path parameter addressing the resource the operation acts upon MUST be named `{uuid}`. A parent resource in a
  nested path MUST be qualified with the parent's name: `{organizationUuid}`. Suffixes other than `Uuid` MUST NOT be
  used, since section 11 requires identifiers to be UUIDs.

#### Actions that are not CRUD

A verb in a path is a **last resort**. The effort to find a resource model MUST be made first; a verb sub-resource
is what remains when that effort genuinely fails, not a shortcut past it. Before adding one, the operation MUST be
checked against each of the following in order, and shown not to fit:

1. **A state change.** Most `activate` / `enable` / `archive` / `suspend` operations are this. Model the state as a
   sub-resource and write it: `PUT /v1/users/{uuid}/status`.
2. **An outcome worth addressing.** If the action produces something the consumer may later retrieve, that something
   is a resource, not a verb: `POST /v1/gateways/{uuid}/runs` creates and returns an addressable run — which also
   gives section 6's asynchronous `202` a resource to reference, where triggering a run is long-running.
3. **A relationship change.** Adding or removing a link between resources is membership in a sub-collection:
   `PUT /v1/organizations/{uuid}/members/{userUuid}` to add, `DELETE` on the same path to remove.

Only when an operation has no state to write, no retrievable outcome, and no relationship to change — when it is
purely a side effect — MUST it be modelled as a `POST` to a **verb sub-resource** of the resource it acts upon:

```
POST /v1/policies/{uuid}/recalculate
```

`POST /v1/gateways/{uuid}/run` is the counter-example: a run is an outcome worth addressing, so it belongs at
`POST /v1/gateways/{uuid}/runs` (rung 2), not as a verb. Likewise, `POST /v1/users/{uuid}/activate` is only a verb
sub-resource when activation has side effects beyond flipping a status — provisioning, outbound mail. When it is
just a status flip, it is `PUT /v1/users/{uuid}/status` (rung 1).

* The verb MUST be the last path segment and MUST be a single imperative verb.
* Top-level verb paths (`POST /v1/login`) SHOULD be avoided, but are acceptable for operations that do not act on an
  addressable resource.
* This ladder governs actions on a single resource. A collection-level verb sub-resource for batching — bulk create
  or bulk replace, per section 3 — is not climbing the ladder in place of a resource; the batch mechanism itself is
  what a verb expresses there, since posting an array to the collection URL is separately forbidden.

### 3. HTTP methods

A **server-managed property** is any property whose value the server assigns or derives rather than accepting from
the client — identifiers, audit timestamps (`createdAt`, `updatedAt`), computed or derived values, and state the
server alone owns. No write body, on any method, MUST be able to set one; see below and section 15.

| Method   | Purpose                                          | Safe | Idempotent | Request body | Typical success (preferred order) |
|----------|--------------------------------------------------|------|------------|--------------|-----------------------------------|
| `GET`    | Retrieve a resource or a collection              | Yes  | Yes        | MUST NOT     | `200`                             |
| `POST`   | Create a resource, or perform a non-CRUD action  | No   | No         | Usually      | `201`, `202`, `200`, `204`        |
| `PUT`    | Full replacement or upsert at a known identifier | No   | Yes        | MUST         | `200`, `201`, `202`, `204`        |
| `PATCH`  | Partial update                                   | No   | Yes        | MUST         | `200`, `202`, `204`               |
| `DELETE` | Remove a resource, or a bounded set of resources | No   | Yes        | MUST NOT     | `204`, `202`, `200`               |

* `GET` MUST NOT have side effects observable by the consumer. It MUST NOT carry a request body, because intermediaries
  are not required to forward it.
* `POST` is the only non-idempotent method. Operations that clients may safely retry SHOULD be modelled with `PUT`
  instead. When a `POST` has significant side effects, it MUST support idempotency keys; see section 13.
* `PUT` MUST replace the **writable** properties of the resource in full. Properties absent from the request body MUST
  be reset to their default or cleared — a `PUT` is not a partial update. Server-managed properties are not part of
  that representation: they MUST NOT be accepted in the request body, and their absence from it MUST NOT reset or
  clear them. `PUT` MAY create the resource when the client controls the identifier.
* `PATCH` MUST apply a partial update to an existing resource, using JSON Merge Patch; see below.
* A `POST`, `PUT` or `PATCH` body containing a property that is unknown, read-only, immutable after creation, or
  otherwise server-managed MUST be rejected with `400` and an `errors` entry pointing at it. Silently discarding it
  turns a typo, or an attempt to change `uuid`, into an apparently successful write. These properties MUST be absent
  from the request schema, or marked `readOnly: true`; see section 15.
* `DELETE` MUST be idempotent. Deleting an already-deleted resource MUST return `204`, not `404`, when the API can
  determine the resource previously existed.
* `HEAD` and `OPTIONS` are handled by the framework and MUST NOT be implemented manually.

#### Choosing between `PUT` and `PATCH`

Both methods update an existing resource; the choice is determined by what the **client** holds, not by what is
convenient to implement:

* Use `PUT` when the client holds, and is responsible for, the **entire** representation — a settings document it just
  rendered in full, or a resource whose every writable property is on the form being submitted.
* Use `PATCH` when the client holds a **subset**: it is changing two properties out of twenty, or the resource carries
  writable properties this particular client neither knows nor owns.
* When in doubt, prefer `PATCH`; see Consequences for what a `PUT` built from an incomplete view costs.

A single-property state change MAY instead be modelled as a `PUT` on a sub-resource (`PUT /v1/users/{uuid}/status`), as
described in section 2, when the property is a genuine state machine with its own transition rules and authorization.
It MUST NOT be used to give every writable property its own sub-resource: patching several properties of one resource
is a `PATCH`, not a series of non-atomic `PUT` calls.

#### Partial updates with `PATCH`

`PATCH` MUST use **JSON Merge Patch** (RFC 7396) with the `application/merge-patch+json` content type. JSON Patch
(RFC 6902) MUST NOT be used; see Alternatives Considered.

The request body is not a resource representation. It is a patch document, describing only the change:

* An **absent** property leaves the current value unchanged.
* A `null` value **clears** the property.
* Any other value replaces the current value.

Two consequences of RFC 7396 are frequently misunderstood and are stated here explicitly:

* **Arrays are replaced in full.** Merge patch has no element-level operation: sending `tags` replaces the whole array,
  and there is no way to append to it or to remove one entry. When element-level mutation is genuinely required, the
  element MUST be modelled as an addressable sub-resource (`DELETE /v1/users/{uuid}/roles/{roleUuid}`). Reaching for
  JSON Patch, or inventing an operation object inside the patch body, MUST NOT be done.
* **Nested objects are merged recursively.** Patching `{ "address": { "city": "Québec" } }` changes only `city` and
  leaves the rest of `address` intact. Replacing a nested object in full requires listing all of its properties.

The following rules apply to every `PATCH` operation:

* A `null` MUST be accepted only for properties documented as clearable — those that are optional and nullable. A
  `null` on a required or non-nullable property MUST be rejected with `400` and an `errors` entry pointing at it. It
  MUST NOT be silently ignored, and MUST NOT reset the property to a default.
* A patch containing an unknown, read-only, immutable, or server-managed property MUST be rejected per the rule in
  section 3.
* An empty patch document (`{}`) is a valid no-op. It MUST succeed and MUST NOT be rejected as an empty body.
* `PATCH` MUST NOT create a resource. When the target does not exist, it MUST return `404`. Upsert belongs to `PUT`.
* `PATCH` MUST NOT be sent to a collection URL; there is no bulk patch. See the bulk operations subsection below for
  why.
* The patch MUST be applied atomically. A patch that fails validation MUST leave the resource entirely unchanged; a
  partially applied patch is never an acceptable outcome.
* Validation failures MUST be reported per section 7, with `pointer` a JSON Pointer into the **patch document**.
* `PATCH` is idempotent: applying the same merge patch twice MUST produce the same resource state as applying it once.

#### Bulk operations

The server-managed-property and request-body rules above apply to bulk operations exactly as they do to
single-resource ones. This subsection adds the rules specific to acting on more than one resource at once.

* Bulk **delete** MUST be expressed as `DELETE` on the **collection URL**, carrying the target identifiers as a
  repeated `uuid` query parameter — `DELETE /v1/users?uuid=a&uuid=b` — per the repeated-parameter convention of
  section 10. `DELETE` MUST NOT carry a request body, bulk or otherwise, and a comma-separated identifier list MUST
  NOT be used.
* `DELETE` on a collection URL that carries no `uuid` parameter MUST be rejected with `400`. It MUST NOT be
  interpreted as a request to delete every resource in the collection.
* A bulk operation MUST document and enforce a maximum number of identifiers per request. A request exceeding that
  maximum MUST be rejected with `400`, not silently truncated (unlike `pageSize` in section 10). The maximum SHOULD
  be set low enough that it is reached, and rejected with `400`, before the request line grows past the ingress URI
  length limit that would otherwise surface as an undocumented `414`; see section 5.
* Repeated identifiers MUST be de-duplicated by the server; sending the same identifier twice is not an error.
* Bulk `DELETE` MUST remain idempotent: an identifier that does not exist, or is already deleted, MUST NOT fail the
  request, consistent with the single-resource rule above.
* A bulk operation MUST be **atomic** by default: if any part of it cannot be applied, none of it is, and the
  resources named in the request MUST be left entirely unchanged. Per-item partial success MAY be offered only where
  atomicity is genuinely impossible, and an operation that does so MUST document it, and MUST return `200` with a
  body reporting the outcome for every requested identifier. `207 Multi-Status` MUST NOT be used; it is not part of
  the status code set in section 5.
* An atomic bulk delete that succeeds in full MUST return `204` with no body. A bulk delete reporting per-item
  outcomes MUST return `200` with the result body described above.
* Deleting by **predicate** rather than by an explicit identifier list (`DELETE /v1/sessions?expiredBefore=...`) MAY
  be offered, but MUST require an explicit `confirmBulkDelete=true` query parameter. A request matching a predicate
  without that parameter MUST be rejected with `400`. Both the predicate and the confirmation requirement MUST be
  documented. A predicate MUST NOT be combined with `uuid` in the same request.
* Bulk **create** and bulk **replace** MUST NOT be expressed by posting an array to the collection URL. Individual
  requests are the default. Where a genuine batch is required, it MUST be modelled as a `POST` to a collection-level
  verb sub-resource per section 2 (`POST /v1/users/bulkCreate`), with the same documented maximum batch size and the
  same atomicity rules as bulk delete.
* Bulk `PATCH` remains forbidden: `PATCH` MUST NOT be sent to a collection URL. A merge patch is defined against one
  resource's current state, so a collection-wide merge patch has no single well-defined target — unlike a bulk
  delete or a bulk create, which name their targets, or the new resources, explicitly.
* A bulk operation MUST NOT accept `If-Match`; see section 13.
* A bulk operation that cannot complete within its request budget follows the asynchronous path of section 6, in
  place of the synchronous responses described above.

### 4. Casing

camelCase is the default casing across the API surface. The table below is authoritative: where it names a different
convention, that convention MUST be used instead.

| Element              | Convention             | Example                                               |
|----------------------|------------------------|-------------------------------------------------------|
| Path segments        | camelCase              | `/v1/oauthClients`, `/v1/serviceProfiles`             |
| Path parameters      | camelCase              | `/v1/organizations/{organizationUuid}/members/{uuid}` |
| Query parameters     | camelCase              | `?includeDeleted=true&pageSize=50`                    |
| JSON body properties | camelCase              | `"ownerOrganizationUuid"`, `"createdAt"`              |
| Enum values          | `SCREAMING_SNAKE_CASE` | `"status": "PENDING_ACTIVATION"`                      |
| HTTP header names    | hyphen-separated       | `Content-Type`, `Retry-After`                         |

* Acronyms MUST be treated as ordinary words: `oauthClientUuid`, `httpStatus`, `pdfDocument` — not `OAuthClientUUID`,
  `HTTPStatus`, or `PDFDocument`.
* HTTP header names are case-insensitive (RFC 9110). This ASR writes them in their conventional `Hyphen-Separated`
  form, and a service MUST NOT depend on the casing of a header it receives.
* kebab-case and snake_case MUST NOT be used in paths, parameters, or JSON properties. This keeps generated SDKs
  idiomatic in TypeScript and Kotlin without renaming rules.
* Boolean properties and parameters SHOULD be named affirmatively (`includeDeleted`, not `excludeDeleted` or
  `notDeleted`).

### 5. Status codes

APIs MUST restrict themselves to the status codes listed below. A small, predictable set is far more valuable to
consumers than precise but obscure semantics.

The restriction applies to what the **application** deliberately returns. A framework, ingress or proxy also emits
status codes on its own, as correct HTTP behaviour for conditions the application never sees. Those are expected, not
violations, and the two groups are governed by different rules.

#### Codes the application returns

These are chosen deliberately by the developer for a given operation.

| Code  | Name                  | Use when                                                                                      |
|-------|-----------------------|-----------------------------------------------------------------------------------------------|
| `200` | OK                    | A successful request that returns a body.                                                     |
| `201` | Created               | A resource was created. MUST include a `Location` header; see section 6.                      |
| `202` | Accepted              | The request was accepted for asynchronous processing and is not yet complete.                 |
| `204` | No Content            | The request succeeded and there is deliberately no body to return.                            |
| `304` | Not Modified          | A conditional `GET` or `HEAD` whose `If-None-Match` matched the current `ETag`; see section 14. |
| `400` | Bad Request           | The request is malformed: unparseable body, wrong type, missing required field, invalid enum. |
| `401` | Unauthorized          | No credentials were provided, or they are invalid or expired.                                 |
| `403` | Forbidden             | The caller is authenticated but not allowed to perform this operation.                        |
| `404` | Not Found             | The resource does not exist, or the caller is not allowed to know that it exists.             |
| `409` | Conflict              | The request conflicts with the current state: duplicate key, conflicting state transition.    |
| `412` | Precondition Failed   | A concurrency precondition was sent and did not hold; see section 13.                         |
| `422` | Unprocessable Content | The request is well-formed but fails a business rule that cannot be expressed structurally.   |
| `428` | Precondition Required | The operation requires `If-Match` and the request did not carry one; see section 13.          |
| `429` | Too Many Requests     | The caller exceeded a rate limit. MUST include a `Retry-After` header.                        |
| `500` | Internal Server Error | An unexpected server-side failure. Never used for anything the client could correct.          |
| `503` | Service Unavailable   | A dependency is unavailable or the service is shedding load. SHOULD include `Retry-After`.    |

* Every code in this group that an operation can return MUST be declared for that operation in the OpenAPI
  specification; see section 15.
* Every `4xx` and `5xx` in this group MUST carry RFC 9457 problem details; see section 7.
* `412` and `428` apply only to operations that support optimistic concurrency; see section 13.
* `304` applies only to operations that emit an `ETag`; see section 14.

#### Codes emitted around the application

These are produced by the framework, by shared middleware, or by the ingress or a proxy, before or around the
application code. They are permitted, and a service is not required to implement them itself.

| Code  | Name                   | Emitted when                                                                           |
|-------|------------------------|----------------------------------------------------------------------------------------|
| `405` | Method Not Allowed     | The path is routed but the method is not.                                              |
| `406` | Not Acceptable         | The `Accept` header cannot be satisfied; see section 8.                                |
| `413` | Content Too Large      | The request body exceeds the configured limit.                                         |
| `414` | URI Too Long           | The request line exceeds the configured limit.                                         |
| `415` | Unsupported Media Type | The request `Content-Type` is not supported; see section 8.                            |
| `502` | Bad Gateway            | The ingress cannot reach the service, or received an invalid response from it.         |
| `504` | Gateway Timeout        | The ingress did not receive a response from the service in time.                       |

* These codes MUST NOT be implemented by hand in application code, and MUST NOT be caught and remapped to a code from
  the first table; see the `404` vs `405` entry below for why.
* The error codes in this group SHOULD be rendered as problem details wherever the framework allows a custom error
  representation. Responses generated outside the service — `502`, `504`, and `413` when the limit is enforced by the
  ingress — cannot be, so consumers MUST tolerate a `4xx` or `5xx` whose body is not a problem document.
* They need not be declared on every operation. They SHOULD be declared once, as common responses of the
  specification.
* Content negotiation SHOULD be configured to serve `application/json` rather than emit `406`, since it is the only
  representation offered; see section 8.

Any other status code MUST NOT be used without an explicit, documented exception.

#### Disambiguation

| Pair          | Rule                                                                                                    |
|---------------|-----------------------------------------------------------------------------------------------------------|
| `401` vs `403` | `401` means "we do not know who you are"; `403` means "we know who you are, and you may not do this". A `401` MUST include a `WWW-Authenticate` header. |
| `400` vs `422` | `400` is a structural or syntactic problem detectable from the schema alone (missing field, wrong type). `422` is a semantic problem requiring business context (the end date precedes the start date, the account is not eligible). When in doubt, use `400`. |
| `403` vs `404` | When disclosing the existence of a resource is itself a leak, return `404`. Otherwise `403`. |
| `409` vs `422` | `409` is about the current state of the server (something already exists, a transition is not possible from the current state); `422` is about the content of the request. |
| `409` vs `412` | A failed concurrency precondition is always `412`, never `409`; see section 13. |
| `412` vs `428` | `412` means "you sent a precondition and it did not hold"; `428` means "you sent no precondition and this operation requires one". |
| `404` vs `405` | A `405` means routing found the path and rejected only the method. It MUST NOT be masked as a `404`. |
| `500`          | MUST NOT be returned for any condition the client caused. A `500` is always a defect. |

### 6. Response headers and bodies for creation and update

* A `POST` that creates a resource MUST return `201 Created` with a **relative** `Location` header pointing at the
  canonical URL of the new resource:

  ```
  HTTP/1.1 201 Created
  Location: /v1/organizations/e4c82d21-13e9-4186-988a-13940cdf191c
  Content-Type: application/json
  ```

* A `201` response SHOULD also return the created representation in the body, so that server-managed properties
  (identifier, timestamps, computed defaults) are available without a follow-up `GET`. When the body is deliberately
  omitted, the `Location` header remains mandatory.
* A `POST` to a verb sub-resource that does not create anything MUST return `200` with a body or `204` without one, or
  `202` when it starts asynchronous work, per the rule below. It MUST NOT return `201`.
* A `PUT` that creates a resource MUST follow the same rules as a creating `POST`: `201` plus `Location`.
* An update that returns `200` MUST return the **full updated representation**, not a fragment and not an echo of the
  request. This matters most for `PATCH`, where the client by definition does not hold the complete state and would
  otherwise need a follow-up `GET` to learn the result of its own write.
* An update MAY return `204` when the client provably gains nothing from the representation. `200` is preferred, and
  `204` MUST NOT be used when the operation changes properties the client did not send — recomputed totals, derived
  status, `updatedAt`.
* A successful `PUT` or `PATCH` on a resource that carries an `ETag` MUST return the **new** `ETag` in the response,
  including on `204`. Without it, a client performing successive updates must re-`GET` between every write, which
  defeats the concurrency control of section 13.
* Responses to a creating `POST` or `PUT` MUST NOT be cached; see section 14.
* Every response with a body MUST set an explicit `Content-Type`. Every response without a body MUST NOT set one.

#### Asynchronous operations

Any `POST`, `PUT`, `PATCH` or `DELETE` that cannot complete within its request budget MUST return `202 Accepted` with
a relative `Location` header pointing at an **operation resource** the client can poll — not only a creating `POST`.
`GET` MUST NOT return `202`: a safe method has nothing to accept.

* A `202` means the request was **accepted, not completed**. The response MUST NOT carry the target resource's
  representation, and the change MUST NOT be assumed to have taken effect until the operation reaches a terminal
  state.
* The operation resource MUST be a shared, top-level collection: `GET /v1/operations/{uuid}`. One uniform shape and
  one shared implementation per service replaces a bespoke status sub-resource per feature; see Appendix A.
* An operation resource MUST expose `uuid`, `status`, `createdAt` and `updatedAt`, following the identifier and
  timestamp conventions of section 11.
* `status` MUST be one of `PENDING`, `RUNNING`, `SUCCEEDED` or `FAILED`. `PENDING` and `RUNNING` are non-terminal;
  `SUCCEEDED` and `FAILED` are terminal and MUST NOT change afterwards.
* On `SUCCEEDED`, the operation resource MUST carry a `resourceUrl` member — a relative URL to the resource the
  operation affected, equivalent to what a synchronous `201` would have returned in `Location`. When the operation
  affects no single addressable resource (an asynchronous bulk delete, for example), `resourceUrl` MUST be absent,
  and that MUST be documented for the operation.
* On `FAILED`, the operation resource MUST return `200` — the poll itself succeeded — with an `error` member holding
  a complete RFC 9457 problem document as described in section 7, including `code` and, where applicable, `errors`.
  `error.status` MUST be the status code the synchronous equivalent of the operation would have returned; it is
  deliberately not the `200` of the envelope. See section 7 for the corresponding carve-out.
* An operation resource MUST remain retrievable for a documented minimum retention period after reaching a terminal
  state, so a client that lost its connection while the operation was running can still learn the outcome. A request
  for an unknown or expired operation MUST return `404`. When the operation accepts an `Idempotency-Key`, this
  retention period MUST be at least as long as the key retention of section 13, so that a replay can never reference
  an operation that has already expired.
* Access to an operation resource MUST be governed by the same authorization as the request that created it. A
  caller that could not have issued that request MUST NOT be able to read its outcome.
* A non-terminal operation resource MUST be served with `Cache-Control: no-store`; see section 14.
* A retried request carrying the same `Idempotency-Key` as an in-flight or completed asynchronous operation MUST
  return the original `202` and reference the **same** operation resource, not start a second one; see section 13.
* Changing an operation from a synchronous response (`200`, `201`, `204`) to `202` is a **breaking change**; see
  section 9.

### 7. Error handling

All error responses (`4xx` and `5xx`) returned by the application MUST be **Problem Details for HTTP APIs** as defined
by **RFC 9457**, serialized with the `application/problem+json` content type. Responses generated by the framework or
the ingress are covered by section 5.

The one exception is a failed asynchronous operation, reported through a `200` poll response with the problem
document embedded in an `error` member rather than as the top-level response; see section 6 for that rule.
Everywhere else in this section, "the response" means the problem document returned directly, and "a response MUST
contain exactly one problem document" below applies equally to the embedded `error` member.

#### Structure

| Member     | Required | Description                                                                                |
|------------|----------|--------------------------------------------------------------------------------------------|
| `type`     | Yes      | A stable URI identifying the problem type. Dereferenceable documentation is preferred.     |
| `title`    | Yes      | A short, human-readable summary.                                                           |
| `status`   | Yes      | The HTTP status code, duplicated in the body.                                              |
| `detail`   | Yes      | A human-readable explanation specific to **this** occurrence.                              |
| `instance` | No       | A URI identifying the specific occurrence, typically the request path.                     |
| `code`     | Yes      | **Extension.** A stable, machine-readable `SCREAMING_SNAKE_CASE` code consumers branch on. |
| `errors`   | No       | **Extension.** Field-level details for validation failures. See below.                     |
| `traceId`  | Yes      | **Extension.** The W3C trace identifier of the request, for support and correlation.       |

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

Validation failures MUST populate `errors`, with one entry per invalid input — including, for a repeated query
parameter such as a bulk delete's `uuid` list, the zero-based index of the failing occurrence:

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
    {
      "pointer": "/emailAddress",
      "code": "INVALID_FORMAT",
      "detail": "Must be a valid email address."
    },
    {
      "pointer": "/organizations/0/uuid",
      "code": "REQUIRED",
      "detail": "Must not be null."
    },
    {
      "in": "query",
      "pointer": "pageSize",
      "code": "OUT_OF_RANGE",
      "detail": "Must be between 1 and 100."
    },
    {
      "in": "path",
      "pointer": "organizationUuid",
      "code": "INVALID_FORMAT",
      "detail": "Must be a UUID."
    },
    {
      "in": "query",
      "pointer": "uuid[2]",
      "code": "INVALID_FORMAT",
      "detail": "Must be a UUID."
    }
  ]
}
```

Each entry of `errors` has the following members:

| Member    | Required | Description                                                                                  |
|-----------|----------|----------------------------------------------------------------------------------------------|
| `in`      | No       | The part of the request the invalid input came from. Defaults to `body` when absent.         |
| `pointer` | Yes      | The locator of the invalid input within `in`. See below.                                     |
| `code`    | Yes      | A stable, machine-readable `SCREAMING_SNAKE_CASE` code for this specific failure.            |
| `detail`  | Yes      | A human-readable explanation of this specific failure.                                       |

* `in` MUST be one of `body` (the default; SHOULD be omitted, but an explicit `"in": "body"` is valid and consumers
  MUST treat both forms as equivalent), `query`, `path` or `header` — no other value, since cookies are not a
  documented API input.
* When `in` is `body` or absent, `pointer` MUST be a JSON Pointer (RFC 6901) into the request body. Otherwise,
  `pointer` MUST be the parameter or header name exactly as declared in OpenAPI, with no prefix or decoration. For a
  repeated query parameter, `pointer` MUST include a zero-based index suffix identifying the failing occurrence:
  `uuid[2]` for the third `uuid` value — see the worked example below.

#### Rules

* `code` is the contract. Consumers MUST be able to branch on `code` alone; `title` and `detail` are for humans and
  MAY be reworded at any time. Adding a new `code` is a non-breaking change; changing the meaning of an existing one
  is breaking.
* `type` URIs MUST be stable and MUST NOT be reused for a different problem.
* `detail` MUST NOT contain stack traces, SQL, internal hostnames, class names, or any other implementation detail.
* Error messages MUST be written in English. Localization is the consumer's responsibility, driven by `code` and, when
  needed, by additional structured extension members — never by parsing `detail`.
* A response MUST contain exactly one problem document. Multiple failures are reported through `errors`.
* `5xx` responses MUST NOT expose the underlying cause. They MUST still carry `traceId` so the failure can be
  correlated with server-side logs.
* Every documented operation MUST declare its error responses in its OpenAPI specification.

### 8. Content types

* Request and response bodies MUST use `application/json`, except where this ASR states otherwise.
* Error responses MUST use `application/problem+json`.
* `PATCH` requests MUST use `application/merge-patch+json`. A `PATCH` sent with `application/json` MUST be rejected
  with `415`, and MUST NOT be accepted as a merge patch.
* Binary payloads MUST use `application/octet-stream`, or `multipart/form-data` when metadata accompanies the file.
  Binary content MUST NOT be base64-encoded inside a JSON body except for small payloads where a separate request is
  impractical, and that choice MUST be documented.
* The `charset` parameter MUST be omitted. JSON is always UTF-8.
* Custom or vendor media types (`application/vnd.*`) MUST NOT be used. Hypermedia formats such as HAL or JSON:API
  MUST NOT be used.
* A request with an unsupported `Content-Type` MUST be rejected with `415`. A request whose `Accept` header cannot be
  satisfied SHOULD still be served `application/json` rather than failing, since it is the only representation offered.
* JSON response bodies MUST be objects at the top level, never arrays or scalars, so that the response can be extended
  without breaking consumers. See section 10.

### 9. Versioning and breaking changes

* Every API MUST carry a **major version as the first path segment**: `/v1/users`, `/v2/users`. This is required from
  the very first release, even when no second version is anticipated.
* The version MUST be a major version only. Minor and patch numbers MUST NOT appear in the path; those are conveyed by
  the service's own SemVer release (ASR-03).
* The version MUST NOT be placed anywhere else: not mid-path, not in a query parameter, not in a custom header, and
  not in a vendor media type.
* Internal BFF endpoints are exempt from this section, as their only consumer is deployed in lockstep with them.

#### Breaking versus non-breaking

APIs MUST evolve additively. The table below is authoritative for whether a change MUST be made in place (non-breaking)
or MUST NOT be made to a published version (breaking):

| Non-breaking — apply in place                                                 | Breaking — requires a new major version                                                                                            |
|-------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------|
| Adding a new endpoint, or a new optional query parameter with a safe default. | Removing or renaming an endpoint, a parameter, or a response property.                                                             |
| Adding a new property to a response body.                                     | Changing the type, format, or unit of an existing property.                                                                        |
| Adding a new optional property to a request body.                             | Adding a required request property, or a new constraint on an existing one.                                                        |
| Adding a new enum value **sent only by the client**.                          | Adding a new enum value **returned by the server**, unless the API documents up front that consumers must tolerate unknown values. |
| Relaxing a validation constraint.                                             | Changing the status code or the error `code` returned for an existing condition.                                                   |
| Adding a new error `code` under an existing status code.                      | Changing default values, sort order, or pagination behaviour.                                                                      |
|                                                                               | Changing a synchronous operation (`200`, `201`, `204`) to `202` plus an operation resource, or vice versa; see section 6.          |

Because generated SDKs deserialize responses strictly, adding a server-returned enum value is treated as breaking
unless tolerance for unknown values was declared up front. New APIs SHOULD declare that tolerance explicitly.

#### Introducing a new major version

A new major version is a last resort, taken only when the change cannot be made additively. When it is necessary:

1. The new version MUST be served in parallel with the previous one from a single deployment.
2. The previous version MUST be marked `deprecated: true` in the OpenAPI specification, with a description that points
   at its replacement.
3. Deprecated responses MUST include the `Deprecation` header (RFC 9745) and MUST include the `Sunset` header
   (RFC 8594) giving the date after which the version will be removed. Both SHOULD be accompanied by a
   `Link` header with `rel="deprecation"` pointing at the migration documentation.

   ```
   Deprecation: @1767225600
   Sunset: Fri, 01 Jan 2027 00:00:00 GMT
   Link: <https://docs.equisoft.io/api/migrations/v1-to-v2>; rel="deprecation"
   ```

4. The owning team MUST identify the remaining consumers of the deprecated version before removing it. A version MUST
   NOT be removed while it still has active consumers.
5. Removing a major version is a breaking change to the service and MUST be released as a major version under ASR-03.

Individual properties and operations MAY be deprecated within a version using `deprecated: true` in OpenAPI, provided
they continue to work until the next major version.

### 10. Collections, pagination, filtering and sorting

#### Envelope

Collection responses MUST be a JSON object containing an `items` array. A bare JSON array MUST NOT be returned, as it
cannot be extended with pagination or metadata without breaking consumers.

```json
{
  "items": [ { "uuid": "..." } ],
  "pagination": {
    "page": 1,
    "pageSize": 50,
    "totalItems": 327,
    "totalPages": 7
  }
}
```

#### Pagination

* Every collection endpoint that can grow without bound MUST be paginated, from its first release. Retrofitting
  pagination onto an unpaginated collection is a breaking change.

    | Aspect             | Page pagination (default)                                                                                         | Cursor pagination                                                                                                                                                        |
    |--------------------|-------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
    | When to use        | MUST be used unless there is a specific reason not to.                                                            | MAY be used instead for large or high-churn datasets, where counting the full result set is expensive or items shifting between pages during iteration is unacceptable.  |
    | Request parameters | `page` (1-based, defaulting to `1`) and `pageSize`.                                                               | `pageToken` (opaque, absent for the first page) and `pageSize`.                                                                                                          |
    | Response members   | `pagination.page`, `pagination.pageSize`, `pagination.totalItems`, `pagination.totalPages`.                       | `pagination.pageSize`, `pagination.nextPageToken` (`null` on the last page), `pagination.previousPageToken` (`null` on the first page or if forward-only).               |
    | Edge case          | Requesting a page beyond the last one MUST return an empty `items` array with the pagination metadata, not `404`. | `pageToken` MUST be opaque to the client; its contents MUST NOT be documented or parsed. `pagination.totalItems` MUST NOT be returned unless it can be computed cheaply. |

* An endpoint MUST offer exactly one of the two schemes; accepting both `page` and `pageToken` is not permitted.
* Switching an endpoint from page to cursor pagination is a breaking change; see section 9.
* `pageSize` MUST have a documented default and a documented maximum. Requests above the maximum MUST be clamped to it,
  not rejected.
* Results MUST have a deterministic total order, so that pagination is stable across requests.

#### Filtering and sorting

* Filters MUST be expressed as individual camelCase query parameters named after the property they filter:
  `?status=ACTIVE&createdAfter=2025-01-01T00:00:00Z`. A general-purpose query language MUST NOT be invented.
* Multi-valued filters MUST be repeated parameters (`?status=ACTIVE&status=PENDING`), not comma-separated values.
* Range filters SHOULD use `<property>After` / `<property>Before` for times and `min<Property>` / `max<Property>` for
  quantities.
* Free-text search MUST use a single `query` parameter.
* Sorting MUST use a single `sort` parameter containing a comma-separated list of properties, each optionally prefixed
  with `-` for descending order: `?sort=-createdAt,displayName`. A separate direction parameter MUST NOT be used.
* Sortable and filterable properties MUST be documented explicitly. An unknown value MUST be rejected with `400`.

### 11. Data formats

| Type                | Rule                                                                                                                      | Example                                      |
|---------------------|---------------------------------------------------------------------------------------------------------------------------|----------------------------------------------|
| Identifiers         | MUST be **UUIDs**, lowercase canonical strings. Database sequence identifiers MUST NOT be exposed.                        | `e4c82d21-13e9-4186-988a-13940cdf191c`       |
| Timestamps          | MUST be RFC 3339 / ISO 8601, **UTC**, explicit offset. Epoch numbers MUST NOT be used. Property names SHOULD end in `At`. | `"createdAt": "2025-01-15T14:32:07Z"`        |
| Calendar dates      | MUST be `YYYY-MM-DD` strings, and MUST NOT be represented as timestamps.                                                  | `"birthDate": "1990-06-01"`                  |
| Durations           | MUST be ISO 8601 durations, or an integer with the unit in the property name.                                             | `"P30D"` or `"timeoutSeconds": 30`           |
| Monetary amounts    | MUST pair a **decimal string** amount with an ISO 4217 currency code. Floating-point numbers MUST NOT be used.            | `{ "amount": "1234.56", "currency": "CAD" }` |
| Languages/countries | Languages MUST use ISO 639-1; countries MUST use ISO 3166-1 alpha-2.                                                      | `"fr"`, `"CA"`                               |

* An **absent** property and a `null` property MUST NOT mean different things in a response, though they do differ in
  `PATCH` requests per RFC 7396 (`null` clears, absent leaves unchanged). A nullable property whose value is `null`
  MUST therefore be serialized explicitly in responses, never omitted — otherwise a client building its next merge
  patch from the last response cannot tell an unset property from one that was simply not sent.
* Empty collections MUST be serialized as `[]`, never as `null` or omitted.
* Every string property MUST have a documented maximum length. Unbounded strings MUST NOT be accepted.

### 12. Authentication and security

* APIs MUST be served over TLS only.
* Authentication MUST use OAuth 2.0 bearer tokens: `Authorization: Bearer <jwt>`. Custom authentication headers or
  API keys in query parameters MUST NOT be used. A `401` MUST include `WWW-Authenticate`; see section 5.
* Credentials and tokens MUST NOT appear in URLs — neither path segments nor query parameters — because URLs are
  recorded in access logs, proxies, and browser history. Personal data MUST NOT appear in path segments for the same
  reason, but MAY appear in a query parameter where filtering or searching on it is the documented purpose of the
  operation (`?emailAddress=`, `?query=`); such parameters MUST be identified in the OpenAPI specification and MUST
  be redacted by the service's access logging.
* Authorization MUST be enforced per operation on the server. Hiding an operation from a client is not authorization.
* CORS MUST be restricted to the organization's own origins, unless there is a specific reason to allow third-party
  origins.
* Responses MUST NOT echo back more data than the caller is entitled to see, including through error messages.

### 13. Observability and reliability headers

* Services MUST propagate W3C Trace Context (`traceparent`, `tracestate`). When a request arrives without one, the
  service MUST create it. The trace identifier MUST be surfaced in error responses as `traceId`; see section 7.
* Custom correlation headers such as `X-Request-Id` MUST NOT be introduced.
* `429` responses MUST include `Retry-After`. `503` responses SHOULD include it.
* Non-idempotent operations with significant or irreversible side effects (payments, provisioning, outbound
  communications) MUST accept an `Idempotency-Key` request header. `Idempotency-Key` values are client-generated and
  therefore not globally unique, so a stored record MUST NOT be keyed by the header value alone.
* A record MUST also be scoped by the authenticated principal (and tenant, where distinct), the HTTP method, and the
  target URI. A key presented by a different principal, method or URI is an unrelated key — never a replay, never a
  `409`. This scoping MUST be implemented once, in the shared middleware of Appendix A, so no individual service
  reduces it to indexing by the raw key.
* Two requests are the **same request**, for replay purposes, when method, target URI (query string included) and
  request body all match. The body MUST be compared by a stored digest, not by re-executing the operation.
* The service MUST return the original response, unchanged, for a repeated key with the same request, and MUST
  return `409` — a problem document under section 7, with a dedicated `code` — when the same key, in the same scope,
  is reused with a different request. A repeat that arrives while the original request is still being processed
  MUST NOT trigger the side effect a second time: return `409` while the outcome is indeterminate, then the original
  response once it is known.
* Keys SHOULD be retained for at least 24 hours, and MUST NOT be retained longer than the associated operation
  resource remains retrievable; see section 6.
* When the operation is asynchronous, "the original response" is the original `202` referencing the same operation
  resource. A repeated `Idempotency-Key` MUST NOT start a second operation; it MUST return `202` with the same
  `Location` for as long as that operation resource remains retrievable, regardless of whether it has since reached
  a terminal state; see section 6.
* Resources subject to concurrent modification SHOULD support optimistic concurrency: return an `ETag` on `GET` (see
  section 14), accept `If-Match` on `PUT`, `PATCH` and `DELETE`, and return `412` when the precondition fails. When
  `ETag` is not supported, a version property in the body MAY be used instead, and MUST also return `412` on mismatch,
  so that consumers branch on one status code regardless of how concurrency is implemented.
* A resource that accepts `PATCH` MUST support optimistic concurrency: a merge patch applied to state the client
  never saw is the characteristic way an update is silently lost.
* An operation MAY require `If-Match` rather than merely honouring it, when a blind overwrite would be unsafe. It MUST
  then reject a request that does not carry one with `428`, and MUST document that requirement.
* A bulk operation MUST NOT accept `If-Match`, since a single `ETag` cannot express a precondition over more than one
  resource; see section 3. A caller that needs optimistic concurrency MUST use the single-resource operation instead.

### 14. Caching and conditional requests

* Every `GET` response MUST set an explicit `Cache-Control`. Leaving it to the framework's default is how the same
  resource ends up cached differently depending on where it is deployed.
* Responses containing personal or otherwise sensitive data MUST set `Cache-Control: no-store`.
* Any response whose content depends on the caller MUST set `Cache-Control: private`, and MUST set
  `Vary: Authorization` when it is cacheable at all, so that a shared cache cannot serve one caller's representation
  to another.
* Responses to `POST`, `PUT`, `PATCH` and `DELETE` MUST set `Cache-Control: no-store`. The `ETag` that section 6
  requires on a successful update is not in tension with this: it is a validator for the next `If-Match`, not a
  licence to cache the response.
* A non-terminal operation resource (`PENDING`, `RUNNING`; see section 6) MUST set `Cache-Control: no-store`, since a
  cached poll response is indistinguishable from a stale one. A terminal operation resource (`SUCCEEDED`, `FAILED`)
  follows the ordinary `GET` rules above.
* A `GET` on a single resource SHOULD return a strong `ETag`. Collection responses MAY.
* `ETag` values MUST be opaque. Consumers MUST NOT parse them. An `ETag` MUST change whenever the representation
  changes, and is scoped to the full request URL, query parameters included: two pages of the same collection are two
  representations with two `ETag` values.
* A `GET` or `HEAD` carrying an `If-None-Match` that matches the current `ETag` MUST return `304`, with no body,
  carrying the same `ETag` and `Cache-Control` the `200` would have carried. A `304` is not an error and MUST NOT
  carry problem details.
* `If-None-Match` comparison MUST be **weak** (RFC 9110 §13.1.2): the `W/` prefix, if present, is ignored when
  comparing, unlike the strong comparison `If-Match` requires for concurrency; see section 13. `If-None-Match: *`
  MUST match any current representation, so a `GET` or `HEAD` carrying it against an existing resource MUST also
  return `304`.
* `Last-Modified` and `If-Modified-Since` MAY be supported in addition. Where both `ETag`/`If-None-Match` and
  `Last-Modified`/`If-Modified-Since` are present on the same request, `If-None-Match` MUST take precedence and
  `If-Modified-Since` MUST be ignored.
* The same `ETag` drives optimistic concurrency through `If-Match`; see section 13. See Consequences for why that,
  not `304` revalidation, is the primary reason to emit an `ETag` in our APIs.

### 15. The specification is the contract

* Every API MUST publish an **OpenAPI 3.x** specification, and that specification MUST be generated from, or verified
  against, the implementation. A specification that can drift from the code is not a contract.
* Every operation MUST document a `summary`, a description, all success responses, and all error responses the
  application returns — with their problem-detail schemas and `code` values. Codes emitted by the framework are
  declared once for the specification as a whole; see section 5.
* Every property MUST document its type, format, constraints, and whether it is required.
* The request schema of every `POST`, `PUT` and `PATCH` operation MUST NOT allow a consumer to set a server-managed
  property: each such property MUST either be absent from that schema, or present and marked `readOnly: true`.
  Reusing the resource schema unchanged as the request body for any of these methods tells consumers they may set
  `uuid` and `createdAt`.
* Every operation accepting `PATCH` MUST additionally declare a **dedicated patch schema**, separate from the resource
  schema and bound to the `application/merge-patch+json` content type. In that schema every property is optional and
  clearable properties are nullable, on top of the server-managed-property rule above. Reusing the resource schema
  for a patch body also misdeclares required properties.
* Dedicated write schemas SHOULD be named after the resource and the operation, so their scope is unambiguous from
  the schema name alone: `<Resource>Patch` for the merge patch body, and `<Resource>CreateRequest` /
  `<Resource>ReplaceRequest` when a `POST` or `PUT` operation declares a dedicated request schema rather than relying
  on `readOnly` markings on the resource schema itself.
* Generated SDKs MUST preserve the three states a merge-patch property can be in — absent, `null`, and a value (see
  Consequences). The representation is language-specific and is fixed by the shared generator configuration, not per
  service; see Appendix A.
* The specification MUST be linted in CI with **Spectral** using the shared organization configuration, and violations
  MUST fail the build, in line with the zero-tolerance policy of ASR-08.
* Client SDKs MUST be generated from the specification using the shared organization tooling. Hand-written clients for
  internal APIs MUST NOT be maintained.
* Changes to the specification MUST be reviewed as code, with explicit attention to the breaking-change rules in
  section 9.

## Consequences

* **Uniform consumer experience.** Every generated SDK exposes the same casing, the same collection envelope, the same
  pagination, and the same error type. Integrating with a second service costs materially less than integrating with
  the first.
* **Objective API review.** Design discussions become a matter of checking against this document rather than
  negotiating preferences per pull request.
* **Two error formats will coexist.** Until existing services migrate, consumers must handle both the legacy
  `{ code, message }` shape and RFC 9457 problem details. Appendix B keeps `code` intact specifically to make that
  transition mechanical.
* **`PATCH` becomes a first-class method, and it is not free.** Merge patch requires a distinct schema per patchable
  resource, a three-state binding — absent, `null`, value — that no generator produces by default (the plain
  optional-nullable field generators emit collapses "leave unchanged" and "clear" into one value), and optimistic
  concurrency on every resource that accepts it. That is more work than adding another `PUT`. It is accepted because
  the alternative — the `PUT` everywhere pattern the Context section describes — makes every partial update a
  full-representation write, and a client that writes back a representation it did not fully understand clears data
  without anyone noticing. Keeping server-managed properties out of every `POST`, `PUT` and `PATCH` request schema —
  by omission or `readOnly: true` — is likewise an ongoing discipline, applied each time a resource gains a new
  server-managed property, not a one-time cost paid only when `PATCH` is introduced.
* **Bulk delete is deliberately bounded.** There is no unbounded criteria delete without explicit confirmation, every
  bulk operation carries a hard maximum batch size, and `207 Multi-Status` is not available for reporting partial
  outcomes. A caller that needs per-resource optimistic concurrency, or needs to affect more resources than the
  documented maximum, MUST fall back to the single-resource operation, one identifier at a time.
* **Asynchronous operations require a durable operation store, not just a `202`.** Generalizing `202` beyond
  creating `POST` requires every service to run the shared operation resource: a retrievable, retention-bound record
  of status that other endpoints do not otherwise need. Skipping that store to save the `202` response is how
  services would end up polling in-memory state that a second instance cannot see.
* **Versioning has an ongoing cost.** Serving two major versions in parallel means maintaining two mappings and two
  sets of tests for the duration of the deprecation window. This is the intended trade-off: it makes breaking changes
  visible and deliberate rather than silent.
* **Additive evolution constrains design.** Getting a resource model wrong is now expensive to correct, which raises
  the value of upfront design review — and means teams will occasionally carry an imperfect model longer than they
  would like.
* **Exhausting the resource-modelling ladder before reaching for a verb is real design work, and it is not free.**
  Checking an action against a state sub-resource, an addressable outcome, and a relationship change — in that
  order — takes longer than writing a `POST .../activate` and moving on. It is accepted because a verb path does
  not compose: it cannot be nested, filtered, paginated, or cached the way a resource can. It also resists uniform
  authorization, which attaches naturally to a resource and a method but must be special-cased per verb. Each verb
  accepted without exhausting the ladder is a small step of the API toward RPC-over-HTTP, where the method and
  status code stop carrying meaning and the path becomes the entire contract.
* **New CI friction, and lasting drift.** Spectral linting and specification review will reject changes that
  previously merged silently. Because remediation of existing services is opportunistic, the organization will also
  operate with two generations of API conventions for an extended period — accepted in exchange for not halting
  feature work.
* **Shared tooling is required.** A shared problem-details model, exception mapper, pagination envelope, and
  `Idempotency-Key` implementation must be published in the organization's shared libraries so that each service does
  not reimplement them — and diverge again. Appendix A lists what this ASR depends on.
* **`ETag` earns its keep mainly through concurrency, not caching.** Most responses carrying personal data are
  `no-store` and so cannot be revalidated by the client; `If-Match` optimistic concurrency, not `304` revalidation, is
  the primary reason our APIs emit an `ETag` at all.

## Alternatives Considered

* **kebab-case paths.** kebab-case is the more common convention in the wider industry and reads slightly better in
  URLs. It was rejected because camelCase is already the dominant convention in our services and matches our JSON
  bodies, query parameters, and both target SDK languages. Mass-renaming existing paths would be a breaking change
  with no functional benefit.
* **Keeping the existing `{ code, message }` error schema.** It is already shared by most services and is simple. It
  was rejected because it carries no field-level validation detail, no correlation identifier, and no stable problem
  taxonomy — every service ended up inventing its own enum. RFC 9457 provides all of that as a standard that tooling
  already understands, and the `code` extension preserves what was useful about the existing shape.
* **JSON Patch (RFC 6902) instead of JSON Merge Patch.** JSON Patch is more capable — it can add, remove and reorder
  individual array elements, its `test` operation gives a precondition at property granularity, and an operation
  sequence expresses intent merge patch cannot. It was rejected because a patch body is an untyped list of operation
  objects: it cannot be generated as a typed SDK method, cannot be validated against the resource schema (so the
  `readOnly` mechanism section 3 relies on has nothing to attach to), and turns per-field authorization into an
  interpretation of `path` strings. `move`, `copy` and index-based operations also invite clients to encode
  assumptions about server-side ordering. Merge patch's weakness is the point: shaped like the resource, it validates,
  generates and authorizes like the resource. Where it is genuinely insufficient — element-level array mutation — the
  answer is an addressable sub-resource, which produces a better resource model than an operation list would have.
* **Allowing both formats, negotiated by content type.** Rejected for the same reason the ASR admits only one
  collection envelope and one pagination scheme by default: a second permitted format is a second thing every consumer
  must understand and every service must implement, and in practice it would become the escape hatch that avoids the
  resource-modelling work above.
* **Header or media-type versioning** (`Accept-Version`, `application/vnd.equisoft.v2+json`). More architecturally
  pure, since a resource keeps one URL. Rejected because it is invisible in logs and browsers, awkward to test with
  ordinary tooling, poorly supported by SDK generators, and easy for consumers to get wrong by omission.
* **No versioning, additive evolution only.** This is effectively today's situation. Rejected because it provides no
  escape hatch: when a breaking change is genuinely unavoidable, teams end up creating parallel endpoints with ad hoc
  names, which is versioning without the discipline.
* **Hypermedia (HAL, JSON:API, HATEOAS).** Rejected as disproportionate. Our consumers are generated SDKs compiled
  against a known specification, not generic clients discovering the API at runtime; the added payload complexity buys
  nothing.

## Related Decisions

* [ASR-01: Repository management](ASR-01_repository-management.md) — code review requirements that apply to
  specification changes.
* [ASR-03: Project versioning](ASR-03_project-versioning.md) — SemVer, and the relationship between a service release
  and an API major version.
* [ASR-05: Languages Selection and Libraries](ASR-05_languages-selection-and-libraries.md) — the stack these APIs are
  implemented in.
* [ASR-08: Code Quality Analysis](ASR-08_code-quality-analysis.md) — the zero-tolerance CI policy that specification
  linting falls under.

## Appendix A: Shared tooling and models required for this ASR

This ASR describes a single API surface shared by every service, but only takes effect once its building blocks
exist as shared, versioned artifacts — reimplementing them per service is how the divergence in the Context section
happened. Each item below MUST be published in the organization's shared libraries, for each stack of ASR-05, before
services are expected to comply with the corresponding section.

| Artifact                  | Section | Contents                                                                                                                                                                                                                                       |
|---------------------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Merge patch support       | 3       | Three-state binding, patch application, and server-managed property enforcement across `POST`, `PUT` and `PATCH`                                                                                                                               |
| Bulk operation support    | 3       | Repeated-identifier binding, batch-size enforcement, and atomic/partial delete execution                                                                                                                                                       |
| Operation resource        | 6       | The shared `/v1/operations/{uuid}` model, its status machine, and retention                                                                                                                                                                    |
| Problem-details model     | 7       | RFC 9457 document with the `code`, `errors` and `traceId` extensions                                                                                                                                                                           |
| Exception mapper          | 7       | Framework mapping of application and validation errors to problem details                                                                                                                                                                      |
| Problem type URI registry | 7       | The `https://errors.ca.equisoft.io` namespace and the common problem types                                                                                                                                                                     |
| Collection envelope       | 10      | `items` / `pagination` models and the binding of the paging and `sort` inputs                                                                                                                                                                  |
| Idempotency-key support   | 13      | Middleware storing and replaying responses per `Idempotency-Key`, scoped by principal, method and target URI, retained no longer than the referenced operation resource; includes the operation resource reference for asynchronous operations |
| Trace context propagation | 13      | W3C `traceparent` / `tracestate` propagation, exposed to the exception mapper                                                                                                                                                                  |
| Shared OpenAPI components | 15      | Reusable schemas and parameters for the models above                                                                                                                                                                                           |
| Spectral ruleset          | 15      | The mechanically checkable rules of this ASR, as a published ruleset                                                                                                                                                                           |
| SDK generation tooling    | 15      | Shared generator configuration for the TypeScript, PHP and Kotlin clients                                                                                                                                                                      |

Rules:

* These artifacts MUST be versioned and released per ASR-03, and their behaviour MUST match this document; when the
  two disagree, this ASR is authoritative and the tooling MUST be corrected. A change to this ASR affecting any
  artifact above MUST be accompanied by the corresponding change to that artifact, or by an issue tracking it.
* Services MUST consume these artifacts rather than defining local equivalents. A local implementation is acceptable
  only while the shared one does not yet exist for that stack, and it MUST then be replaced.
* Until the Spectral ruleset is published, section 15's linting requirement is aspirational; conformance is verified
  in code review.
* The merge patch artifact carries one obligation the others do not: the absent / `null` / value distinction of
  RFC 7396 MUST survive deserialization, application and SDK generation intact (see the Consequences bullet on
  `PATCH`). How it is represented — `undefined` versus `null` in TypeScript, a wrapper type in Kotlin and PHP — is
  decided once, by this artifact and the shared generator configuration, and MUST NOT be decided per service.

## Appendix B: Migrating existing error responses to RFC 9457

Existing services return `{ "code": "NOT_FOUND", "message": "..." }` with `application/json`. The migration is
deliberately designed so that the machine-readable part of the contract — `code` — is preserved.

1. **Map the existing enum.** Every value of the service's current `ErrorCodes` enum MUST be mapped to a `type` URI
   and a `title`; the enum value itself becomes the `code` extension member, unchanged, so consumers branching on
   `code` continue to work once they read it from the new payload.
2. **Introduce the problem-details model.** Adopt the shared model from Appendix A rather than defining a local one.
   Populate `type`, `title`, `status`, `detail`, `instance`, `code` and `traceId`; the legacy `message` becomes
   `detail`.
3. **Add validation details.** Map framework validation failures to the `errors` array, with one entry per invalid
   input, per section 7. This is new information and breaks nothing.
4. **Switch the content type at the new major version.** Changing the error content type and body shape is a
   breaking change; it MUST be introduced alongside the `/v{major}` path prefix, following the major-version
   procedure of section 9. The previous version keeps returning the legacy shape until sunset.
5. **Regenerate SDKs before migrating consumers.** Publish the regenerated SDK for the new version first, then
   migrate consumers one at a time, then sunset the old version per section 9.
6. **Unversioned services adopting `/v1` for the first time** MUST treat their existing surface as `v1` and serve it
   unchanged, introducing problem details only in `v2` — unless every consumer can be migrated at once, in which
   case the change MAY be made directly in `v1` with the agreement of all consumers, recorded in the pull request.

Migration is opportunistic per section 1, but it MUST NOT be deferred past the service's next major API version.
