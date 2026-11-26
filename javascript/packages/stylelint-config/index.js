const { defineStylistic, rules: stylisticRules } = require('@stylistic/stylelint-config');

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

        'order/order': [
            'custom-properties',
            'declarations',
        ],
        'order/properties-alphabetical-order': true,

        // https://github.com/stylelint/stylelint/issues/4953
        // https://github.com/stylelint/stylelint/pull/4944
        'function-name-case': null,
        'value-keyword-case': null,

        '@stylistic/indentation': 4,
        '@stylistic/no-extra-semicolons': null,
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
                'no-empty-source': null,
                'property-no-vendor-prefix': true,
                'value-no-vendor-prefix': true,

                // Use the styled-components version of each base rule from stylistic
                ...defineStylistic({
                    syntax: 'styled',
                    rules: stylisticRules,
                }),

                // Note: null overrides must target each syntax in which they were declared.

                // https://github.com/hudochenkov/postcss-styled-syntax/issues/2
                '@stylistic/indentation': null,
                '@stylistic/styled/indentation': null,

                '@stylistic/max-line-length': null,
                '@stylistic/styled/max-line-length': null,

                '@stylistic/no-empty-first-line': null,
                '@stylistic/styled/no-empty-first-line': null,

                '@stylistic/no-extra-semicolons': null,
                '@stylistic/styled/no-extra-semicolons': null,

                '@stylistic/no-missing-end-of-source-newline': null,
                '@stylistic/styled/no-missing-end-of-source-newline': null,

                '@stylistic/no-eol-whitespace': [
                    true,
                    {
                        ignore: ['empty-lines'],
                    },
                ],
                '@stylistic/string-quotes': 'single',
            },
        },
    ],
};
