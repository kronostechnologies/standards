# JavaScript and TypeScript

## Building & Publishing

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
