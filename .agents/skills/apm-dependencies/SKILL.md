---
name: apm-dependencies
description: Manage APM dependencies in a repository that consumes kronostechnologies/agent-toolkit packages — adding, updating, or removing dependencies, and diagnosing apm audit failures.
---

# APM Dependency Management

Use this skill when asked to add, update, or remove an APM dependency in a repository, or to
diagnose a failing `apm audit` / APM CI check.

## Adding, updating, or removing a dependency

1. Edit `dependencies.apm` in this repository's `apm.yml`. Coordinates look like
   `kronostechnologies/agent-toolkit/plugins/<package>#<ref>`, where `<ref>` is a commit SHA, tag,
   or branch.
2. Run `apm install` to re-resolve and re-deploy.
3. Commit `apm.lock.yaml` **and** every changed file under `.github/instructions/`,
   `.github/prompts/`, `.github/agents/`, `.agents/skills/`, and `.github/mcp.json` in the same
   commit. Deployed files are checked into git; committing the lockfile without the deployed files
   (or vice versa) fails `apm audit`'s drift check.
4. A package can pull in others transitively via its own `path:` dependencies (e.g.
   `kotlin-backend`, `frontend`, and `php-backend` all depend on `org-standards` this way) — you do not
   need to depend on `org-standards` directly to receive `asr.instructions.md`, though depending on
   it explicitly is fine and keeps intent clear.

## Diagnosing `apm audit --ci` failures

| Check | Cause | Fix |
|---|---|---|
| `content-integrity` / `drift` | A deployed file was hand-edited, or was committed out of sync with the lockfile | Run `apm install` (add `--force` if it reports files as unmanaged) and commit the result |
| `dependency-pinned-constraint` | A dependency uses an unpinned ref (a branch name instead of a SHA/tag) | Re-pin to a commit SHA or semver tag |
| `dependencies.allow` policy rejection | The dependency coordinate doesn't match the org allow-list | See "Org policy" below — this is usually a glob-depth problem, not a real access issue |

## Org policy (`kronostechnologies/.github-private/apm-policy.yml`)

- `dependencies.allow` patterns match the **full** `owner/repo/path` coordinate. A pattern like
  `kronostechnologies/*` matches only one path segment and will reject
  `kronostechnologies/agent-toolkit/plugins/<anything>`. Monorepo subpath dependencies need
  `kronostechnologies/**`.
- Policy is fetched from a separate private repo at audit time — this requires the same
  private-repo access described below, not just read access to the consuming repo.

## Private-repo access in CI

- Use the shared composite action `equisoft-actions/setup-apm` (reference it by its floating
  major tag, e.g. `@v1` — `equisoft-actions` is exempt from SHA-pinning). It reads the consuming
  repo's `.apm-version` file and mints a short-lived GitHub App token, exported as
  `GITHUB_APM_PAT`, scoped by the action's `owner`/`repositories` inputs (defaulting to
  `kronostechnologies`/`agent-toolkit,.github-private`).
- The default `GITHUB_TOKEN` **cannot** read another private repository in the same org — not even
  with the repo's Settings → Actions → General → Access set to "organization" (that setting only
  covers reusable-workflow/action sharing, not git clones or API reads). A GitHub App token or PAT
  is mandatory for any workflow that resolves cross-repo private APM dependencies.
- Never invoke `microsoft/apm-action` without pinning `apm-version` explicitly. An unpinned install
  can resolve a stale cached CLI version whose policy discovery predates
  `.github-private` support, causing `apm audit` to report "No org policy found" and pass
  trivially — a silent false green, not a real audit.

## Authoring and registering a new package in `agent-toolkit`

1. Create `plugins/<name>/apm.yml` plus primitives under `plugins/<name>/.apm/<type>/`
   (`instructions/`, `skills/<skill-name>/SKILL.md`, `agents/`, `prompts/`, etc.).
2. Register the package in all of:
   - root `apm.yml` → `marketplace.packages[]`
   - `.claude-plugin/marketplace.json` → `plugins[]` (generate this via `apm`, don't hand-edit it —
     CI runs `apm pack --check-clean` and fails on drift)
   - `release-please-config.json` → `packages[]`, with `extra-files` version jsonpaths mirroring
     the existing package entries
   - `.release-please-manifest.json`
3. Run `apm audit --ci --no-cache` and `apm pack --check-versions --check-clean` locally before
   opening a PR.
