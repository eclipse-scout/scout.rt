module.exports = {
  extends: [
    // 'plugin:@typescript-eslint/recommended',
    '@eclipse-scout'
  ],
  plugins: ['@typescript-eslint'],
  parser: '@typescript-eslint/parser',
  rules: {
    '@typescript-eslint/ban-types': 'warn',
    '@typescript-eslint/no-inferrable-types': 'warn',
    '@typescript-eslint/ban-ts-comment': 'off'
  }
}
