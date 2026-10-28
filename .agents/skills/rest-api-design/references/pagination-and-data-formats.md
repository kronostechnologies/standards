# Content Types, Collections and Data Formats (ASR-15 sections 8, 10 and 11)

## Content types

- Bodies use `application/json`; errors use `application/problem+json`; `PATCH` uses `application/merge-patch+json`
  (`PATCH` sent as `application/json` is rejected with `415`, never accepted as a merge patch).
- Binary payloads use `application/octet-stream`, or `multipart/form-data` when metadata accompanies the file.
  Do not base64-encode binary in JSON except small payloads where a separate request is impractical, and document that choice.
- Omit the `charset` parameter; JSON is always UTF-8.
- No vendor media types (`application/vnd.*`) and no hypermedia formats (HAL, JSON:API).
- Unsupported request `Content-Type` is rejected with `415`. An unsatisfiable `Accept` SHOULD still be served `application/json`.
- JSON response bodies are objects at the top level, never arrays or scalars.

## Collection envelope

Collection responses MUST be a JSON object with an `items` array, never a bare array:

```json
{
  "items": [ { "uuid": "..." } ],
  "pagination": { "page": 1, "pageSize": 50, "totalItems": 327, "totalPages": 7 }
}
```

## Pagination

Every collection that can grow without bound MUST be paginated from its first release (retrofitting is breaking).

| Aspect | Page pagination (default) | Cursor pagination |
|---|---|---|
| When | MUST be used unless there is a specific reason not to. | MAY be used for large or high-churn datasets where counting is expensive or items shifting between pages is unacceptable. |
| Request | `page` (1-based, default `1`) and `pageSize`. | `pageToken` (opaque, absent for first page) and `pageSize`. |
| Response | `pagination.page`, `pageSize`, `totalItems`, `totalPages`. | `pagination.pageSize`, `nextPageToken` (`null` on last page), `previousPageToken` (`null` on first page or if forward-only). |
| Edge cases | A page beyond the last returns an empty `items` array plus pagination metadata, not `404`. | `pageToken` is opaque: never document or parse its contents. Do not return `totalItems` unless cheap. |

- Offer exactly one scheme per endpoint; never accept both `page` and `pageToken`.
- Switching from page to cursor pagination is breaking.
- `pageSize` has a documented default and maximum; requests above the maximum are **clamped**, not rejected.
- Results MUST have a deterministic total order.

## Filtering and sorting

- Filters are individual camelCase query parameters named after the property: `?status=ACTIVE&createdAfter=2025-01-01T00:00:00Z`. Do not invent a query language.
- Multi-valued filters are repeated parameters (`?status=ACTIVE&status=PENDING`), not comma-separated.
- Range filters SHOULD use `<property>After`/`<property>Before` for times and `min<Property>`/`max<Property>` for quantities.
- Free-text search uses a single `query` parameter.
- Sorting uses a single `sort` parameter, comma-separated, `-` prefix for descending: `?sort=-createdAt,displayName`. No separate direction parameter.
- Document sortable and filterable properties. Unknown values are rejected with `400`.

## Data formats

| Type | Rule | Example |
|---|---|---|
| Identifiers | **UUIDs**, lowercase canonical strings; never database sequence IDs. | `e4c82d21-13e9-4186-988a-13940cdf191c` |
| Timestamps | RFC 3339 / ISO 8601, **UTC**, explicit offset; no epoch numbers; names SHOULD end in `At`. | `"createdAt": "2025-01-15T14:32:07Z"` |
| Calendar dates | `YYYY-MM-DD` strings, never timestamps. | `"birthDate": "1990-06-01"` |
| Durations | ISO 8601 durations, or an integer with the unit in the property name. | `"P30D"`, `"timeoutSeconds": 30` |
| Money | **Decimal string** amount plus ISO 4217 currency; never floating point. | `{ "amount": "1234.56", "currency": "CAD" }` |
| Languages/countries | ISO 639-1 languages; ISO 3166-1 alpha-2 countries. | `"fr"`, `"CA"` |

- Absent and `null` MUST NOT mean different things in a response (they do in `PATCH` requests). Serialize a
  nullable property whose value is `null` explicitly; never omit it, so clients can build their next merge patch from the last response.
- Serialize empty collections as `[]`, never `null` or omitted.
- Every string property MUST have a documented maximum length; never accept unbounded strings.
