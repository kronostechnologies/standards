const { defineStylistic, rules: stylisticRules } = require('@stylistic/stylelint-config');

const COMMON_STYLELINT_RULES = {
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
};

const COMMON_STYLISTIC_RULES = {
    'indentation': 4,
    'no-extra-semicolons': null,
};

const JS_STYLELINT_RULES = {
    'no-empty-source': null,
    'property-no-vendor-prefix': true,
    'value-no-vendor-prefix': true,
};

const JS_STYLISTIC_RULES = {
    // https://github.com/hudochenkov/postcss-styled-syntax/issues/2
    'indentation': null,

    'max-line-length': null,
    'no-empty-first-line': null,
    'no-missing-end-of-source-newline': null,
    'no-eol-whitespace': [
        true,
        {
            ignore: ['empty-lines'],
        },
    ],
    'string-quotes': 'single',
};

module.exports = {
    extends: [
        '@stylistic/stylelint-config',
    ],
    plugins: ['stylelint-order'],
    rules: {
        ...COMMON_STYLELINT_RULES,

        ...defineStylistic({
            rules: COMMON_STYLISTIC_RULES,
        }),
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
                ...JS_STYLELINT_RULES,

                ...defineStylistic({
                    rules: JS_STYLISTIC_RULES,
                }),

                ...defineStylistic({
                    syntax: 'styled',
                    rules: {
                        ...stylisticRules,
                        ...COMMON_STYLISTIC_RULES,
                        ...JS_STYLISTIC_RULES,
                    },
                }),
            },
        },
    ],
};
