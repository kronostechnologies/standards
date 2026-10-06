---
name: ticket-to-implementation
description: Turn a verified Jira issue into an implementation plan and correctly keyed branch.
---

# Start Work from a Ticket

Require a full issue key (or an explicit project key plus issue number). Load
`jira-context` and `jira-sdlc`; validate canonical site and issue identity. Read
description, the separate Acceptance Criteria field, comments as needed, parent,
directional dependencies, and relevant children. Report missing, inaccessible, partial,
or incomplete context; never infer requirements from a summary or description alone.

Compare the ticket with repository behavior and applicable instructions. Summarize
scope, source-linked criteria where required or present, constraints, dependencies,
approval evidence, lifecycle readiness, and open questions. If required
criteria are missing, changed after approval, or history is incomplete, state the
gate/unknown and resolve material ambiguity before recommending implementation
readiness. Missing criteria alone are not a blocker for Initiative, Epic, Task, or
Sub-Task. Preserve the source ticket's language.

Propose a concrete implementation and targeted validation plan. Suggest a branch only
when useful and align its prefix with repository instructions and
`ticket-conventions` (feature/bug branches and ongoing `dev/` work are distinct).
Do not create/switch branches or change Jira without explicit authorization. If the user
requests Jira updates, use `update-ticket`; confirm each operation and exact payload
individually.
