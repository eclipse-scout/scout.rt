/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
'use strict';

module.exports = {
  env: {
    es6: true,
    browser: true,
    jquery: true,
    jasmine: true
  },
  extends: ['eslint:recommended', 'google'],
  parserOptions: {
    ecmaVersion: 6,
    sourceType: 'module'
  },
  rules: {
    'indent': ['error', 2, {'SwitchCase': 1}],
    'no-extra-parens': ['off'],
    'accessor-pairs': 'warn',
    'array-callback-return': 'error',
    'guard-for-in': 'off',
    'max-classes-per-file': ['error', 1],
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
    'no-multi-spaces': 'error',
    'no-proto': 'error',
    'no-prototype-builtins': 'off',
    'no-return-assign': 'error',
    'eslint func-call-spacing': 'off',
    'no-return-await': 'error',
    'no-self-compare': 'error',
    'no-label-var': 'error',
    'no-shadow': 'off',
    'no-var': 'warn',
    'eol-last': ['error', 'always'],
    'comma-spacing': ['error', {'before': false, 'after': true}],
    'array-bracket-spacing': ['error', 'never'],
    'brace-style': ['error', '1tbs'],
    'require-unicode-regexp': 'off',
    'no-sequences': 'error',
    'no-unmodified-loop-condition': 'error',
    'computed-property-spacing': ['error', 'never'],
    'no-useless-return': 'error',
    'no-else-return': 'error',
    'max-len': ['error', 240, 2, {ignoreUrls: true, ignorePattern: '^import .*'}],
    'semi': ['error', 'always'],
    'quotes': ['error', 'single'],
    'comma-dangle': ['error', 'never'],
    'object-curly-spacing': ['error', 'never'],
    'operator-linebreak': 'off',
    'arrow-parens': ['error', 'as-needed'],
    'arrow-spacing': 'error',
    'no-duplicate-imports': 'error',
    'one-var': 'off',
    'padded-blocks': 'off',
    'prefer-arrow-callback': 'warn',
    'prefer-const': 'off',
    'prefer-rest-params': 'warn',
    'prefer-spread': 'warn',
    'prefer-template': 'off',
    'template-curly-spacing': ['error', 'never'],
    'linebreak-style': ['error', 'unix'],
    'newline-per-chained-call': 'off',
    'no-lonely-if': 'off',
    'new-cap': ['error', {'capIsNewExceptions': ['Deferred']}],
    'no-multi-assign': 'error',
    'no-unused-vars': 'off',
    'require-jsdoc': 'off',
    'no-plusplus': 'off',
    'no-trailing-spaces': 'error',
    'space-before-function-paren': ['error', {
      'anonymous': 'never',
      'named': 'never',
      'asyncArrow': 'always'
    }],
    'curly': ['error', 'all'],
    'eqeqeq': ['error', 'always', {'null': 'ignore'}],
    'spaced-comment': ['error', 'always', {'exceptions': ['*']}],
    'valid-jsdoc': 'off'
  }
};
