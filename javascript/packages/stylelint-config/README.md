# @equisoft/stylelint-config

This is [Equisoft](https://equisoft.com)'s [stylelint](https://stylelint.io) config. This can be used with projects that rely on [styled-components](https://www.styled-components.com) as stylelint now supports css-in-js syntax.

## Versioning

The versioning of this project respects [semver](https://semver.org/). That means your project's package.json can caret (`^`) import it.

## Installation

Install the libraries in your project:

```bash
yarn add --dev stylelint stylelint-order @equisoft/stylelint-config postcss postcss-styled-syntax
```

Then create a _.stylelintrc_ file that uses Equisoft's configuration:

```json
{
  "extends": ["@equisoft/stylelint-config"]
}
```

Finally create a script in your _package.json_ to easily run stylelint:

```json
{
  "scripts": {
    "stylelint": "stylelint --config .stylelintrc --custom-formatter @equisoft/stylelint-config/formatter 'src/**/*.{css,scss,js,jsx,ts,tsx}'",
    "stylelint:ci": "yarn stylelint"
  }
}
```

Now you can use `yarn stylelint` to validate the code style of your files!

## Continuous Integration
We strongly suggest that you enforce code style checks on your CI. For example, on Github Actions you can add a configuration similar to this one to your workflow:

```yaml
stylelint:
  name: Stylelint
  runs-on: ubuntu-latest
  steps:
      - name: Checkout
        uses: actions/checkout@v3

      - name: Setup asdf-vm
        uses: equisoft-actions/with-asdf-vm@v1

      - name: Install NPM dependencies
        uses: equisoft-actions/yarn-install@v1

      - name: Run Stylelint
        uses: equisoft-actions/yarn-stylelint@v1
```

## Migrating an existing codebase

Stylelint supports exclusion of globs in _.stylelintignore_. One way to gradually migrate a legacy code base to this configuration is to exclude all source files and add conforming files one at a time. To achieve this, add this key to your ".stylelintignore" file:

```
src/**/*.scss
!src/contacts/**/*.scss
```

Note how an entry prefixed by `!` is added to re-include some source files.

For more information on this technique, you can read the [stylelint documentation](https://stylelint.io/user-guide/ignore-code#files-entirely).
