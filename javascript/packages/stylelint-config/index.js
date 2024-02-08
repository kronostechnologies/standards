module.exports = {
    extends: [
        '@stylistic/stylelint-config',
    ],
    plugins: ['stylelint-order'],
    rules: {
        'at-rule-no-unknown': [
            true,
            {
                ignoreAtRules: ['extend', 'each', 'else', 'for', 'if', 'include'],
            },
        ],
        'font-weight-notation': 'numeric',

        '@stylistic/indentation': 4,

        'order/order': [
            'custom-properties',
            'declarations',
        ],
        'order/properties-alphabetical-order': true,

        // https://github.com/stylelint/stylelint/issues/4953
        // https://github.com/stylelint/stylelint/pull/4944
        'function-name-case': null,
        '@stylistic/no-extra-semicolons': null,
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
            rules: {
                'scss/load-no-partial-leading-underscore': null,
            }
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
                // https://github.com/hudochenkov/postcss-styled-syntax/issues/2
                '@stylistic/indentation': null,
                '@stylistic/no-eol-whitespace': [
                    true,
                    {
                        ignore: ['empty-lines'],
                    },
                ],
                '@stylistic/max-line-length': null,

                // stylelint-config-styled-components
                '@stylistic/no-empty-first-line': null,
                '@stylistic/string-quotes': 'single',
                'no-empty-source': null,
                '@stylistic/no-missing-end-of-source-newline': null,
                'property-no-vendor-prefix': true,
                'value-no-vendor-prefix': true,
            },
        },
    ],
};
