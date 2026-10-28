# ASR-02: Project Onboarding

Status: Accepted

## Context

Many project use different languages, frameworks and technologies. Repositories may also require different versions of
these technologies, making it difficult for developers to work on multiple codebases simultaneously. When a developer
opens a project for the first time, he has to read a readme file and perform various steps to bring a project in a
working state. Oftentimes that readme file is either missing or outdated.

We want to improve the onboarding experience for new developers and reduce the time it takes to get a project up and
running on a local machine. This includes setting up the development environment, installing dependencies, and
configuring any necessary tools.

## Decision

To streamline the onboarding process, we will implement the following practices:

1. Limit the required tools to:
    - Git
    - Docker
    - Make
    - asdf-vm

   Through Docker and asdf-vm, we can manage most dependencies and tools required for development without requiring
   developers to install them directly on their machines.
2. Each repository must include a `Makefile` that defines pre-defined tasks to set up the development environment, run
   tests, and build the application.
3. Each repository must include an `.tool-versions` file that specifies the required versions of languages and tools
   used in the project. This file is the single source of truth for these versions and is used by asdf-vm to install
   and manage them. Do not duplicate tool version pins in setup scripts, Makefiles, or CI configuration.
4. PHP is the exception: because its runtime is too heavy to manage through asdf-vm, projects must provide PHP through
   a container image pinned to an exact PHP version instead of listing it in `.tool-versions`.
5. Yarn is managed through Corepack, not asdf-vm. JavaScript projects using Yarn may declare its exact Yarn Berry
   version in the `packageManager` field of `package.json`; Corepack must be enabled in local setup and CI and invoked
   with `corepack yarn` so it selects that version. Do not list Yarn in `.tool-versions`, install Yarn through asdf
   (including Yarn Classic), or commit a Yarn release binary.

### Makefile Targets

The make targets are idempotent. They may be run multiple times without adverse effects. It is possible that a target is
irrelevant on a particular repository. In that case, the target must still be present, but act as a no-op. For example
PHP code is not compiled, so `make compile` will do nothing.

The following targets must always be defined for all repositories:

| Target          | Description                                                                                                                                                                                                                                                                                                                                                                                                              |
|-----------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `all`           | Default target. Execute setup, check, test, compile and package targets. This is the command to run by developers the first time they clone a repository. This can also be run anytime to know that the repository in a stable state.                                                                                                                                                                                    |
| `setup`         | Bring the repository in a usable state for the developer. This includes, but is not limited to, checking if requirements are available and installing asdf-vm tool versions. If additional requirements are not met and it is not possible to use them through Docker or asdf-vm, then this command must check their presence or fail otherwise. Clear instructions on what to do next should be printed for developers. |
| `check`         | Execute all static checks. Fail when checks fail, success otherwise.                                                                                                                                                                                                                                                                                                                                                     |
| `test`          | Execute all tests. Fail when tests fail, success otherwise.                                                                                                                                                                                                                                                                                                                                                              |
| `compile`       | Execute all compilation and transpilation steps.                                                                                                                                                                                                                                                                                                                                                                         |
| `package`       | Create all distributable packages.                                                                                                                                                                                                                                                                                                                                                                                       |
| `package.image` | Build a container images for distribution purposes.                                                                                                                                                                                                                                                                                                                                                                      |

## Consequences

- Developers can get a project up and running with a single command: `make all`.
- The real tooling is hidden behind asdf-vm and may lack standardization across projects. However, this allows projects
  to be updated independently.
- Tool versions have one source of truth in `.tool-versions`, with PHP and Yarn handled by their explicit exceptions.
- Developers need to install the required tools. However, these tools should already be installed as part
  of https://github.com/kronostechnologies/bootcamp setup.
