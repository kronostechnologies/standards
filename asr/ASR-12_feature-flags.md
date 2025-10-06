# ASR-12: Feature Flags

Status: Accepted

## Context

Feature flags (also known as feature toggles) are a powerful technique in software development that allows teams to
enable or disable specific features or functionalities in an application without deploying new code. This approach
provides several benefits, including the ability to test new features in production, perform gradual rollouts, and
quickly disable features that may be causing issues (kill switch). By implementing feature flags, we can improve our
development workflow, enhance collaboration between teams, and reduce the risk associated with deploying new features.

## Decision

We will implement a feature flag management system to control the activation and deactivation of features in our
applications. All major stakeholders, including developers, business analysts and product owners, will be involved in
the feature flag process to ensure alignment with business goals and technical requirements.

To facilitate the targeting of feature flags, we will expose the following attributes to the feature flag management
system:

| Field         | Format                                                                                                                                                                                |
|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| User ID       | This must be the global user ID (account-service), not an application specific user ID. When running from an anonymous context (ie: scheduled task), the user ID should be anonymous. |
| appVersion    | The version of the application. This should follow SemVer ).                                                                                                                          |
| env           | The environment the application is running in (e.g., development, staging, production).                                                                                               |
| site          | _ca_, _us_ or a dedicated customer identifier.                                                                                                                                        |
| lang          | The language preference of the user in ISO 639-1 format.                                                                                                                              |
| organizations | Comma-separated set of organization IDs that the current user belongs to.                                                                                                             |
|

Split.io will be used as the feature flag management system. Feature flags will be defined and managed through Split.io,
and the application will integrate with Split.io SDK to evaluate and apply feature flags at runtime.

## Consequences

- Improved Development Workflow: Feature flags will allow us to test new features in production and perform gradual
  rollouts, reducing the risk associated with deploying new code.
- Enhanced Collaboration: Involving all major stakeholders in the feature flag process will ensure that features are
  aligned with business goals and technical requirements.
- Faster Development Cycles: Feature flags will enable us to release features more quickly and iterate based on user
  feedback. Less reliance on feature branches will also reduce merge conflicts and integration issues.
- Quick Issue Resolution: The ability to quickly disable features that may be causing issues will help maintain
  application stability and improve user experience.
- Ongoing Maintenance: Feature flags will need to be periodically reviewed and updated or removed to ensure they remain
  relevant.
- Increased Complexity: Implementing feature flags introduces additional complexity to the codebase.
