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

  // This build creates resources that can directly be included in a html file without needing a build stack (webpack, babel etc.).
  // The resources are available by a CDN that provides npm modules (e.g. https://www.jsdelivr.com/package/npm/@eclipse-scout/core)
  return {
    ...config,
    entry: {
      'eclipse-scout-core': './src/index.ts',
      'eclipse-scout-core-theme': './src/index.less',
      'eclipse-scout-core-theme-dark': './src/index-dark.less'
    },
    optimization: {
      ...config.optimization,
      splitChunks: undefined // disable splitting
    },
    externals: {
      ...config.externals,
      // Dependencies should not be included in the resulting js file.
      // The consumer has to include them by himself which gives him more control (maybe his site has already added jQuery or he wants to use another version)
      // Left side is the import name, right side the name of the global variable added by the plugin (e.g. window.jQuery)
      'jquery': 'jQuery',
      'sourcemapped-stacktrace': 'sourceMappedStackTrace'
    }
  };
};
