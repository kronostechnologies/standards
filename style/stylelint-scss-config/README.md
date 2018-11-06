# Equisoft's stylelint SASS configuration

This project hosts Equisoft's [stylelint](https://stylelint.io/) configuration for SASS.

## Versioning

The versioning of this project respects [semver](https://semver.org/). That means your project's package.json can caret (`^`) import it.

## Installation

Install the libraries in your project:

```bash
yarn add --dev @equisoft/stylelint-scss-config stylelint
```

Then create a _.stylelintrc_ file that uses Equisoft's configuration:

```json
{
  "extends": ["@equisoft/stylelint-scss-config"]
}
```

Finally create a script in your _package.json_ to easily run stylelint:

```json
{
  "scripts": {
    "stylelint": "stylelint 'src/**/*.{css,scss}'",
    "stylelint:ci": "yarn stylelint"
  }
}
```

Now you can use `yarn stylelint` to validate the code style of your CSS files!

## Continuous Integration
We strongly suggest that you enforce code style checks on your CI. For example, on CircleCI you can add a configuration similar to this one to your _.circleci/config.yml_:

```yaml
stylelint:
  executor: 'node'
  steps:
    - run: 'yarn stylelint:ci'
```

## Migrating an existing codebase

Stylelint supports exclusion of globs in _.stylelintignore_. One way to gradually migrate a legacy code base to this configuration is to exclude all source files and add conforming files one at a time. To achieve this, add this key to your ".stylelintignore" file:

```
src/**/*.scss
!src/contacts/**/*.scss
```

Note how an entry prefixed by `!` is added to re-include some source files.

For more information on this technique, you can read the [stylelint documentation](https://github.com/stylelint/stylelint/blob/master/docs/user-guide/configuration.md#stylelintignore).
