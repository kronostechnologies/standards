module.exports = {
    extends: [
        '@equisoft/eslint-config-react',
        '@equisoft/eslint-config-typescript',
    ],

    rules: {
        'react/jsx-filename-extension': ['error', { extensions: ['.jsx', '.tsx'] }],
    },
};
