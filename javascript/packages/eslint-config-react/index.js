module.exports = {
    extends: [
        '@equisoft/eslint-config',
        'eslint-config-airbnb/rules/react',
        'eslint-config-airbnb/rules/react-hooks',
    ],
    parserOptions: {
        ecmaVersion: 2018,
        sourceType: 'module',
    },
    plugins: [
        'eslint-plugin-jsx-a11y',
        'eslint-plugin-react',
        'eslint-plugin-react-hooks',
    ],
    rules: {
        'import/no-extraneous-dependencies': 'off',
        'jsx-quotes': ['error', 'prefer-double'],
        'no-underscore-dangle': ['error', { 'allowAfterThis': true }],
        'react/default-props-match-prop-types': ['error', { allowRequiredDefaults: true }],
        'react/destructuring-assignment': 'off',
        'react/function-component-definition': ['error', { 'namedComponents': 'arrow-function' }],
        'react/jsx-indent': ['error', 4],
        'react/jsx-indent-props': ['error', 4],
        'react/jsx-tag-spacing': ['error', {
            closingSlash: 'never',
            beforeSelfClosing: 'always',
            afterOpening: 'never',
            beforeClosing: 'never',
        },
        ],
        'react/prop-types': 'off',
        'react/require-default-props': 'off',
    },
    overrides: [
        { files: ['*.js', '*.jsx'] },
        {
            files: ['**/*.@(test|spec).jsx'],
            rules: {
                'react/jsx-props-no-spreading': 'off',
            },
        }
    ],
};
