module.exports = {
    extends: [
        'stylelint-stylistic/config',
    ],
    plugins: ['stylelint-order', 'stylelint-stylistic'],
    rules: {
        'at-rule-no-unknown': [
            true,
            {
                ignoreAtRules: ['extend', 'each', 'else', 'for', 'if', 'include'],
            },
        ],
        'font-weight-notation': 'numeric',

        'stylistic/indentation': 4,

        'order/order': [
            'custom-properties',
            'declarations',
        ],
        'order/properties-alphabetical-order': true,

        // https://github.com/stylelint/stylelint/issues/4953
        // https://github.com/stylelint/stylelint/pull/4944
        'function-name-case': null,
        'stylistic/no-extra-semicolons': null,
        'value-keyword-case': null,
    },
    overrides: [
        {
            files: [
                '**/*.css',
                '**/*.scss',
                '**/*.sass',
                '**/*.less',
            ],
            extends: ['stylelint-config-standard-scss'],
        },
        {
            files: [
                '**/*.cjs',
                '**/*.mjs',
                '**/*.mts',
                '**/*.mtsx',
                '**/*.ts',
                '**/*.tsx',
                '**/*.js',
                '**/*.jsx',
            ],
            customSyntax: 'postcss-styled-syntax',
            rules: {
                'stylistic/indentation': null,
                'stylistic/max-line-length': null,

                // stylelint-config-styled-components
                'no-empty-first-line': null,
                'no-empty-source': null,
                'no-missing-end-of-source-newline': null,
                'property-no-vendor-prefix': true,
                'value-no-vendor-prefix': true,
            },
        },
    ],
};
