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
module.exports = (env, args) => {
  const config = baseConfig(env, args);

  let testingConfig = {
    entry: {
      'eclipse-scout-testing.esm': './src/testing/index.ts'
    },
    ...baseConfig.libraryConfig(config)
  };
  testingConfig.externals = [
    baseConfig.rewriteIndexImports('@eclipse-scout/core', 'testing'),
    testingConfig.externals
  ];

  return testingConfig;
};
