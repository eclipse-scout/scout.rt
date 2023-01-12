/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
