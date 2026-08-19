module.exports = {
    extends: ['@equisoft/eslint-config-typescript'],
    settings: {
        'import/resolver': {
            typescript: {
                project: [
                    './tsconfig.json',
                ],
            },
        },
    },
};
