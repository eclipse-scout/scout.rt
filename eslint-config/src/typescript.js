/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
module.exports = {
  extends: [
    '@eclipse-scout',
    'plugin:@typescript-eslint/recommended',
    './common' // @typescript-eslint/recommended imports @typescript-eslint/eslint-recommended which overrides some rules of scout -> re override them again by importing common
  ],
  plugins: ['@typescript-eslint'],
  parser: '@typescript-eslint/parser',
  rules: {
    '@typescript-eslint/ban-types': 'warn', // Change from error to warn
    '@typescript-eslint/no-inferrable-types': 'warn', // Change from error to warn
    '@typescript-eslint/ban-ts-comment': 'off', // Allow ts-ignore
    '@typescript-eslint/no-unused-vars': 'off', // Allow unused parameters
    'spaced-comment': ['error', 'always', {'exceptions': ['*'], 'markers': ['/']}], // Allow triple slash directives
    'semi': 'off', // Disable because it will be replaced by typescript rule below to avoid conflicting rules with semicolon in interfaces
    '@typescript-eslint/semi': ['error'],
    '@typescript-eslint/member-delimiter-style': 'warn', // Enforce semicolon for interface members for consistency,
    '@typescript-eslint/no-this-alias': 'off', // Allow assigment of this to a variable, e.g. for better readability. 'That' and 'self' are not used often anymore.
    'indent': 'off', // Disable because it will be replaced by the following ts rule
    '@typescript-eslint/indent': ['error', 2, {'ignoredNodes': ['PropertyDefinition[decorators]']}], // Workarounds bug https://github.com/typescript-eslint/typescript-eslint/issues/1824. See also https://github.com/eslint/eslint/issues/15299#issuecomment-967762181
    'linebreak-style': 'off'
  }
};
