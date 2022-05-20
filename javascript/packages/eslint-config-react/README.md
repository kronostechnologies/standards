# Equisoft's ESLint configuration

This project hosts Equisoft's [ESLint](https://eslint.org/) configuration for React projects.

The rules of this ESLint configuration uses [AirBnB](https://github.com/airbnb/javascript/tree/master/packages/eslint-config-airbnb)'s packages, with some small overrides to make up Equisoft's flavor.

For Node projects, you should instead look at [@equisoft/eslint-config](https://www.npmjs.com/package/@equisoft/eslint-config) which does not include React rules.

## Versioning

The versioning of this project respects [semver](https://semver.org/). That means your project's package.json can caret (`^`) import it.

## Installation

Install the [prerequisites](../eslint-config/README.md#prerequisities).

Install the libraries in your project:

```bash
yarn add --dev @equisoft/eslint-config-react eslint-plugin-jsx-a11y eslint-plugin-react eslint-plugin-react-hooks  
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
    "eslint:ci": "yarn eslint"
  }
}
```

Now you can use `yarn eslint` to validate the code style of your Javascript files!

## Continuous Integration
See [here](../eslint-config/README.md#continuous-integration).

## Migrating an existing codebase
See [here](../eslint-config/README.md#migrating-an-existing-codebase).
