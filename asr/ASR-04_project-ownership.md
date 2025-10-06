# ASR-04: Repository management

Status: Accepted

## Context

As our development team expands, it becomes increasingly important to establish clear ownership and access controls for
our projects. Different team members have varying roles and responsibilities, necessitating a structured approach to
project and repository management. Without defined ownership, there is a risk of unauthorized changes,
miscommunication,and potential security vulnerabilities. To ensure the integrity of our codebase and streamline
collaboration, we need to implement a system that clearly delineates who has access to what parts of the project based
on their roles within the team.

## Decision

We will implement role-based access control to protect sensitive parts of the projects and repositories and ensure that
only authorized personnel can contribute, release or administer repositories. The codebase will be protected via a
CODEOWNERS file, which will define the ownership and review requirements for different parts of the codebase.

Each team will be declined into the following roles:

- All team members: A parent group containing all team members, their peer review will be required for all pull
  requests.
- Release Operators: Responsible for managing releases, they will have additional permissions to create release
  branches, merge changes into release branches, and create new version tags.
- Leadership: Responsible for overseeing the team's work and managing repository settings, they will have administrative
  access to manage a subset of repository settings and overall governance.

Additionally, the AWT developers group will be created to include all developers working on AWT projects. AWT developers
will be granted write access to development and feature branches, allowing them to contribute code changes across all
projects.

## Consequences

1. Enhanced Security: By restricting access based on roles, we reduce the risk of unauthorized changes and potential
   security vulnerabilities.
2. Clear Accountability: Defined ownership ensures that team members are aware of their responsibilities, leading to
   better accountability and oversight.
3. Streamlined Collaboration: Role-based access control facilitates smoother collaboration by ensuring that team members
   can only access the parts of the project relevant to their roles.
4. Improved Code Quality: Requiring peer reviews from the owner team for all pull requests makes sure no unplanned
   changes are introduced into the codebase.

## Example

Using the equisoft-plan repository as an example, the following teams will be defined:

- AWT: All AWT developers, including those not part of the Plan team.
- Plan: All members of the Plan team, including leadership and release operators.
- Plan Release Ops: Members of the Plan team with release management responsibilities.
- Plan Leadership: Members of the Plan team with leadership responsibilities. Typically only the team lead. Can
  occasionally include other people if the team lead is unavailable for an extended period.
