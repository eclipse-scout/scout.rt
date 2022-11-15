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
  args.resDirArray = ['res'];
  const config = baseConfig(env, args);

  if (config.output.clean) {
    // output.clean will (randomly) clean resources built by this build
    // -> Delete the output folder "manually" and disable the clean plugin
    const {deleteFolder} = require('@eclipse-scout/cli/scripts/files');
    deleteFolder(config.output.path);
    config.output.clean = false;
  }

  let esmConfig = {
    entry: {
      'eclipse-scout-core.esm': './src/index.ts'
    },
    // Clean is false because the last config will clean it
    ...baseConfig.libraryConfig(config, {clean: false})
  };

  let testingConfig = {
    entry: {
      'eclipse-scout-testing.esm': './src/testing/index.ts'
    },
    ...baseConfig.libraryConfig(config, {clean: false, externalizeDevDeps: true})
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

  // This build creates resources that can directly be included in a html file without needing a build stack (webpack, babel etc.).
  // The resources are available by a CDN that provides npm modules (e.g. https://www.jsdelivr.com/package/npm/@eclipse-scout/core)
  let globalConfig = {
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

  return [esmConfig, testingConfig, globalConfig];
};
