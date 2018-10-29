module.export = {
  rules: {
    "import/extensions": "off",
    "indent": ["error", "tab", {"SwitchCase": 1}],
    "jsx-a11y/label-has-for": "off",
    "max-len": [2, 220, 2, {"ignoreUrls": true, "ignoreComments": false}],
    "no-mixed-operators": ["error", {"allowSamePrecedence": true}],
    "no-param-reassign": "warn",
    "no-plusplus": ["error", {"allowForLoopAfterthoughts": true}],
    "no-cond-assign": ["error", "except-parens"],
    "no-return-assign": ["error", "except-parens"],
    "no-undef": "warn",
    "object-shorthand": "warn",
  }
};
