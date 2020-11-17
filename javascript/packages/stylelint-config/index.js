module.exports = {
    extends: [
        'stylelint-config-standard',
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
        indentation: 4,
        'order/order': [
            'custom-properties',
            'declarations',
        ],
        'order/properties-alphabetical-order': true,

        // stylelint-config-styled-components
        'no-empty-source': null,
        'no-missing-end-of-source-newline': null,
        'property-no-vendor-prefix': true,
        'value-no-vendor-prefix': true,
    },
};
