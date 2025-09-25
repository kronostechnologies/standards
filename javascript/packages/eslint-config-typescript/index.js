module.exports = {
    extends: [
        '@equisoft/eslint-config',
        'plugin:@typescript-eslint/recommended',
        'plugin:import/typescript',
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

        'no-use-before-define': 'off',
        '@typescript-eslint/no-use-before-define': ['error',
            {
                'classes': true,
                'enums': true,
                'functions': true,
                'ignoreTypeReferences': false,
                'typedefs': true,
                'variables': true,
            },
        ],

        'no-useless-constructor': 'off',
        '@typescript-eslint/no-useless-constructor': 'error',

        'no-empty-function': 'off',
        '@typescript-eslint/no-empty-function': 'error',

        '@typescript-eslint/no-empty-object-type': [
            'error',
            { 'allowInterfaces': 'always', 'allowObjectTypes': 'always' },
        ],

        'no-unused-expressions': 'off',
        '@typescript-eslint/no-unused-expressions': 'error',

        'space-infix-ops': 'off',
        '@stylistic/space-infix-ops': ['error'],

        '@typescript-eslint/explicit-function-return-type': 'off', // See overrides
        '@typescript-eslint/ban-types': 'off',
        '@typescript-eslint/ban-ts-comment': ['error',
            {
                'ts-ignore': 'allow-with-description',
            },
        ],

        'no-shadow': 'off',
        '@typescript-eslint/no-shadow': ['error'],

        // Useless in TypeScript
        'consistent-return': 'off',
        'default-case': 'off',
    },
    overrides: [
        {
            files: ['*.spec.ts', '*.spec.tsx', '*.test.ts', '*.test.tsx'],
            rules: {
                '@typescript-eslint/no-empty-function': 'off',
            }
        },
        {
            files: ['*.ts', '*.tsx'],
            rules: {
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
