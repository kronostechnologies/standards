# Security and the OpenAPI Contract (ASR-15 sections 12 and 15, Appendix A)

## Authentication and security

- Serve APIs over **TLS only**.
- Authenticate with **OAuth 2.0 bearer tokens** (`Authorization: Bearer ...`). No custom authentication headers
  and no API keys in query parameters. A `401` includes `WWW-Authenticate`.
- Credentials and tokens MUST NOT appear in URLs (paths or query). Personal data MUST NOT appear in path segments;
  it MAY appear in a query parameter only where filtering/searching on it is the documented purpose
  (`?emailAddress=`, `?query=`). Identify such parameters in the OpenAPI spec and redact them in access logs.
- Enforce authorization **per operation on the server**; hiding an operation from a client is not authorization.
- Restrict CORS to the organization's own origins unless there is a specific reason to allow third parties.
- Never echo more data than the caller may see, including in error messages.

## The specification is the contract

- Publish an **OpenAPI 3.x** spec generated from, or verified against, the implementation; it must not be able to drift from the code.
- Every operation documents a `summary`, a description, all success responses, and all application error
  responses with their problem-detail schemas and `code` values. Framework-emitted codes are declared once for the whole spec.
- Every property documents its type, format, constraints and whether it is required. Every string has a maximum length.
- Request schemas of every `POST`, `PUT` and `PATCH` MUST NOT let consumers set a server-managed property: each
  is either absent from the schema or marked `readOnly: true`. Do not reuse the resource schema unchanged as a request body.
- Every operation accepting `PATCH` declares a **dedicated patch schema** bound to `application/merge-patch+json`:
  every property optional, clearable properties nullable, server-managed properties excluded.
- Name dedicated write schemas after resource and operation: `<Resource>Patch`, and `<Resource>CreateRequest` /
  `<Resource>ReplaceRequest` for `POST`/`PUT` with a dedicated request schema.
- Generated SDKs MUST preserve the three merge-patch states (absent, `null`, value). The representation
  (`undefined` vs `null` in TypeScript, a wrapper type in Kotlin and PHP) is fixed by the shared generator
  configuration, never decided per service.
- Lint the spec in CI with **Spectral** using `spectral:oas` and the shared organization configuration; violations
  fail the build (zero tolerance, ASR-08). Until the shared ruleset is published, verify conformance in code review.
- Generate client SDKs from the spec with the shared organization tooling; never maintain hand-written clients for internal APIs.
- Review spec changes as code, paying attention to the breaking-change rules in `versioning.md`.

## Shared artifacts (Appendix A)

Consume the shared, versioned artifacts (ASR-03) instead of local equivalents: merge patch support, bulk
operation support, operation resource, problem-details model, exception mapper, problem type URI registry,
collection envelope, idempotency-key support, trace context propagation, shared OpenAPI components, Spectral
ruleset and SDK generation tooling. A local implementation is acceptable only while the shared one does not
exist for your stack, and must then be replaced. When tooling and ASR-15 disagree, ASR-15 is authoritative.
