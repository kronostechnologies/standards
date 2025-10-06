# ASR-03: Project versioning

Status: Accepted

## Context

We need a consistent way to version our projects to ensure compatibility and traceability across different components
and services. A clear versioning strategy helps in managing releases, dependencies, and updates effectively.

## Decision

We will adopt Semantic Versioning (SemVer) for all our projects. This means that each version will be in the format of
MAJOR.MINOR.PATCH, where:

- MAJOR version is incremented for incompatible API changes,
- MINOR version is incremented for backward-compatible functionality or as part of our regular release cycle, and
- PATCH version is incremented for backward-compatible bug fixes.

Versions will be tagged in the repository using Git tags to mark release points. Version tags will be created against
commits that are present in the main branch or release branches (
See [ASR-01_repository-management.md](ASR-01_repository-management.md)). Only authorized release operators will have
permissions to create new tags.

To limit overhead, we will not track versions in package management files (e.g., package.json, gradle.build). When a new
version is tagged, automated tools will update these files as part of the release process and include them in build
artifacts.

## Consequences

- All release operators must adhere to the SemVer guidelines when updating version numbers.
- Automated tools will be set up to assist in version management and ensure consistency.

## Additional Guidelines

Major releases are only created when there are breaking changes to the public API. Major visual changes or dependency
updates that do not affect the public API should be released as minor versions.
