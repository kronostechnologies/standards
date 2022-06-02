module.exports = {
    extends: [
        'stylelint-config-standard-scss',
    ],
    plugins: ['stylelint-order'],
    processors: ["stylelint-processor-styled-components"],
    rules: {
        'at-rule-no-unknown': [
            true,
            {
                ignoreAtRules: ['extend', 'each', 'else', 'for', 'if', 'include'],
            },
        ],
        'font-weight-notation': 'numeric',
        indentation: 4,
        'order/order': [
            'custom-properties',
            'declarations',
        ],
        'order/properties-alphabetical-order': true,

        // stylelint-config-styled-components
        'no-empty-first-line': null,
        'no-empty-source': null,
        'no-missing-end-of-source-newline': null,
        'property-no-vendor-prefix': true,
        'value-no-vendor-prefix': true,

        // https://github.com/stylelint/stylelint/issues/4953
        // https://github.com/stylelint/stylelint/pull/4944
        'function-name-case': null,
        'no-extra-semicolons': null,
        'value-keyword-case': null,
    },
};
