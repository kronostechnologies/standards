# Observability, Idempotency, Concurrency and Caching (ASR-15 sections 13 and 14)

## Observability

- Propagate W3C Trace Context (`traceparent`, `tracestate`); create it when a request arrives without one.
  Surface the trace identifier in error responses as `traceId`.
- Do NOT introduce custom correlation headers such as `X-Request-Id`.
- `429` MUST include `Retry-After`; `503` SHOULD.

## Idempotency keys

Non-idempotent operations with significant or irreversible side effects (payments, provisioning, outbound
communications) MUST accept an `Idempotency-Key` request header.

- Keys are client-generated and not globally unique: never key a stored record by the header value alone.
  Scope each record by the authenticated principal (and tenant where distinct), HTTP method and target URI.
  A key from a different principal, method or URI is unrelated: never a replay, never a `409`. Implement this
  once, in the shared idempotency middleware.
- Requests are the **same request** when method, target URI (query string included) and body all match. Compare
  the body by a stored digest, not by re-executing the operation.
- Same key and same request: return the original response unchanged. Same key, same scope, different request:
  `409` as a problem document with a dedicated `code`. A repeat while the original is still processing MUST NOT
  trigger the side effect again: return `409` while the outcome is indeterminate, then the original response once known.
- Retain keys at least 24 hours (SHOULD), and never longer than the associated operation resource stays retrievable.
- For asynchronous operations the "original response" is the original `202`: return it with the same `Location`
  for as long as the operation resource is retrievable, even if it is terminal. Never start a second operation.

## Optimistic concurrency

- Resources subject to concurrent modification SHOULD support it: return an `ETag` on `GET`, accept `If-Match`
  on `PUT`, `PATCH` and `DELETE`, and return `412` when the precondition fails.
- If `ETag` is unsupported, a body version property MAY be used instead and MUST also return `412` on mismatch.
- A resource that accepts `PATCH` MUST support optimistic concurrency.
- An operation MAY **require** `If-Match` when a blind overwrite is unsafe: reject requests without one with `428`
  and document the requirement.
- `If-Match` uses strong comparison. A bulk operation MUST NOT accept `If-Match`.

## Caching and conditional requests

- Every `GET` MUST set an explicit `Cache-Control`; never rely on framework defaults.
- Personal or sensitive data: `Cache-Control: no-store`.
- Caller-dependent responses: `Cache-Control: private`, plus `Vary: Authorization` when cacheable at all.
- `POST`, `PUT`, `PATCH` and `DELETE` responses set `Cache-Control: no-store` (the update `ETag` is a validator for
  the next `If-Match`, not a caching licence). Non-terminal operation resources also use `no-store`; terminal ones follow normal `GET` rules.
- A single-resource `GET` SHOULD return a strong `ETag`; collections MAY. `ETag` values are opaque, change whenever
  the representation changes, and are scoped to the full URL including query parameters.
- `GET`/`HEAD` with a matching `If-None-Match` returns `304` with no body and the same `ETag` and `Cache-Control`
  as the `200`. A `304` is not an error and carries no problem details.
- `If-None-Match` uses **weak** comparison (ignore `W/`); `If-None-Match: *` matches any current representation
  and so returns `304` against an existing resource.
- `Last-Modified`/`If-Modified-Since` MAY be supported; when both validators are present, `If-None-Match` wins and
  `If-Modified-Since` is ignored.
- The primary reason to emit an `ETag` is optimistic concurrency via `If-Match`, not `304` revalidation.
