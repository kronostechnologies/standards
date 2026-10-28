---
name: jira-sdlc
description: Apply Equisoft AWT SDLC evidence and human approval gates to Jira planning, refinement, and workflow actions.
---

# Jira SDLC Evidence and Gates

Apply the canonical lifecycle in `kronostechnologies/standards/docs/sdlc.md`
(Requirements Definition, Requirement Validation and Approval, Code Approval,
Manual Testing, and Production Deployment). Keep this skill usable when that source
repository is not installed: the contract below summarizes the relevant requirements.

## Requirement evidence

- Acceptance Criteria are mandatory for every issue type except **Initiative**, **Epic**,
  **Task**, and **Sub-Task**. Those four types may omit them without being incomplete.
  For all other types, the **Acceptance Criteria** field contains the test criteria for
  the requirement; keep it distinct from the description wherever the project/type
  supports that field.
- Draft criteria must be testable and trace to an explicit source requirement. Mark
  missing business decisions as questions; never invent thresholds, actors, scope,
  or expected behavior to fill a template.
- Distinguish blank, placeholder, unavailable, and verified criteria. A populated
  default DoR/DoD template is not evidence that its checklist was completed.

## Human validation and approval

- Requirement approval is performed by an authorized Product Owner, Business Analyst,
  or Team Lead. The Approver field must identify the requirement approver.
- The transition to **Selected for Development** represents requirement approval and
  is restricted to the evidence owners. Before recommending or making that transition,
  verify available transition fields and inspect approval evidence/history when available.
- Any edit to Acceptance Criteria after approval requires formal reapproval. Compare
  the current criteria and recorded history; if history is incomplete or the prior
  approved value cannot be verified, report approval as unknown and ask the stakeholder.
- User confirmation authorizes only the exact operation described. It does not make
  the user an organizational approver, tester, or release operator; never populate
  Approver or Tester with an inferred identity.
- Read-only investigation and planning may proceed for an unapproved ticket, but
  label the approval gate and do not imply it is ready for development.

## Code and test evidence

- PR titles include the verified Jira issue key. Code review and automated analyses
  are separate evidence; neither proves requirement approval or manual testing.
- Acceptance Criteria define the manual test grid for feature/bug tickets. The Tester
  field and **Tested** transition record authorized testing. Do not infer either from
  a merged PR, passing build, or a generic Done status.
- Project statuses and available transitions differ. Resolve the actual transition
  destination and required fields; a transition's display name is not necessarily
  its destination. Do not assume that statusCategory Done means Tested or Released.

## Release evidence

Release-plan preparation, risk documentation, acceptance testing, deployment approval,
production deployment, and release-plan closure are distinct steps. Do not perform,
combine, or mark these complete through ticket-planning workflows. Require the
responsible human evidence owner and applicable release process.

For each task, report one of:

- **Ready to plan** — scope and test criteria, when required for the issue type, are
  sufficiently evidenced.
- **Clarification required** — requirements or capability discovery leave a material
  decision unresolved.
- **Approval evidence missing** — planning may continue, but the lifecycle approval
  gate is not evidenced.
- **Reapproval required** — criteria changed after approval or history proves a change.
- **Unknown** — required history, field, role, or permission evidence is unavailable.

Give the reason and evidence for the status. Never convert unavailable evidence into
an assertion of compliance or noncompliance.
