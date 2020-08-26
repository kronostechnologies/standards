module.exports = {
    extends: [
        '@equisoft/eslint-config',
        'plugin:@typescript-eslint/recommended',
    ],
    parser: '@typescript-eslint/parser',
    settings: {
        'import/parsers': {
            '@typescript-eslint/parser': ['.ts', '.tsx'],
        },
    },
    rules: {
        'no-unused-vars': 'off',
        '@typescript-eslint/no-unused-vars': ['error', { 'argsIgnorePattern': '^_$' }],

        'no-useless-constructor': 'off',
        '@typescript-eslint/no-useless-constructor': 'error',

        'no-unused-expressions': 'off',
        '@typescript-eslint/no-unused-expressions': 'error',

        '@typescript-eslint/explicit-function-return-type': 'off', // See overrides
        '@typescript-eslint/ban-types': 'off',
        '@typescript-eslint/ban-ts-comment': ['error', {
            'ts-ignore': 'allow-with-description',
        }],

        // Useless in TypeScript
        'consistent-return': 'off',
        'default-case': 'off',
    },
    overrides: [
        {
            'files': ['*.ts', '*.tsx'],
            'rules': {
                '@typescript-eslint/explicit-function-return-type': ['error',
                    {
                        allowExpressions: true,
                        allowTypedFunctionExpressions: true,
                        allowHigherOrderFunctions: true,
                    },
                ],
            },
        },
    ],
};
