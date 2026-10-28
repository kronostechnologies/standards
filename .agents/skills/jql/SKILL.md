---
name: jql
description: Build safe, scoped Jira JQL queries for triage, sprints, epics, and recent activity.
---

# JQL Cookbook

Load `jira-context` when project capabilities, fields, or complete retrieval matter.
Require a project key for every query; replace `PROJECT` with the key supplied by the
user or taken from a supplied issue key. Verify the key and permissions first. Jira
Cloud's field names and hierarchy vary by project: inspect the project's fields and
issue types before relying on custom fields or hierarchy queries.

| Goal | JQL |
| --- | --- |
| My unfinished issues | `project = PROJECT AND assignee = currentUser() AND statusCategory != Done ORDER BY updated DESC` |
| Active sprint | `project = PROJECT AND sprint in openSprints() ORDER BY Rank ASC` |
| Recently updated | `project = PROJECT AND updated >= -7d ORDER BY updated DESC` |
| Label | `project = PROJECT AND labels = "label-name" ORDER BY updated DESC` |
| Component | `project = PROJECT AND component = "Component Name" ORDER BY updated DESC` |
| Epic's direct children | `project = PROJECT AND parent = PROJECT-123 ORDER BY Rank ASC` |

- Quote text containing spaces or JQL syntax; never splice untrusted free text into a
  JQL expression without escaping/validating it.
- `parent` works for supported Jira Cloud hierarchies; if the project uses legacy
  Epic Link or a custom hierarchy, discover the field and query it instead.
- For sprint queries, verify the project is on a Scrum board. For archived or
  inaccessible projects, report the error rather than returning an empty result.
- Paginate until `isLast`/the end token only when a complete inventory is required.
  Otherwise state the sample limit, returned count, and whether a continuation token
  remains. Never describe a partial result as a complete sprint, epic, or initiative list.
- A JQL result row is not a source of project workflow, field metadata, or permissions.
  Use the task-relevant discovery operation for those facts; distinguish status lists
  from transitions available to a particular issue.
