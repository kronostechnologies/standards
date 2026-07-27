# ASR-15: REST API Design

Status: Proposed

## Context

Our systems are built as a set of HTTP services that communicate with each other and with our frontends. Client SDKs
(TypeScript, PHP, Kotlin) are generated from each service's OpenAPI specification, which means that every inconsistency
in an API surface is propagated directly into the code of every consumer.

Until now, no written REST API design standard existed. Each service defined its own conventions, and a review of our
existing services shows meaningful divergence:

* Error payloads are mostly `{ "code": "NOT_FOUND", "message": "..." }`, but at least one service uses a different
  shape, and none of them expose field-level validation details.
* Collections are returned in three incompatible shapes: a named array with cursor tokens, a paginated envelope with
  page counters, and bare JSON arrays.
* Pagination uses cursors in one service, `page`/`size` in another, and `offset`/`limit` in a third.
* Almost no service is versioned. One uses a `/v1` path prefix, another embeds a version mid-path. Breaking changes
  have been handled ad hoc by creating a new endpoint and deprecating the old one in its description text.
* Path segments are usually camelCase, but not always. Query parameter naming is inconsistent.
* `PUT` is used for virtually every update; `PATCH` appears once across the entire organization.
* There is no convention for deprecation signalling, concurrency control, idempotency, or retry hints.

The cost of this divergence is paid by consumers: every integration requires learning a new set of conventions, and
generated SDKs cannot offer a uniform developer experience. A single, explicit standard removes that cost and makes API
review objective rather than a matter of taste.

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
* Paths MUST NOT contain verbs for CRUD operations. The HTTP method carries that meaning. `GET /v1/getUsers` is
  incorrect; `GET /v1/users` is correct.
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

Some operations are genuinely not the creation, replacement, or removal of a resource — activating an account,
triggering a run, sending a message. These MUST be modelled as a `POST` to a **verb sub-resource** of the resource they
act upon:

```
POST /v1/users/{uuid}/activate
POST /v1/gateways/{uuid}/run
```

* The verb MUST be the last path segment and MUST be a single imperative verb.
* Verb sub-resources MUST NOT be used as a substitute for a proper resource when one can be modelled. Before adding
  one, consider whether the action is really a state change that could be expressed as a sub-resource
  (`PUT /v1/users/{uuid}/status`).
* Top-level verb paths (`POST /v1/login`) SHOULD be avoided, but are acceptable for operations that do not act on an
  addressable resource.

### 3. HTTP methods

| Method   | Purpose                                          | Safe | Idempotent | Request body | Typical success (preferred order) |
|----------|--------------------------------------------------|------|------------|--------------|-----------------------------------|
| `GET`    | Retrieve a resource or a collection              | Yes  | Yes        | MUST NOT     | `200`                             |
| `POST`   | Create a resource, or perform a non-CRUD action  | No   | No         | Usually      | `201`, `202`, `200`, `204`        |
| `PUT`    | Full replacement or upsert at a known identifier | No   | Yes        | MUST         | `200`, `201`, `204`               |
| `PATCH`  | Partial update                                   | No   | Yes        | MUST         | `200`, `204`                      |
| `DELETE` | Remove a resource                                | No   | Yes        | MUST NOT     | `204`                             |

* `GET` MUST NOT have side effects observable by the consumer. It MUST NOT carry a request body, because intermediaries
  are not required to forward it.
* `POST` is the only non-idempotent method. Operations that clients may safely retry SHOULD be modelled with `PUT`
  instead. When a `POST` has significant side effects, it MUST support idempotency keys; see section 13.
* `PUT` MUST replace the resource in full. Properties absent from the request body MUST be reset to their default or
  cleared — a `PUT` is not a partial update. `PUT` MAY create the resource when the client controls the identifier.
* `PATCH` MUST apply a partial update to an existing resource, using JSON Merge Patch; see below.
* `DELETE` MUST be idempotent. Deleting an already-deleted resource MUST return `204`, not `404`, when the API can
  determine the resource previously existed.
* `HEAD` and `OPTIONS` are handled by the framework and MUST NOT be implemented manually.

#### Choosing between `PUT` and `PATCH`

Both methods update an existing resource, and the choice between them is determined by what the **client** holds, not
by what is convenient to implement:

* Use `PUT` when the client holds, and is responsible for, the **entire** representation — a settings document it just
  rendered in full, or a resource whose every writable property is on the form being submitted.
* Use `PATCH` when the client holds a **subset**: it is changing two properties out of twenty, or the resource carries
  writable properties this particular client neither knows nor owns.
* When in doubt, prefer `PATCH`. A `PUT` that a client builds from an incomplete view of the resource silently clears
  every property it did not know about, and that failure is invisible until the data is gone.

A single-property state change MAY instead be modelled as a `PUT` on a sub-resource (`PUT /v1/users/{uuid}/status`), as
described in section 2. That is preferable to a `PATCH` when the property is a genuine state machine with its own
transition rules and its own authorization. It MUST NOT be used to give every writable property its own sub-resource:
patching several properties of one resource is a `PATCH`, not a series of `PUT` calls that cannot be applied atomically.

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
* A patch containing a property that is unknown, read-only, or immutable after creation MUST be rejected with `400`.
  Silently discarding it turns a typo, or an attempt to change `uuid`, into an apparently successful update. These
  properties MUST be marked `readOnly: true` in the specification; see section 15.
* An empty patch document (`{}`) is a valid no-op. It MUST succeed and MUST NOT be rejected as an empty body.
* `PATCH` MUST NOT create a resource. When the target does not exist, it MUST return `404`. Upsert belongs to `PUT`.
* `PATCH` MUST NOT be sent to a collection URL. There is no bulk patch: `PATCH /v1/users` MUST NOT be routed.
* The patch MUST be applied atomically. A patch that fails validation MUST leave the resource entirely unchanged; a
  partially applied patch is never an acceptable outcome.
* Validation failures MUST be reported per section 7, with `pointer` a JSON Pointer into the **patch document**.
* `PATCH` is idempotent: applying the same merge patch twice MUST produce the same resource state as applying it once.

### 4. Casing

camelCase is the default casing across the API surface. The table below is authoritative: where it names a different
convention, that convention MUST be used instead.

| Element              | Convention             | Example                                   |
|----------------------|------------------------|-------------------------------------------|
| Path segments        | camelCase              | `/v1/oauthClients`, `/v1/serviceProfiles` |
| Path parameters      | camelCase              | `/v1/files/{uuid}`                        |
| Query parameters     | camelCase              | `?includeDeleted=true&pageSize=50`        |
| JSON body properties | camelCase              | `"ownerOrganizationUuid"`, `"createdAt"`  |
| Enum values          | `SCREAMING_SNAKE_CASE` | `"status": "PENDING_ACTIVATION"`          |
| HTTP header names    | hyphen-separated       | `Content-Type`, `Retry-After`             |

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

#### Codes emitted around the application

These are produced by the framework, by shared middleware, or by the ingress or a proxy, before or around the
application code. They are permitted, and a service is not required to implement them itself.

| Code  | Name                   | Emitted when                                                                           |
|-------|------------------------|----------------------------------------------------------------------------------------|
| `304` | Not Modified           | A conditional `GET` whose `If-None-Match` matched the current `ETag`; see section 14.  |
| `405` | Method Not Allowed     | The path is routed but the method is not.                                              |
| `406` | Not Acceptable         | The `Accept` header cannot be satisfied; see section 8.                                |
| `413` | Content Too Large      | The request body exceeds the configured limit.                                         |
| `414` | URI Too Long           | The request line exceeds the configured limit.                                         |
| `415` | Unsupported Media Type | The request `Content-Type` is not supported; see section 8.                            |
| `502` | Bad Gateway            | The ingress cannot reach the service, or received an invalid response from it.         |
| `504` | Gateway Timeout        | The ingress did not receive a response from the service in time.                       |

* These codes MUST NOT be implemented by hand in application code, and MUST NOT be caught and remapped to a code from
  the first table. A `405` rewritten as `404` hides a routing defect from the caller and from monitoring.
* `304` is the one code in this group the application participates in: it is rendered by the shared conditional-request
  middleware from the `ETag` the application supplies. The service supplies the validator; it MUST NOT build the `304`
  response itself. See section 14 and Appendix A.
* The error codes in this group SHOULD be rendered as problem details wherever the framework allows a custom error
  representation. Responses generated outside the service — `502`, `504`, and `413` when the limit is enforced by the
  ingress — cannot be, so consumers MUST tolerate a `4xx` or `5xx` whose body is not a problem document.
* They need not be declared on every operation. They SHOULD be declared once, as common responses of the
  specification.
* Content negotiation SHOULD be configured to serve `application/json` rather than emit `406`, since it is the only
  representation offered; see section 8.

Any other status code MUST NOT be used without an explicit, documented exception.

#### Disambiguation

* **`401` vs `403`** — `401` means "we do not know who you are"; `403` means "we know who you are, and you may not do
  this". A `401` MUST include a `WWW-Authenticate` header.
* **`400` vs `422`** — `400` is a structural or syntactic problem the client can detect from the schema alone (missing
  field, wrong type). `422` is a semantic problem that requires business context (the end date precedes the start
  date, the account is not eligible). When in doubt, use `400`.
* **`403` vs `404`** — when disclosing the existence of a resource is itself a leak, return `404`. Otherwise `403`.
* **`409` vs `422`** — `409` is about the current state of the server (something already exists, a transition is not
  possible from the current state); `422` is about the content of the request.
* **`409` vs `412`** — a failed concurrency precondition is always `412`, never `409`; see section 13.
* **`412` vs `428`** — `412` means "you sent a precondition and it did not hold"; `428` means "you sent no
  precondition and this operation requires one".
* **`404` vs `405`** — a `405` means routing found the path and rejected only the method. It MUST NOT be masked as a
  `404`.
* **`500`** MUST NOT be returned for any condition the client caused. A `500` is always a defect.

### 6. Response headers and bodies for creation and update

* A `POST` that creates a resource MUST return `201 Created` with a **relative** `Location` header pointing at the
  canonical URL of the new resource:

  ```
  HTTP/1.1 201 Created
  Location: /v1/organizations/e4c82d21-13e9-4186-988a-13940cdf191c
  Content-Type: application/json
  ```

* A `201` response SHOULD also return the created representation in the body, so that server-assigned properties
  (identifier, timestamps, computed defaults) are available without a follow-up `GET`. When the body is deliberately
  omitted, the `Location` header remains mandatory.
* A `POST` that starts asynchronous work MUST return `202 Accepted` with a `Location` header pointing at a status
  resource the client can poll. The body SHOULD describe the current state of that operation.
* A `POST` to a verb sub-resource that does not create anything MUST return `200` with a body or `204` without one, or
  `202` when it starts asynchronous work, per the rule above. It MUST NOT return `201`.
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

### 7. Error handling

All error responses (`4xx` and `5xx`) returned by the application MUST be **Problem Details for HTTP APIs** as defined
by **RFC 9457**, serialized with the `application/problem+json` content type. Responses generated by the framework or
the ingress are covered by section 5.

#### Structure

| Member     | Required | Description                                                                                |
|------------|----------|--------------------------------------------------------------------------------------------|
| `type`     | Yes      | A stable URI identifying the problem type. Dereferenceable documentation is preferred.     |
| `title`    | Yes      | A short, human-readable summary. MUST NOT change between occurrences of the same `type`.   |
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

Validation failures MUST populate `errors`, with one entry per invalid input:

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

* `in` MUST be one of `body`, `query`, `path` or `header`. No other value is allowed; in particular, cookies are not a
  documented API input.
* `in` SHOULD be omitted when the invalid input is in the request body, since `body` is the default. An explicit
  `"in": "body"` is valid and consumers MUST treat the two forms as equivalent.
* When `in` is `body` or absent, `pointer` MUST be a JSON Pointer (RFC 6901) into the request body.
* Otherwise, `pointer` MUST be the parameter or header name exactly as it appears in the OpenAPI specification, with no
  prefix, sigil or other decoration.

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
* `PATCH` requests MUST use `application/merge-patch+json`. A `PATCH` sent with `application/json` MUST be rejected with
  `415`, and MUST NOT be accepted as a merge patch: silently accepting it is how a service acquires its own private
  patch semantics, which is precisely what section 3 exists to prevent.
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

APIs MUST evolve additively. The following changes are **non-breaking** and MUST be made in place, without a new major
version:

* Adding a new endpoint, or a new optional query parameter with a safe default.
* Adding a new property to a response body.
* Adding a new optional property to a request body.
* Adding a new value to an enum **that is only ever sent by the client**.
* Relaxing a validation constraint.
* Adding a new error `code` under an existing status code.

The following changes are **breaking** and MUST NOT be made to a published version:

* Removing or renaming an endpoint, a parameter, or a response property.
* Changing the type, format, or unit of an existing property.
* Adding a required request property, or a new constraint on an existing one.
* Adding a new value to an enum **returned by the server**, unless the API documents from the outset that consumers
  must tolerate unknown values.
* Changing the status code or the error `code` returned for an existing condition.
* Changing default values, sort order, or pagination behaviour.

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
* **Page pagination is the default** and MUST be used unless there is a specific reason not to:
  * Request: `page` (1-based, defaulting to `1`) and `pageSize`.
  * Response: `pagination.page`, `pagination.pageSize`, `pagination.totalItems` and `pagination.totalPages`.
  * Requesting a page beyond the last one MUST return an empty `items` array with the pagination metadata, not `404`.
* **Cursor pagination** MAY be used instead for large or high-churn datasets, where counting the full result set is
  expensive or where items shifting between pages during iteration is unacceptable:
  * Request: `pageToken` (opaque, absent for the first page) and `pageSize`.
  * Response: `pagination.pageSize`, `pagination.nextPageToken` (`null` on the last page) and
    `pagination.previousPageToken` (`null` on the first page or if the cursor is forward-only).
  * `pageToken` MUST be opaque to the client. Its contents MUST NOT be documented or parsed.
  * `pagination.totalItems` MUST NOT be returned alongside cursor pagination unless it can be computed cheaply.
* An endpoint MUST offer exactly one of the two schemes. Accepting both `page` and `pageToken` on the same operation is
  not permitted, as their semantics conflict.
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

* Public identifiers MUST be **UUIDs**, serialized as lowercase canonical strings. Database sequence identifiers MUST
  NOT be exposed, as they leak volume information and are trivially enumerable.
* Timestamps MUST be RFC 3339 / ISO 8601 strings **in UTC** with an explicit offset: `2025-01-15T14:32:07Z`. Epoch
  numbers MUST NOT be used. Property names SHOULD end in `At` (`createdAt`, `deletedAt`).
* Calendar dates without a time MUST be `YYYY-MM-DD` strings, and MUST NOT be represented as timestamps.
* Durations MUST be ISO 8601 durations (`P30D`), or an integer with the unit in the property name
  (`timeoutSeconds`).
* Monetary amounts MUST be represented as an object pairing a **decimal string** amount with an ISO 4217 currency
  code: `{ "amount": "1234.56", "currency": "CAD" }`. Floating-point numbers MUST NOT be used for money.
* Languages MUST use ISO 639-1 codes; countries MUST use ISO 3166-1 alpha-2 codes.
* An **absent** property and a `null` property MUST NOT mean different things in a response. In `PATCH` requests they
  do differ, per RFC 7396: `null` clears, absent leaves unchanged.
* Because of that asymmetry, a nullable property whose value is null MUST be serialized explicitly as `null` in
  responses rather than omitted. A client builds a merge patch from the representation it last read; if the server
  omits nulls, the client cannot tell an unset property from one it simply was not sent, and cannot tell which
  properties are clearable at all.
* Empty collections MUST be serialized as `[]`, never as `null` or omitted.
* Every string property MUST have a documented maximum length. Unbounded strings MUST NOT be accepted.

### 12. Authentication and security

* APIs MUST be served over TLS only.
* Authentication MUST use OAuth 2.0 bearer tokens: `Authorization: Bearer <jwt>`. Custom authentication headers or
  API keys in query parameters MUST NOT be used.
* A `401` response MUST include a `WWW-Authenticate` header.
* Credentials and tokens MUST NOT appear in URLs — neither in path segments nor in query parameters — because URLs
  are recorded in access logs, proxies, and browser history. They belong in headers or the request body.
* Personal data MUST NOT appear in path segments, for the same reason. It MAY appear in a query parameter only where
  filtering or searching on it is the documented purpose of the operation (`?emailAddress=`, `?query=`); such
  parameters MUST be identified in the OpenAPI specification and MUST be redacted by the service's access logging.
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
  communications) MUST accept an `Idempotency-Key` request header. The service MUST return the original response for a
  repeated key with an identical request, and MUST return `409` when the same key is reused with a different request.
  Keys SHOULD be retained for at least 24 hours.
* Resources subject to concurrent modification SHOULD support optimistic concurrency: return an `ETag` on `GET` (see
  section 14), accept `If-Match` on `PUT`, `PATCH` and `DELETE`, and return `412` when the precondition fails. When
  `ETag` is not supported, a version property in the body MAY be used instead, and MUST also return `412` on mismatch,
  so that consumers branch on one status code regardless of how concurrency is implemented.
* A resource that accepts `PATCH` MUST support optimistic concurrency. `PATCH` is the read-modify-write method: the
  client fetches a representation, decides on a change from what it read, and writes back. Every such exchange has a
  window in which another writer can intervene, and a merge patch applied to state the client never saw is the
  characteristic way an update is silently lost.
* An operation MAY require `If-Match` rather than merely honouring it, when a blind overwrite would be unsafe. It MUST
  then reject a request that does not carry one with `428`, and MUST document that requirement.

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
* A `GET` on a single resource SHOULD return a strong `ETag`. Collection responses MAY.
* `ETag` values MUST be opaque. Consumers MUST NOT parse them. An `ETag` MUST change whenever the representation
  changes, and is scoped to the full request URL, query parameters included: two pages of the same collection are two
  representations with two `ETag` values.
* A `GET` carrying an `If-None-Match` that matches the current `ETag` MUST return `304`, with no body, carrying the
  same `ETag` and `Cache-Control` the `200` would have carried. A `304` is not an error and MUST NOT carry problem
  details.
* `Last-Modified` and `If-Modified-Since` MAY be supported in addition. `ETag` is preferred, and takes precedence
  where both are present.
* The same `ETag` drives optimistic concurrency through `If-Match`; see section 13. Because most responses carrying
  personal data are `no-store`, and therefore cannot be revalidated by the client, `If-Match` concurrency — not `304`
  revalidation — is the primary reason to emit an `ETag` in our APIs. `304` benefits only the minority of responses
  that are cacheable at all.

### 15. The specification is the contract

* Every API MUST publish an **OpenAPI 3.x** specification, and that specification MUST be generated from, or verified
  against, the implementation. A specification that can drift from the code is not a contract.
* Every operation MUST document a `summary`, a description, all success responses, and all error responses the
  application returns — with their problem-detail schemas and `code` values. Codes emitted by the framework are
  declared once for the specification as a whole; see section 5.
* Every property MUST document its type, format, constraints, and whether it is required.
* Every operation accepting `PATCH` MUST declare a **dedicated patch schema**, separate from the resource schema and
  bound to the `application/merge-patch+json` content type. In that schema every property is optional, clearable
  properties are nullable, and server-managed properties are absent or `readOnly: true`. Reusing the resource schema
  for a patch body misdeclares required properties and tells consumers they may patch `uuid` and `createdAt`.
* Generated SDKs MUST preserve the three states a merge-patch property can be in — absent, `null`, and a value. This
  is the distinction RFC 7396 is built on, and the plain optional-nullable field that generators emit by default
  collapses "leave unchanged" and "clear" into one value, making merge patch unusable through the SDK. The
  representation is language-specific and is fixed by the shared generator configuration, not per service; see
  Appendix A.
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
  resource, a three-state binding that no generator produces by default, and optimistic concurrency on every resource
  that accepts it. That is more work than adding another `PUT`. It is accepted because the alternative — the `PUT`
  everywhere pattern the Context section describes — makes every partial update a full-representation write, and a
  client that writes back a representation it did not fully understand clears data without anyone noticing.
* **Versioning has an ongoing cost.** Serving two major versions in parallel means maintaining two mappings and two
  sets of tests for the duration of the deprecation window. This is the intended trade-off: it makes breaking changes
  visible and deliberate rather than silent.
* **Additive evolution constrains design.** Getting a resource model wrong is now expensive to correct, which raises
  the value of upfront design review — and means teams will occasionally carry an imperfect model longer than they
  would like.
* **New CI friction.** Spectral linting and specification review will reject changes that previously merged silently.
* **Existing services will drift from the standard.** Because remediation is opportunistic, the organization will
  operate with two generations of API conventions for an extended period. This is accepted in exchange for not
  halting feature work.
* **Shared tooling is required.** A shared problem-details model, exception mapper, pagination envelope, and
  `Idempotency-Key` implementation must be published in the organization's shared libraries so that each service does
  not reimplement them — and diverge again. Appendix A lists what this ASR depends on.

## Alternatives Considered

* **kebab-case paths.** kebab-case is the more common convention in the wider industry and reads slightly better in
  URLs. It was rejected because camelCase is already the dominant convention in our services and matches our JSON
  bodies, query parameters, and both target SDK languages. Mass-renaming existing paths would be a breaking change
  with no functional benefit.
* **Keeping the existing `{ code, message }` error schema.** It is already shared by most services and is simple. It
  was rejected because it carries no field-level validation detail, no correlation identifier, and no stable problem
  taxonomy — every service ended up inventing its own enum. RFC 9457 provides all of that as a standard that tooling
  already understands, and the `code` extension preserves what was useful about the existing shape.
* **JSON Patch (RFC 6902) instead of JSON Merge Patch.** JSON Patch is the more capable of the two formats, and the
  advantages are real: it can add, remove and reorder individual array elements, its `test` operation gives a
  precondition at property granularity, and a sequence of operations expresses an intent that merge patch cannot. It
  was rejected because a patch body is an untyped list of operation objects, and everything this ASR depends on breaks
  against it. It cannot be generated as a typed SDK method, so consumers would hand-build operation arrays against the
  very services whose clients section 15 requires to be generated. It cannot be validated against the resource schema,
  so per-property constraints and `readOnly` markings — the mechanism section 3 relies on to reject writes to
  `uuid` — have nothing to attach to. Per-field authorization becomes an interpretation of `path` strings rather than
  a property check. And its power is what makes it dangerous: `move`, `copy` and index-based array operations invite
  clients to encode assumptions about server-side ordering. Merge patch is weaker, and its weakness is the point — a
  merge patch is shaped like the resource, so it validates, generates and authorizes like the resource. Where merge
  patch is genuinely insufficient, which is element-level array mutation, the answer is to model the element as an
  addressable sub-resource; that produces a better resource model than an operation list would have.
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

This ASR describes a single API surface shared by every service. It only takes effect in practice once the building
blocks it mandates exist as shared, versioned artifacts. Reimplementing them per service is how the divergence
described in the Context section happened in the first place, so each item below MUST be published in the
organization's shared libraries, for each stack of ASR-05, before services are expected to comply with the
corresponding section.

| Artifact                  | Section | Contents                                                                      |
|---------------------------|---------|-------------------------------------------------------------------------------|
| Merge patch support       | 3       | Three-state binding, patch application, and `readOnly` enforcement            |
| Problem-details model     | 7       | RFC 9457 document with the `code`, `errors` and `traceId` extensions          |
| Exception mapper          | 7       | Framework mapping of application and validation errors to problem details     |
| Problem type URI registry | 7       | The `https://errors.ca.equisoft.io` namespace and the common problem types    |
| Collection envelope       | 10      | `items` / `pagination` models and the binding of the paging and `sort` inputs |
| Idempotency-key support   | 13      | Middleware storing and replaying responses per `Idempotency-Key`              |
| Trace context propagation | 13      | W3C `traceparent` / `tracestate` propagation, exposed to the exception mapper |
| Conditional request       | 14      | `ETag` generation, `If-None-Match` and `If-Match` handling, `304` rendering   |
| Shared OpenAPI components | 15      | Reusable schemas and parameters for the models above                          |
| Spectral ruleset          | 15      | The mechanically checkable rules of this ASR, as a published ruleset          |
| SDK generation tooling    | 15      | Shared generator configuration for the TypeScript, PHP and Kotlin clients     |

Rules:

* These artifacts MUST be versioned and released per ASR-03, and their behaviour MUST match this document. When the
  two disagree, this ASR is authoritative and the tooling MUST be corrected.
* A change to this ASR that affects any artifact above MUST be accompanied by the corresponding change to that
  artifact, or by an issue tracking it. A rule that no shared tooling implements is a rule that will be applied
  inconsistently.
* Services MUST consume these artifacts rather than defining local equivalents. A local implementation is acceptable
  only while the shared one does not yet exist for that stack, and it MUST then be replaced.
* Until the Spectral ruleset is published, section 15's linting requirement is aspirational; conformance is verified in
  code review. Publishing the ruleset is what turns this ASR from a written convention into an enforced one.
* The merge patch artifact carries one obligation the others do not: the absent / `null` / value distinction of
  RFC 7396 MUST survive deserialization, application and SDK generation intact. How it is represented — `undefined`
  versus `null` in TypeScript, a wrapper type in Kotlin and PHP — is decided once, by this artifact and the shared
  generator configuration, and MUST NOT be decided per service. A service that models a merge patch as an ordinary
  optional-nullable structure cannot distinguish "leave unchanged" from "clear", and will silently corrupt data.

## Appendix B: Migrating existing error responses to RFC 9457

Existing services return `{ "code": "NOT_FOUND", "message": "..." }` with `application/json`. The migration to RFC 9457
is deliberately designed so that the machine-readable part of the contract — `code` — is preserved.

1. **Map the existing enum.** Every value of the service's current `ErrorCodes` enum MUST be mapped to a `type` URI
   and a `title`. The enum value itself becomes the `code` extension member, unchanged. Existing consumers that branch
   on `code` therefore continue to work once they read it from the new payload.
2. **Introduce the problem-details model.** Adopt the shared model from the organization's shared libraries, per
   Appendix A, rather than defining a local one. Populate `type`, `title`, `status`, `detail`, `instance`, `code` and
   `traceId`. Where the legacy `message` carried a human-readable explanation, it becomes `detail`.
3. **Add validation details.** Map framework validation failures to the `errors` array, with one entry per invalid
   input: a JSON Pointer in `pointer` for body inputs, or the parameter name plus the matching `in` discriminator for
   query, path and header inputs. This is new information and breaks nothing.
4. **Switch the content type at the version boundary.** Changing the error content type and body shape is a breaking
   change. It MUST be introduced with the service's next major API version, alongside the `/v{major}` path prefix
   required by section 9. The previous version continues to return the legacy shape until it is sunset.
5. **Regenerate SDKs before migrating consumers.** Publish the regenerated SDK for the new version first, then migrate
   consumers one at a time, then sunset the old version per section 9.
6. **Unversioned services adopting `/v1` for the first time** MUST treat their existing surface as `v1` and serve it
   unchanged, then introduce problem details in `v2` — unless every consumer can be migrated at once, in which case the
   change MAY be made directly in `v1` with the agreement of all consumers, recorded in the pull request.

Migration is opportunistic per section 1, but it MUST NOT be deferred past the service's next major API version.
