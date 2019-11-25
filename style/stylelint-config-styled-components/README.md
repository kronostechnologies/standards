# @equisoft/stylelint-config-styled-components

This is [Equisoft](https://equisoft.com)'s [stylelint](https://stylelint.io) config for use in projects that rely on [styled-components](https://www.styled-components.com).

Because this package comes with its own [stylelint](https://stylelint.io) dependency, your project only needs this package. It doesn't need to have [stylelint](https://stylelint.io) as a dependency.

## Add to your project
```
yarn add --dev @equisoft/stylelint-config-styled-components
```
or
```
npm install --save-dev @equisoft/stylelint-config-styled-components
```

## Usage
Set up your project's `.stylelintrc` file as follows:
```
// .stylelintrc
{
  "extends": ["@equisoft/stylelint-config-styled-components"]
}
```
