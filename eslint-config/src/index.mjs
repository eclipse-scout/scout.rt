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
  // Base config for JavaScript files
  {
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.jquery,
        ...globals.jasmine
      },
      parser: tseslint.parser,
      ecmaVersion: 'latest',
      sourceType: 'module'
    },
    extends: [eslint.configs.recommended, common]
  },
  // Additional config for TypeScript files (extends the base config from above)
  {
    extends: [tseslint.configs.recommended],
    plugins: {
      '@typescript-eslint': tseslint.plugin
    },
    files: ['**/*.ts', '**/*.tsx'],
    rules: {
      'prefer-const': 'off', // Enabled by TS ESLint, but we do not want to enforce it for now
      '@typescript-eslint/no-empty-object-type': 'off',
      '@typescript-eslint/no-inferrable-types': 'warn', // Changed from error to warn
      '@typescript-eslint/ban-ts-comment': ['warn', {'ts-expect-error': false, 'ts-ignore': true, 'ts-nocheck': false, 'ts-check': false}], // Prefer ts-expect-error over ts-ignore
      '@typescript-eslint/no-unused-vars': 'off', // Allow unused parameters
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-namespace': ['error', {'allowDeclarations': true}], // Allow namespaces in declarations, e.g. to augment third-party types
      '@typescript-eslint/no-this-alias': 'off', // Allow assigment of this to a variable, e.g. for better readability. 'That' and 'self' are not used often anymore.
      '@typescript-eslint/no-unused-expressions': 'off', // Disabled to make log pattern work that is commonly used in scout ($.log.isTraceEnabled() && $.log.trace(msg));
      '@stylistic/member-delimiter-style': 'warn', // Enforce semicolon for interface members for consistency
      '@stylistic/object-curly-spacing': ['warn', 'never', {'overrides': {TSTypeLiteral: 'always', TSMappedType: 'always'}}]
    }
  }
);
