# Equisoft's ESLint configuration

This projects hosts Equisoft's [ESLint](https://eslint.org/) configuration. It is versatile enough to be used with NodeJS or Web projects.

When using frameworks or libraries, you can augment it with the use of flavors, such as [@equisoft/eslint-config-react](https://www.npmjs.com/package/@equisoft/eslint-config-react).

## Versioning

The versioning of this project respects [semver](https://semver.org/). That means your project's package.json can caret (`^`) import it.

## Installation

Install the libraries in your project:

```bash
yarn add --dev @equisoft/eslint-config
```

Then create a _.eslintrc_ file that uses Equisoft's configuration:

```json
{
  "extends": ["@equisoft/eslint-config"]
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

Now you can use `yarn eslint` to validate the code style of your TypeScript files!

## Continuous Integration
We strongly suggest that you enforce code style checks on your CI. For example, on CircleCI you can add a configuration similar to this one to your _.circleci/config.yml_:

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

ESLint supports exclusion of globs in _.eslintignore_. One way to gradually migrate a legacy code base to this configuration is to exclude all source files and add conforming files one at a time. To achieve this, add this to your _.eslintignore_:

```
src/**/*.js
!src/contacts/**/*.js
```

Note how an entry prefixed by `!` is added to re-include some source files.

For more information on this technique, you can read the [ESLint documentation](https://eslint.org/docs/user-guide/configuring#eslintignore).
