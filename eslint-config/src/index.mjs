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
import path from 'path';
import common from './common.mjs';
import noFloatingPromises from './noFloatingPromises.mjs';

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
      ecmaVersion: 2022,
      sourceType: 'module'
    },
    extends: [eslint.configs.recommended, common]
  },
  // Additional config for TypeScript files (extends the base config from above)
  {
    extends: [tseslint.configs.recommended],
    plugins: {
      '@typescript-eslint': tseslint.plugin,
      '@eclipse-scout': {
        rules: {
          'no-floating-promises': noFloatingPromises
        }
      }
    },
    files: ['**/*.ts', '**/*.tsx'],
    languageOptions: {
      parserOptions: {
        projectService: true, // Type information is required by rules like no-floating-promises. Uses the nearest tsconfig.json of each file.
        // The nearest tsconfig.json is only searched up to tsconfigRootDir, which defaults to the working directory.
        // Use the file system root so the tsconfig.json of the module is found even if ESLint is run in a subfolder of the module.
        tsconfigRootDir: path.parse(process.cwd()).root
      }
    },
    rules: {
      // A promise that is neither returned, awaited nor handled cannot be waited for, e.g. by a form lifecycle (load -> markAsSaved) or a spec.
      // Event handlers (_onXyz methods, callbacks passed to on()/one()) are ignored, nobody waits for them anyway.
      // Other intended fire-and-forget calls can be marked with the void operator: void this._doSomethingAsync();
      // Calls of known safe methods are ignored as well, e.g. ValueField.setValue() (see noFloatingPromises.mjs, projects can add more with the option allowForKnownSafeMethods).
      '@eclipse-scout/no-floating-promises': 'warn',
      'spaced-comment': ['error', 'always', {'exceptions': ['*'], 'markers': ['/']}], // Allow triple slash directives
      'prefer-const': 'off', // Enabled by TS ESLint, but we do not want to enforce it for now
      '@typescript-eslint/no-empty-object-type': 'off',
      '@typescript-eslint/no-inferrable-types': 'warn', // Changed from error to warn
      '@typescript-eslint/ban-ts-comment': 'off', // Allow ts-ignore
      '@typescript-eslint/no-unused-vars': 'off', // Allow unused parameters
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-this-alias': 'off', // Allow assigment of this to a variable, e.g. for better readability. 'That' and 'self' are not used often anymore.
      '@typescript-eslint/prefer-ts-expect-error': 'warn',
      '@typescript-eslint/no-unused-expressions': 'off', // Disabled to make log pattern work that is commonly used in scout ($.log.isTraceEnabled() && $.log.trace(msg));
      '@stylistic/member-delimiter-style': 'warn', // Enforce semicolon for interface members for consistency
      '@stylistic/object-curly-spacing': ['warn', 'never', {'overrides': {TSTypeLiteral: 'always', TSMappedType: 'always'}}]
    }
  }
);
