module.exports = {
  plugins: [
    "react",
  ],

  parserOptions: {
    ecmaFeatures: {
      jsx: true,
    },
  },

  rules: {
    "no-underscore-dangle": ["error", { "allowAfterThis": true }],
    "import/no-extraneous-dependencies": "off",
    "react/prop-types": "off",
  }
};
