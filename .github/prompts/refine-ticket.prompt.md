---
description: Review a Jira ticket for readiness and propose field-specific refinements.
---

# Refine Ticket

Require a full Jira issue key (or explicit project key and issue number). Use the
`ticket-refiner` agent with `jira-context` and `jira-sdlc`. Read the separate
Acceptance Criteria field, description, relevant comments, parent, dependencies,
available types/fields, and relevant approval history from
https://equisoft.atlassian.net/. Report incomplete retrieval and preserve the ticket's
language, media, panels, mentions, and links.

Show a readiness status with evidence, questions, duplicates/dependency concerns,
and separate concise before/after diffs for summary, description, Acceptance Criteria,
and any other proposed fields. Do not edit by default. If the user asks to apply
changes, show the exact complete payload, recheck current state and approval gates,
then obtain explicit confirmation for that operation. Confirm comments, links,
assignments, and transitions separately; verify each write by reading it back.
