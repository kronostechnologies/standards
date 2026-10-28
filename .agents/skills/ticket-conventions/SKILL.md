---
name: ticket-conventions
description: Apply Equisoft AWT Jira issue-key conventions to branches, commits, PR titles, and TODOs.
---

# Ticket Conventions

- Require an explicit project key (or a full issue key containing it); never assume a
  default. Use the canonical issue key returned by Jira, e.g. `QCTECH-123`.
- Follow the repository's branch instructions and `release-and-versioning` skill.
  ASR-01 uses `dev/` for ongoing development (features and/or bug fixes), `feature/` for long-lived feature branches,
  and `release/` for release branches. If no repository convention resolves the choice, state a short issue-keyed
  suggestion and ask before switching or renaming an existing branch. Prefer short-lived branches and feature flags.
- Include the issue key in PR titles for traceability (ASR-01). Example:
  `feat: add search filters (QCTECH-123)`.
- When suggesting a commit subject, follow the repository's commit convention and include
  the issue key where the repository requires it; e.g. `fix: handle empty results (QCTECH-123)`.
- Every `TODO` or `FIXME` introduced in code must reference a Jira issue key. Avoid creating
  placeholder tickets or keys.
- Before linking, mentioning, or changing an issue, verify the key in Jira and use its
  actual project and URL. Do not silently switch to another project.
