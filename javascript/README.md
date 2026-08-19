# JavaScript and TypeScript

## Building & Publishing

### [npm](https://npmjs.org)

Releases are fully automated. [release-please](https://github.com/googleapis/release-please) opens a
release pull request based on [conventional commits](https://www.conventionalcommits.org/); merging
it tags the release and triggers the `publish` job of
[`.github/workflows/release-please.yml`](../.github/workflows/release-please.yml), which builds and
publishes the released workspaces to npm.

Respect [semver](https://semver.org/) when writing commit messages, since the version bump is
derived from them:

* `feat!:` / `BREAKING CHANGE:` are for breaking changes. ie: Adding a new linting rule may break
  projects, so this is a new major.
* `feat:` is for new features (minor).
* `fix:` is for bug fixes (patch).

There is nothing to publish manually, and no npm token is involved: authentication uses
[npm trusted publishing](https://docs.npmjs.com/trusted-publishers/) through GitHub Actions OIDC.

### Adding a new package

A trusted publisher can only be configured on a package that **already exists** on the registry, so
the first release of a brand new package cannot be published by the workflow. It fails with
`YN0033: No authentication configured for request`, because Yarn reports the failed OIDC token
exchange as a missing authentication.

To bootstrap a new package:

1. Make sure `repository.url` in its `package.json` exactly matches
   `https://github.com/kronostechnologies/standards.git`. npm rejects OIDC publishes otherwise.
1. Register it in [`release-please-config.json`](../release-please-config.json) and
   [`.release-please-manifest.json`](../.release-please-manifest.json).
1. Publish the first version manually, from an up to date `master`, using a
   [granular access token](https://docs.npmjs.com/creating-and-viewing-access-tokens) with publish
   rights on the `@equisoft` scope:

   ```shell
   cd javascript
   yarn install --immutable
   yarn workspaces foreach -Atv --include @equisoft/<package> run build
   YARN_NPM_AUTH_TOKEN=<token> yarn workspaces foreach -Atv --include @equisoft/<package> \
       npm publish --access public --tolerate-republish
   ```

1. On npmjs.com, open the package settings and add a **Trusted Publisher**:

   | Field | Value |
   | --- | --- |
   | Provider | GitHub Actions |
   | Organization / repository | `kronostechnologies/standards` |
   | Workflow filename | `release-please.yml` |
   | Environment | `release` |

1. Re-run the failed `publish` job. `--tolerate-republish` makes it idempotent.

Subsequent releases of that package are handled by the workflow like any other.
