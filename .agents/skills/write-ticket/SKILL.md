---
name: write-ticket
description: Draft actionable Jira work items using verified project types and separate acceptance-criteria fields.
---

# Write a Jira Ticket

Load `jira-context` and `jira-sdlc`. Require an explicit project key. Verify
`https://equisoft.atlassian.net/`, project identity, available issue types, the selected
type's create fields, required fields, and supported parent relationship before drafting
a create. Never assume a default project, workflow, custom field, or hierarchy.

Ask for missing business context instead of fabricating requirements. Keep the summary
short and description independently understandable. Preserve source references, related
issues, constraints, and the ticket's language.

Use a type-appropriate outline only when that type exists in the target project:

- **Story:** user, capability, outcome, scope/exclusions, and testable criteria.
- **Feature:** outcome, user/business value, scope, dependencies, and testable criteria.
- **Technical enabler:** deliverable, technical rationale, boundaries, dependencies,
  operational impact, and verifiable completion criteria; do not force user-story form.
- **Bug:** observed/expected behavior, reproduction, environment/version, impact,
  evidence, and regression criteria.
- **Task:** deliverable, boundaries, dependencies, and completion checks.
- **Epic:** outcome, scope/non-goals, success measures, dependencies, and candidate
  children. **Initiative:** business goal, measurable outcomes, constraints, dependencies,
  and proposed Epics. Verify both type and parent/child support first.
- For any other type, discover its meaning and ask for context if its expected deliverable
  is unclear; do not silently relabel the work.

Acceptance Criteria are mandatory for every issue type except **Initiative**, **Epic**,
**Task**, and **Sub-Task**. Do not block drafting or readiness for those four types when
criteria are missing; include them when useful and supported. For every other type,
draft testable criteria in the dedicated **Acceptance Criteria** field when available
and show them separately from the description. If required criteria cannot be entered
because the field is absent from the create screen, report that capability gap and do
not silently hide them in the description. Existing tickets may expose criteria on a
different editable screen; inspect actual metadata before proposing edits.

Check readiness for a clear stakeholder, scope/exclusions, dependencies/risks, and
enough information to estimate. For issue types that require criteria, verify that they
are testable. Identify unanswered questions, placeholder templates, and relevant
approval gates. A draft does not assert that an Approver or Tester exists.

Present the exact project, issue type, parent, summary, description, Acceptance Criteria,
and every additional field. Obtain explicit confirmation **before each individual
create or update**. Confirmation authorizes that exact payload only. If anything changes,
including fresh Jira state or required fields, stop, show the revised payload, and obtain
new confirmation. Never invent an issue key or treat general initiative approval as
authorization to create children in batch. Initiative planning instead defaults to
the initiative planner's one explicit exact-tree create-only batch confirmation:
all reviewed Initiative, Epic, and Story creates proceed without additional create
prompts. This exception applies only to its approved create set, not ordinary ticket
operations or updates; new or changed nodes require revised batch approval.
