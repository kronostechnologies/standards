---
name: update-ticket
description: Summarize completed work in Jira and propose comments or workflow transitions safely.
---

# Update a Ticket

Load `jira-context` and `jira-sdlc`. Require an explicit issue key (or project key and
issue number); fetch and verify the canonical issue identity, current status, and
editable fields. Gather the actual PR URL, changes, validation results, and outstanding
work. Do not claim completion or tests passed without evidence.

Draft comments or field changes from verified facts. Keep Acceptance Criteria separate
from description; compare the current approved criteria and relevant history before
proposing a criteria edit. If an approved value changed, or history is incomplete,
report the reapproval gate or unknown. Preserve rich text and media by retaining the
fetched content format; preserve full current multi-value fields before replacement.
Re-read mutable state immediately before a proposed write and stop for renewed
confirmation if the payload-relevant state has changed.

Show the exact target, complete fields/payload, and side effects and get explicit
confirmation **for each operation**. A comment confirmation does not authorize an edit
or transition. For a requested transition, discover the issue's available transitions,
match the destination status (not just the transition name), inspect required fields,
and apply SDLC human-approval/testing/release gates independently. Do not infer Tested,
Released, or approval from Done, a passing build, or a merged PR.

Re-fetch the issue after each write to verify every intended field/result. Report
permission errors, conflicts, uncertain outcomes, and partial progress; do not blindly
retry or roll back.
