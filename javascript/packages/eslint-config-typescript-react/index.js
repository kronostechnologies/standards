module.exports = {
    extends: [
        '@equisoft/eslint-config-react',
        '@equisoft/eslint-config-typescript',
    ],

    rules: {
        'react/jsx-filename-extension': ['error', { extensions: ['.jsx', '.tsx'] }],
    },
    overrides: [
        {
            files: ['**/*.@(test|spec).tsx'],
            rules: {
                'react/jsx-props-no-spreading': 'off',
            },
        }
    ],
};
