# Equisoft's ESLint configuration

This project hosts Equisoft's [ESLint](https://eslint.org/) configuration for TypeScript. 
It is versatile enough to be used with NodeJS or Web projects.

When using frameworks or libraries, you can augment it with the use of flavors, 
such as [@equisoft/eslint-config-typescript-react](https://www.npmjs.com/package/@equisoft/eslint-config-react).

## Versioning

The versioning of this project respects [semver](https://semver.org/). 
That means your project's package.json can caret (`^`) import it.

## Installation

Install the [prerequisites](../eslint-config/README.md#prerequisities).

Install the libraries in your project:

```bash
yarn add --dev @equisoft/eslint-config-typescript @typescript-eslint/eslint-plugin @typescript-eslint/parser eslint-import-resolver-typescript
```

Then create a `.eslintrc.json` file that uses Equisoft's configuration:

```json
{
  "extends": ["@equisoft/eslint-config-typescript"]
}
```

Finally create a script in your `package.json` to easily run ESLint:

```json
{
  "scripts": {
    "eslint": "eslint src",
    "eslint:ci": "yarn eslint"
  }
}
```

Now you can use `yarn eslint` to validate the code style of your TypeScript files!

## Continuous Integration
See [here](../eslint-config/README.md#continuous-integration).

## Migrating an existing codebase
See [here](../eslint-config/README.md#migrating-an-existing-codebase).
