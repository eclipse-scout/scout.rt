/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

const baseConfig = require('@eclipse-scout/cli/scripts/webpack-defaults');
module.exports = (env, args) => {
  const config = require('./webpack.config.global.js')(env, args);
  return {
    ...config,
    entry: {
      'eclipse-scout-testing': './src/testing/index.ts'
    },
    externals: [
      baseConfig.rewriteIndexImports('scout', 'testing'),
      {
        ...config.externals
      }
    ]
  };
};
