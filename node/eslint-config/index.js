module.exports = {
    extends: [
        'eslint-config-airbnb-base',
    ].map(require.resolve),
    parserOptions: {
        ecmaVersion: 2018,
        sourceType: 'module',
    },
    rules: {
        'arrow-parens': [ 'error', 'always' ],
        'import/extensions': 'off',
        'indent': [ 'error', 4, { 'SwitchCase': 1 } ],
        'no-mixed-operators': [ 'error', { 'allowSamePrecedence': true } ],
        'no-plusplus': [ 'error', { 'allowForLoopAfterthoughts': true } ],
        'no-cond-assign': [ 'error', 'except-parens' ],
        'no-return-assign': [ 'error', 'except-parens' ],
        'prefer-destructuring': 'off',
    }
};
