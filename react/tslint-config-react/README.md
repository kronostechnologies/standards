# Equisoft's TSLint configuration

This project hosts Equisoft's [TSLint](https://palantir.github.io/tslint/) configuration for React project. It is aimed to be used alongside the base Equisoft's TSLint configuration, [@equisoft/tslint-config](https://www.npmjs.com/package/@equisoft/tslint-config).

## Versioning

The versioning of this project respects [semver](https://semver.org/). That means your project's package.json can caret (`^`) import it.

## Installation

Install the libraries in your project:

```bash
yarn add --dev @equisoft/tslint-config @equisoft/tslint-config-react tslint
```

Then create a _tslint.json_ file that uses Equisoft's configuration:

```json
{
  "extends": [
    "@equisoft/tslint-config",
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

## Additional steps

Read the [main configuration (@equisoft/tslint-config)](https://www.npmjs.com/package/@equisoft/tslint-config) to get more details on how to bootstrap your TypeScript project using Equisoft's strict set of rules.
