module.exports = {
    extends: [
        'eslint-config-airbnb-base',
    ],
    parserOptions: {
        ecmaVersion: 2018,
        sourceType: 'module',
    },
    plugins: [
        'eslint-plugin-import',
    ],
    rules: {
        'arrow-parens': ['error', 'always'],
        'class-methods-use-this': 'off',
        'import/extensions': 'off',
        'import/prefer-default-export': 'off',
        'import/no-default-export': ['error'],
        'indent': ['error', 4, { 'SwitchCase': 1 }],
        'lines-between-class-members': [
            'error', 'always', { exceptAfterSingleLine true }
        ],
        'max-len': [
            'error',
            {
                'code': 120,
                'tabWidth': 2,
                'ignoreComments': false,
                'ignoreRegExpLiterals': true,
                'ignoreStrings': false, // this otherwise includes imports which we want included
                'ignoreTemplateLiterals': true,
                'ignoreTrailingComments': false,
                'ignoreUrls': true,
            },
        ],
        'no-mixed-operators': ['error', { 'allowSamePrecedence': true }],
        'no-plusplus': ['error', { 'allowForLoopAfterthoughts': true }],
        'no-cond-assign': ['error', 'except-parens'],
        'no-console': ['error', { 'allow': ['info', 'warn', 'error', 'trace'] }],
        'no-return-assign': ['error', 'except-parens'],
        'object-curly-newline': [
            // No max-property
            'error', {
                'ObjectExpression': {
                    'minProperties': 4,
                    'multiline': true,
                    'consistent': true,
                },
                'ObjectPattern': {
                    'minProperties': 4,
                    'multiline': true,
                    'consistent': true,
                },
                'ImportDeclaration': {
                    'consistent': true,
                },
                'ExportDeclaration': {
                    'minProperties': 4,
                    'consistent': true,
                },
            },
        ],
        'prefer-destructuring': 'off',
    },
};
