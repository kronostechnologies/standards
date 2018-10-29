module.exports = {
  extends: [
    '../../node/eslint-config-kt',
    './airbnb-rules/react',
    './airbnb-rules/react-a11y',
    './rules/react',
  ].map(require.resolve),
  parserOptions: {
    ecmaVersion: 2018,
    sourceType: 'module',
  },
};
