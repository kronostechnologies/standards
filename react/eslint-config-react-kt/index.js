module.exports = {
  extends: [
    '../../node/eslint-config-kt',
    './rules/react',
    './rules/react-a11y',
    './rules/react-kronos',
  ].map(require.resolve),
  parserOptions: {
    ecmaVersion: 2018,
    sourceType: 'module',
  },
};
