module.exports = {
  extends: [
    '@equisoft/eslint-config',
    './airbnb-rules/react',
    './rules/react',
  ].map(require.resolve),
  parserOptions: {
    ecmaVersion: 2018,
    sourceType: 'module',
  },
};
