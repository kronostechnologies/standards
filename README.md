# Kronos Technologies Standards

## Supported languages

### PHP

Located in [php/php-coding-standard](php/php-coding-standard).

Supports Squizlab's [PHP CodeSniffer](https://github.com/squizlabs/PHP_CodeSniffer).


### JavaScript (NodeJS)

For use in a NodeJS environment without TypeScript. Located under [node/eslint-config-kt](node/eslint-config-kt).

Applied through [ESLint](https://eslint.org/).

#### React flavor

Use the configuration located under [react/eslint-config-react-kt](react/eslint-config-react-kt) instead.


### TypeScript (NodeJS + Web)

For use in a NodeJS or Web environment with TypeScript. Located under [node/tslint-config](node/tslint-config).

Applied through [TSLint](https://palantir.github.io/tslint/).


## Building & Publishing

### PHP

Apply your changes, merge in `master` branch, then execute the following commands from the root of the directory:

```bash
# Mirrors php-coding-standard repository in kronostechnologies/php-coding-standard
bin/split.sh

# Push tag to kronostechnologies/php-coding-standard
bin/release.sh v(MAJOR.MINOR.PATCH)
```

To get the latest tag without opening the php-coding-standard repository, checkout the file [.tag](php/php-coding-standard/.tag).

Prefer updating minor versions. 

Afterwards, it is automatically published through packagist.


### [NPM](npmjs.org)

Make sure you are starting from an up to date master branch.

1. Create a new branch: `git checkout -b release/1.2.3`
1. Navigate to the directory of the project you wish to publish
1. Update the CHANGELOG.md file to reflect the changes to be published
1. If the project has extra steps to run before publication (ie: test, compile, etc.), run them
1. Configure your username and email: `yarn login`
1. Publish: `yarn publish`.

   When asked for the new version, respect [semver](https://semver.org/):
      * Majors are for breaking changes. ie: Adding a new linting rule may break projects, so this is a new major.
      * Minors are for new features.
      * Patches are for bug fixes.
   
   Yarn will automatically create a commit for this new version and tag it appropriately.

   If you have 2FA enabled on your account, you must use Yarn >=1.12.
1. Push your branch and tag: `git push origin HEAD; git push origin tslint-config-1.2.3`
1. Open a Pull Request on GitHub: `https://github.com/kronostechnologies/standards/compare/master...release/1.2.3`
