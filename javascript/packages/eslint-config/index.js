module.exports = {
    extends: [
        'eslint-config-airbnb-base',
    ].map(require.resolve),
    parserOptions: {
        ecmaVersion: 2018,
        sourceType: 'module',
    },
    rules: {
        'arrow-parens': ['error', 'always'],
        'class-methods-use-this': 'off',
        'import/extensions': 'off',
        'import/prefer-default-export': 'off',
        'import/no-default-export': ['error'],
        'indent': ['error', 4, { 'SwitchCase': 1 }],
        'max-len': ['error', 120, 2,
            {
                ignoreUrls: true,
                ignoreComments: false,
                ignoreRegExpLiterals: true,
                ignoreStrings: true,
                ignoreTemplateLiterals: true,
            },
        ],
        'no-mixed-operators': ['error', { 'allowSamePrecedence': true }],
        'no-plusplus': ['error', { 'allowForLoopAfterthoughts': true }],
        'no-cond-assign': ['error', 'except-parens'],
        'no-return-assign': ['error', 'except-parens'],
        'prefer-destructuring': 'off',
    },
};
