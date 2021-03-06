# Equisoft's ESLint configuration

This project hosts Equisoft's [ESLint](https://eslint.org/) configuration for TypeScript. 
It is versatile enough to be used with NodeJS or Web projects.

## Versioning

The versioning of this project respects [semver](https://semver.org/). 
That means your project's package.json can caret (`^`) import it.

## Installation

Install the libraries in your project:

```bash
yarn add --dev @equisoft/eslint-config-typescript-react eslint
```

Then create a `.eslintrc.json` file that uses Equisoft's configuration:

```json
{
  "extends": ["@equisoft/eslint-config-typescript-react"]
}
```

Finally create a script in your `package.json` to easily run ESLint:

```json
{
  "scripts": {
    "eslint": "eslint . --ext .js,.jsx,.ts,.tsx",
    "eslint:ci": "yarn eslint --format junit -o build/tests/eslint/junit.xml"
  }
}
```

Now you can use `yarn eslint` to validate the code style of your TypeScript files!

## Continuous Integration
We strongly suggest that you enforce code style checks on your CI. 
For example, on CircleCI you can add a configuration similar to this one to your `.circleci/config.yml`:

```yaml
eslint:
  executor: 'node'
  steps:
    - run: 'mkdir -p build/tests/eslint/'
    - run: 'yarn eslint:ci'
    - store_test_results:
        path: 'build/tests'
```

## Migrating an existing codebase

ESLint supports exclusion of globs in `.eslintignore`. One way to gradually migrate a legacy code base to this 
configuration is to exclude all source files and add conforming files one at a time. 
To achieve this, add this to your `.eslintignore`:

```
src/**/*.js
!src/contacts/**/*.js
```

Note how an entry prefixed by `!` is added to re-include some source files.

For more information on this technique, you can read 
the [ESLint documentation](https://eslint.org/docs/user-guide/configuring#eslintignore).
