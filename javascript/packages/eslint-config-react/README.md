# Equisoft's ESLint configuration

This project hosts Equisoft's [ESLint](https://eslint.org/) configuration for React projects.

The rules of this ESLint configuration uses [AirBnB](https://github.com/airbnb/javascript/tree/master/packages/eslint-config-airbnb)'s packages, with some small overrides to make up Equisoft's flavor.

For Node projects, you should instead look at [@equisoft/eslint-config](https://www.npmjs.com/package/@equisoft/eslint-config) which does not include React rules.

## Versioning

The versioning of this project respects [semver](https://semver.org/). That means your project's package.json can caret (`^`) import it.

## Installation

Install the libraries in your project:

```bash
yarn add --dev @equisoft/eslint-config-react eslint
```

Then create a _.eslintrc.json_ file that uses Equisoft's configuration:

```json
{
  "extends": ["@equisoft/eslint-config-react"]
}
```

Finally create a script in your _package.json_ to easily run ESLint:

```json
{
  "scripts": {
    "eslint": "eslint src",
    "eslint:ci": "yarn eslint --format junit -o build/tests/eslint/junit.xml"
  }
}
```

Now you can use `yarn eslint` to validate the code style of your Javascript files!

## Continuous Integration
We strongly suggest that you enforce code style checks on your CI. For example, on CircleCI you can add a configuration similar to this one to your _.circleci/config.yml_:

```yaml
orbs:
  eq: equisoft/build-tools@latest

jobs:
  eslint:
    executor: node
    steps:
      - eq/with-yarn-cache
      - run:
          name: ESLint
          command: yarn eslint:ci
      - store_test_results:
          path: build/tests
```

## Migrating an existing codebase

ESLint supports exclusion of globs in _.eslintignore_. One way to gradually migrate a legacy code base to this configuration is to exclude all source files and add conforming files one at a time. To achieve this, add this to your _.eslintignore_:

```
src/**/*.js
!src/contacts/**/*.js
```

Note how an entry prefixed by `!` is added to re-include some source files.

For more information on this technique, you can read the [ESLint documentation](https://eslint.org/docs/user-guide/configuring#eslintignore).
