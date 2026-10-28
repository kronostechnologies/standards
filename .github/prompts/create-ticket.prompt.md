---
description: Draft and optionally create a Jira ticket in an explicitly selected project.
---

# Create Ticket

Ask for the project key, issue type, and problem/outcome if not provided; never choose
a default project. Use `write-ticket`, `jira-context`, and `jira-sdlc` to verify the
project, issue type, required fields, and supported parent relationship on
https://equisoft.atlassian.net/ and prepare an actionable draft. Show project, type,
parent (if any), summary, description, Acceptance Criteria when required or useful
(optional for Initiative, Epic, Task, and Sub-Task), where supported,
all other fields, and unresolved capability gaps. Preserve the source language and
rich-text content.

Stay read-only if the user wants a draft. Otherwise, create only after explicit
confirmation of the exact payload. General approval is not batch authorization.
Report the returned key and URL only after reading the issue back and verifying the
intended fields; report failures or uncertain outcomes without blind retries.
