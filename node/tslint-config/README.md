# Equisoft's TSLint configuration

This project hosts Equisoft's [TSLint](https://palantir.github.io/tslint/) configuration. It is versatile enough to be used with NodeJS or Web projects.

For react, you should instead look at [@equisoft/tslint-config-react](https://www.npmjs.com/package/@equisoft/tslint-config-react) which comes with additional validations.

## Versioning

The versioning of this project respects [semver](https://semver.org/). That means your project's package.json can caret (`^`) import it.

## Installation

Install the library in your project:

```bash
yarn add --dev @equisoft/tslint-config
```

Then create a _tslint.json_ file that uses Equisoft's configuration:

```json
{
  "extends": ["@equisoft/tslint-config"]
}
```

Finally create a script in your _package.json_ to easily run TSLint:

```json
{
  "scripts": {
    "tslint": "tslint -p tsconfig.json 'src/**/*.ts'",
    "tslint:ci": "yarn tslint --format junit -o build/tests/tslint/junit.xml"
  }
}
```

Now you can use `yarn tslint` to validate the code style of your TypeScript files!

## TypeScript compiler checks
This repository additionally exports a `tsconfig.standards.json` file that you should extend in your _tsconfig.json_ configuration:

```json
{
  "extends": "./node_modules/@equisoft/tslint-config/tsconfig.standards.json"
}
```

## Continuous Integration
We strongly suggest that you enforce code style checks on your CI. For example, on CircleCI you can add a configuration similar to this one to your _.circleci/config.yml_:

```yaml
orbs:
  eq: equisoft/build-tools@latest

jobs:
  tslint:
    executor: node
    steps:
      - eq/with-yarn-cache
      - run:
          name: TSLint
          command: yarn tslint:ci
      - store_test_results:
          path: build/tests
```

## Migrating an existing codebase

TSLint supports exclusion of globs in _tsconfig.json_. One way to gradually migrate a legacy code base to this configuration is to exclude all source files and add conforming files one at a time. To achieve this, add this key to your tsconfig.json:

```json
{
  "linterOptions": {
    "ecxlude": [
      "src/**/*.ts",
      "!src/contacts/**/*.ts"
    ]
  }
}
```

Note how an entry prefixed by `!` is added to re-include some source files.
