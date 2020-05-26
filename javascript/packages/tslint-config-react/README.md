# Equisoft's TSLint configuration

This project hosts Equisoft's [TSLint](https://palantir.github.io/tslint/) configuration for React project.

For Node projects, you should instead look at [@equisoft/tslint-config](https://www.npmjs.com/package/@equisoft/tslint-config) which does not include React rules.

## Versioning

The versioning of this project respects [semver](https://semver.org/). That means your project's package.json can caret (`^`) import it.

## Installation

Install the libraries in your project:

```bash
yarn add --dev @equisoft/tslint-config-react
```

Then create a _tslint.json_ file that uses Equisoft's configuration:

```json
{
  "extends": [
    "@equisoft/tslint-config-react"
  ]
}
```

Finally create a script in your _package.json_ to easily run TSLint:

```json
{
  "scripts": {
    "tslint": "tslint -p tsconfig.json 'src/**/*.{ts,tsx}'",
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
