/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import globals from 'globals';
import eslint from '@eslint/js';
import {defineConfig} from 'eslint/config';
import tseslint from 'typescript-eslint';
import common from './common.mjs';

export default defineConfig(
  {
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.jquery,
        ...globals.jasmine
      },
      parser: tseslint.parser,
      ecmaVersion: 2022,
      sourceType: 'module'
    },
    extends: [eslint.configs.recommended, common]
  },
  {
    extends: [tseslint.configs.recommended, common],
    plugins: {
      '@typescript-eslint': tseslint.plugin
    },
    files: ['**/*.ts', '**/*.tsx'],
    rules: {
      '@typescript-eslint/no-empty-object-type': 'off',
      '@typescript-eslint/no-inferrable-types': 'warn', // Changed from error to warn
      '@typescript-eslint/ban-ts-comment': 'off', // Allow ts-ignore
      '@typescript-eslint/no-unused-vars': 'off', // Allow unused parameters
      'spaced-comment': ['error', 'always', {'exceptions': ['*'], 'markers': ['/']}], // Allow triple slash directives
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-this-alias': 'off', // Allow assigment of this to a variable, e.g. for better readability. 'That' and 'self' are not used often anymore.
      '@typescript-eslint/prefer-ts-expect-error': 'warn',
      '@typescript-eslint/no-unused-expressions': 'off' // Disabled to make log pattern work that is commonly used in scout ($.log.isTraceEnabled() && $.log.trace(msg));
    }
  }
);
