---
name: feature-flags
description: Integrate Split.io feature flags and expose the required targeting attributes, per Equisoft AWT ASR-12.
---

# Feature Flags

### Split.io (ASR-12)

- **Split.io** is the organization-standard feature flag management system.
- Feature flags are preferred over feature branches and release branches for isolating
  in-progress work.
- All applications must integrate with the **Split.io SDK** to evaluate flags at runtime.
- Feature flags must be periodically reviewed and removed when no longer relevant.

### Targeting Attributes

You must expose the following attributes to Split.io for flag targeting:

| Attribute | Format / Notes |
|---|---|
| **User ID** | Global user ID from account-service (not application-specific). Use `anonymous` for non-user contexts (e.g., scheduled tasks). |
| **appVersion** | SemVer string of the running application. |
| **env** | Runtime environment: `development`, `staging`, `production`, etc. |
| **site** | `ca`, `us`, or a dedicated customer identifier. |
| **lang** | User language preference in ISO 639-1 format (e.g., `fr`, `en`). |
| **organizations** | Comma-separated list of organization IDs the current user belongs to. |
