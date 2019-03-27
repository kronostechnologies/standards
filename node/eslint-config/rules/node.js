module.exports = {
  rules: {
    "import/extensions": "off",
    "indent": ["error", 4, {"SwitchCase": 1}],
    "max-len": [2, 220, 2, {"ignoreUrls": true, "ignoreComments": false}],
    "no-mixed-operators": ["error", {"allowSamePrecedence": true}],
    "no-param-reassign": "warn",
    "no-plusplus": ["error", {"allowForLoopAfterthoughts": true}],
    "no-cond-assign": ["error", "except-parens"],
    "no-return-assign": ["error", "except-parens"],
    "no-undef": "warn",
    "object-shorthand": "warn",
    "prefer-destructuring": "off",
  }
};
