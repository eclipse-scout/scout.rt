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
const baseConfig = require('@eclipse-scout/cli/scripts/webpack-defaults');
const path = require('path');
module.exports = (env, args) => {
  const config = baseConfig(env, args);

  let testingConfig = {
    entry: {
      'eclipse-scout-testing.esm': './src/testing/index.ts'
    },
    ...baseConfig.libraryConfig(config, {externalizeDevDeps: true})
  };
  testingConfig.externals = [
    testingConfig.externals,
    ({context, request, contextInfo}, callback) => {
      // Externalize every import to the main index and replace it with @eclipse-scout/core
      // Keep imports to the testing index
      if (/\/index$/.test(request) && !path.resolve(context, request).includes('testing')) {
        return callback(null, '@eclipse-scout/core');
      }

      // Continue without externalizing the import
      callback();
    }
  ];

  return testingConfig;
};
