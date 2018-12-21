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
    'react/jsx-indent': ['error', 4],
    "no-underscore-dangle": ["error", { "allowAfterThis": true }],
    "import/no-extraneous-dependencies": "off",
    "react/prop-types": "off",
  }
};
