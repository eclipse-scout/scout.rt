module.exports = {
  extends: '@eclipse-scout',
  plugins: ['@babel'],
  parser: '@babel/eslint-parser',
  parserOptions: {
    requireConfigFile: false
  },
  rules: {
    'linebreak-style': 'off'
  }
};
