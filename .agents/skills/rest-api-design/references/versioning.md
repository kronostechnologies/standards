# Versioning and Breaking Changes (ASR-15 sections 1 and 9)

## Scope

- ASR-15 applies to all new HTTP APIs and to existing APIs being redesigned or released under a new major version.
- Existing APIs migrate opportunistically, at the latest when they publish their next major version.
- **Internal BFF endpoints** (consumed only by their own frontend, deployed in lockstep, not exposed to any other
  consumer) are exempt from this file's versioning and breaking-change rules; all other sections apply to them.
- Non-REST interfaces (message broker contracts) are out of scope.

## Versioned paths

- Every API carries a **major version as the first path segment** (`/v1/users`, `/v2/users`), from the very
  first release even if no second version is expected.
- Major only: no minor/patch numbers in the path (those come from the service's own SemVer, ASR-03).
- Never place the version elsewhere: not mid-path, query parameter, custom header, or vendor media type.

## Breaking versus non-breaking

Evolve APIs additively. Non-breaking changes are applied in place; breaking changes MUST NOT be made to a published version.

| Non-breaking — apply in place | Breaking — requires a new major version |
|---|---|
| Add an endpoint, or a new optional query parameter with a safe default. | Remove or rename an endpoint, parameter or response property. |
| Add a response property. | Change the type, format or unit of an existing property. |
| Add an optional request property. | Add a required request property, or a new constraint on an existing one. |
| Add an enum value **sent only by the client**. | Add an enum value **returned by the server**, unless tolerance for unknown values was documented up front. |
| Relax a validation constraint. | Change the status code or error `code` for an existing condition. |
| Add a new error `code` under an existing status code. | Change defaults, sort order or pagination behaviour (including page to cursor, or adding pagination to an unpaginated collection). |
| | Change a synchronous operation (`200`, `201`, `204`) to `202` plus an operation resource, or vice versa. |

Generated SDKs deserialize strictly, so new APIs SHOULD declare tolerance for unknown enum values explicitly.

## Introducing a new major version

A new major version is a last resort, used only when the change cannot be additive.

1. Serve the new version in parallel with the previous one from a single deployment.
2. Mark the previous version `deprecated: true` in OpenAPI, with a description pointing at its replacement.
3. Deprecated responses MUST include `Deprecation` (RFC 9745) and `Sunset` (RFC 8594) headers, and SHOULD add a
   `Link` header with `rel="deprecation"` to the migration docs:

   ```
   Deprecation: @1767225600
   Sunset: Fri, 01 Jan 2027 00:00:00 GMT
   Link: <https://docs.equisoft.io/api/migrations/v1-to-v2>; rel="deprecation"
   ```

4. Identify remaining consumers before removal; MUST NOT remove a version that still has active consumers.
5. Removing a major version is a breaking change to the service and is released as a major version under ASR-03.

Individual properties and operations MAY be deprecated within a version with `deprecated: true`, provided they
keep working until the next major version.
