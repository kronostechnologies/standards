module.exports = {
    extends: [
        '@equisoft/eslint-config',
        'eslint-config-airbnb/rules/react',
        'eslint-config-airbnb/rules/react-hooks',
    ].map(require.resolve),
    parserOptions: {
        ecmaVersion: 2018,
        sourceType: 'module',
    },
    rules: {
        'import/no-extraneous-dependencies': 'off',
        'no-underscore-dangle': [ 'error', { 'allowAfterThis': true } ],
        'react/destructuring-assignment': 'off',
        'react/jsx-indent': [ 'error', 4 ],
        'react/jsx-indent-props': [ 'error', 4 ],
        'react/jsx-tag-spacing': ['error', {
            closingSlash: 'never',
            beforeSelfClosing: 'never',
            afterOpening: 'never',
            beforeClosing: 'never',
        }],
        'react/prop-types': 'off',
    },
    overrides: [
        { files: [ '*.js', '*.jsx' ] },
    ],
};