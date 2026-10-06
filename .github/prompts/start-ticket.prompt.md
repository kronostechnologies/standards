---
description: Read a Jira issue and prepare an implementation plan for it.
---

# Start Ticket

Use the `ticket-to-implementation`, `jira-context`, `jira-sdlc`, and
`ticket-conventions` skills. Ask for a full Jira issue key if none was provided (the
key supplies the project key). Verify the issue on https://equisoft.atlassian.net/,
read description, Acceptance Criteria when required or present, dependencies and
approval evidence, then
inspect the relevant codebase and draft a scoped implementation/validation plan with
open questions. Flag missing criteria for issue types that require them, changed
criteria, and incomplete approval history;
do not claim implementation readiness without evidence. Suggest a repository-aligned,
issue-keyed branch when useful. Do not create a branch or change Jira without explicit
authorization. Use direct retrieval for simple reads; do not invoke another agent just
to relay a Jira lookup.
