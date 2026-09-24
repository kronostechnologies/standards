# AGENTS.md

This is the **standards** repository for the Kronos/Equisoft organization. It contains the canonical
Architecturally Significant Requirements (ASRs), shared tooling configurations, and coding standards
used across all repositories.

---

## Organization-Wide Standards

Organization-wide standards for AI coding agents are derived from the ASRs in `asr/` and the tooling
documented in `docs/analysis-tooling.md`. They cover languages & stack, project setup, repository &
versioning, code quality & CI, dependency management, and feature flags, and apply to **all**
repositories in the organization.

### Downstream Usage

**Recommended: consume the `org-standards` [APM](https://microsoft.github.io/apm/) package.**
Add a dependency on `kronostechnologies/agent-toolkit/plugins/org-standards` (pinned to a tag or
commit SHA, per ASR-14) in your repository's `apm.yml`, then run `apm install`. This deploys a
small always-on core plus several narrowly-scoped instruction files and skills, so agents only load
the rules relevant to what they're touching instead of one large always-on document. See
[`kronostechnologies/agent-toolkit`](https://github.com/kronostechnologies/agent-toolkit) for setup
details.

**Pre-migration fallback: raw URL.** Repositories not yet on APM may reference the frozen,
no-longer-updated snapshot in their own `AGENTS.md`:

```markdown
You **must** read and follow the organization-wide standards defined at:
https://raw.githubusercontent.com/kronostechnologies/standards/master/docs/instructions/asr.instructions.md
```

This file will go stale as the ASRs evolve — treat it as a bridge while migrating to the APM
package, not a long-term source of truth.

---

## Repository-Specific Instructions

<!-- Add any instructions specific to this standards repository below. -->
