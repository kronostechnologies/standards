module.exports = {
  extends: [
    '../../node/eslint-config',
    './airbnb-rules/react',
    './rules/react',
  ].map(require.resolve),
  parserOptions: {
    ecmaVersion: 2018,
    sourceType: 'module',
  },
};
