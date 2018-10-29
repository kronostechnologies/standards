module.exports = {
  extends: [
    './airbnb-rules/best-practices',
    './airbnb-rules/errors',
    './airbnb-rules/node',
    './airbnb-rules/style',
    './airbnb-rules/variables',
    './airbnb-rules/es6',
    './airbnb-rules/imports',
    './rules/node',
  ].map(require.resolve),
  parserOptions: {
    ecmaVersion: 2017,
    sourceType: 'module',
  },
};
