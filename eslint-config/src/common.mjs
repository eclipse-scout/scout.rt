/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {defineConfig} from 'eslint/config';
import stylistic from '@stylistic/eslint-plugin';

export default defineConfig({
  plugins: {
    '@stylistic': stylistic
  },
  rules: {
    'accessor-pairs': 'warn',
    'array-callback-return': 'error',
    'guard-for-in': 'off',
    'no-alert': 'warn',
    'no-eval': 'error',
    'no-extra-bind': 'error',
    'no-extra-label': 'error',
    'no-implicit-coercion': 'off',
    'no-implied-eval': 'error',
    'no-invalid-this': 'off',
    'no-iterator': 'error',
    'no-caller': 'warn',
    'no-console': 'off',
    'no-labels': 'error',
    'no-undef': 'off',
    'no-throw-literal': 'off',
    'camelcase': ['error', {allow: ['^\\$', '_']}],
    'no-lone-blocks': 'error',
    'no-loop-func': 'error',
    'no-proto': 'error',
    'no-prototype-builtins': 'off',
    'no-return-assign': 'error',
    'no-return-await': 'off',
    'no-self-compare': 'error',
    'no-label-var': 'error',
    'no-shadow': 'off',
    'no-var': 'warn',
    'require-unicode-regexp': 'off',
    'no-sequences': 'error',
    'no-unmodified-loop-condition': 'error',
    'no-useless-return': 'error',
    'no-else-return': 'error',
    'arrow-parens': ['error', 'as-needed'],
    'no-duplicate-imports': 'error',
    'one-var': 'off',
    'prefer-arrow-callback': 'warn',
    'prefer-template': 'off',
    'no-lonely-if': 'off',
    'new-cap': ['error', {'capIsNewExceptions': ['Deferred', '$.Event'], 'capIsNewExceptionPattern': 'Model$'}],
    'no-multi-assign': 'error',
    'no-unused-vars': 'off',
    'require-jsdoc': 'off',
    'no-plusplus': 'off',
    'curly': ['error', 'all'],
    'eqeqeq': ['error', 'always', {'null': 'ignore'}],
    'valid-jsdoc': 'off',
    'prefer-const': 'off',
    'prefer-rest-params': 'warn',
    'prefer-spread': 'warn',
    'max-classes-per-file': 'off',
    '@stylistic/no-multi-spaces': 'warn',
    '@stylistic/func-call-spacing': 'off',
    '@stylistic/eol-last': ['warn', 'always'],
    '@stylistic/comma-spacing': ['warn', {'before': false, 'after': true}],
    '@stylistic/array-bracket-spacing': ['warn', 'never'],
    '@stylistic/brace-style': ['warn', '1tbs'],
    '@stylistic/computed-property-spacing': ['warn', 'never'],
    '@stylistic/max-len': ['warn', 240, 2, {ignoreUrls: true, ignorePattern: '^import .*'}],
    '@stylistic/semi': ['warn', 'always'],
    '@stylistic/quotes': ['warn', 'single'],
    '@stylistic/comma-dangle': ['warn', 'never'],
    '@stylistic/object-curly-spacing': ['warn', 'never'],
    '@stylistic/operator-linebreak': 'off',
    '@stylistic/arrow-spacing': 'warn',
    '@stylistic/padded-blocks': 'off',
    '@stylistic/template-curly-spacing': ['warn', 'never'],
    '@stylistic/linebreak-style': ['warn', 'unix'],
    '@stylistic/newline-per-chained-call': 'off',
    '@stylistic/no-trailing-spaces': 'warn',
    '@stylistic/space-before-function-paren': ['warn', {
      'anonymous': 'never',
      'named': 'never',
      'asyncArrow': 'always'
    }],
    '@stylistic/indent': ['warn', 2, {'SwitchCase': 1}],
    '@stylistic/no-extra-parens': ['off'],
    '@stylistic/spaced-comment': ['warn', 'always', {'exceptions': ['*']}]
  }
});
