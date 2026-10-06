---
description: Draft a Jira initiative hierarchy from a specification, then optionally create it.
---

# Plan Initiative

Ask for a specification document (local path, text, or Confluence page) and an
explicit Jira project key if missing. Invoke `initiative-planner` for the substantive
workflow; it loads `jira-context` and `jira-sdlc` and reads the source and exact
project/type hierarchy on https://equisoft.atlassian.net/. Present a project-compatible
full tree with relevant, source-backed Stories beneath every Epic by default, separate
dependency graph, duplicate candidates, source-to-criteria links, and unknowns. Verify
that Story exists and can be parented to Epic. Ask the user to resolve missing material
requirements before drafting affected Stories or final approval. If Story or its parent
relationship is unavailable, stop and ask which verified alternative issue type or
hierarchy to use; do not silently substitute a type or assume parent relationships.
Use `write-ticket` for story structure and testable Acceptance Criteria.

Default to one explicit create-only batch confirmation of the exact final ordered
tree and every payload, including the Initiative, all Epics, all Epic Stories, and
declared parent placeholders. After confirmation, create the reviewed nodes without
additional per-node or Story-create approval prompts. Draft-only requests remain
read-only; a general initiative request is not final-plan authorization. Use individual
confirmation only if explicitly requested. Create Stories only beneath verified Epic
keys. Batch approval covers create-only operations and only the declared parent-key
substitutions. New or changed nodes require revised approval of the remaining create
set, not recreation of verified nodes. Re-read every created issue and stop/report
partial outcomes on drift, ambiguous results, or verification failure. No edits, links,
comments, assignments, transitions, or deletes are authorized by tree approval; those
require separate confirmation.
