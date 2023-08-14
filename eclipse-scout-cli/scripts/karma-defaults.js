/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
const fs = require('fs');
const path = require('path');
const jquery = require.resolve('jquery');
const scoutBuildConstants = require('./constants');
const {SourceMapDevToolPlugin} = require('webpack');
const StatsExtractWebpackPlugin = require('./StatsExtractWebpackPlugin');

module.exports = (config, specEntryPoint) => {
  const webpackConfigFilePath = path.resolve('webpack.config.js');
  if (!fs.existsSync(webpackConfigFilePath)) {
    const message = 'Karma requires a webpack config file at location "' + webpackConfigFilePath + '" but it could not be found.';
    console.error(message);
    throw new Error(message);
  }
  let webpackConfigProvider = require(webpackConfigFilePath);

  const webpackArgs = Object.assign({
    mode: scoutBuildConstants.mode.development,
    watch: true, // by default tests are running in watch mode
    tsOptions: {
      compilerOptions: {
        // No need to create declarations for tests
        declaration: false,
        declarationMap: false
      }
    }
  }, config.webpackArgs);
  let webpackConfig = webpackConfigProvider(null, webpackArgs);
  if (Array.isArray(webpackConfig)) {
    webpackConfig = webpackConfig[0];
  }
  delete webpackConfig.entry;
  if (webpackConfig.optimization) {
    delete webpackConfig.optimization.splitChunks; // disable splitting for tests
  }

  if (webpackConfig.output) {
    // Remove output file as Karma uses an in-memory middleware and complains if an output file is present
    delete webpackConfig.output.filename;
    // Don't create a library, it would create an error during test run (module not found)
    delete webpackConfig.output.library;
  }

  if (webpackConfig.externals) {
    // Remove externals, so they don't have to be provided
    // Add jquery as the only external, so it won't be loaded twice because it is provided by @metahub/karma-jasmine-jquery
    webpackConfig.externals = {
      'jquery': 'global jQuery'
    };
  }

  // specify output directory for webpack (use different from normal output dir so that they are not polluted with test artifacts)
  webpackConfig.output = webpackConfig.output || {};
  webpackConfig.output.path = path.resolve(scoutBuildConstants.outDir.target, scoutBuildConstants.outDir.distKarma, scoutBuildConstants.outSubDir.development);
  fs.mkdirSync(webpackConfig.output.path, {recursive: true});

  webpackConfig.watch = !!webpackArgs.watch;

  const sourceMapPlugin = webpackConfig.plugins.find(plugin => plugin instanceof SourceMapDevToolPlugin);
  if (sourceMapPlugin) {
    // Use inline source maps because external source maps are not supported by karma (https://github.com/webpack-contrib/karma-webpack/issues/224)
    delete sourceMapPlugin.sourceMapFilename;
  }
  webpackConfig.plugins.push(new StatsExtractWebpackPlugin()); // used by scout-scripts to access the webpack build result.

  const specIndex = searchSpecEntryPoint(specEntryPoint);
  const preprocessorObj = {};
  preprocessorObj[specIndex] = ['webpack'];

  config.set({
    browsers: ['Chrome'],
    files: [
      {pattern: jquery, watched: false},
      {pattern: specIndex, watched: false}
    ],
    frameworks: ['webpack', 'jasmine-scout', 'jasmine-jquery', 'jasmine-ajax', 'jasmine'], /* order of the frameworks is relevant! */
    // Reporter for "Jasmine Spec Runner" results in browser
    // https://www.npmjs.com/package/karma-jasmine-html-reporter
    reporters: ['kjhtml', 'junit'],
    junitReporter: {
      outputDir: './test-results/',
      outputFile: 'test-results.xml'
    },
    plugins: [
      require('karma-jasmine-html-reporter'),
      require('karma-junit-reporter'),
      require('karma-jasmine'),
      require('@metahub/karma-jasmine-jquery'),
      require('karma-jasmine-ajax'),
      require('@eclipse-scout/karma-jasmine-scout'),
      require('karma-webpack'),
      require('karma-chrome-launcher')
    ],
    client: {
      // Leave "Jasmine Spec Runner" output visible in browser
      clearContext: false,
      jasmine: {
        random: false
      }
    },
    preprocessors: preprocessorObj,
    // If true, Karma will start and capture all configured browsers, run tests and then exit with an exit code of 0 or 1
    // depending on whether all tests passed or any tests failed.
    singleRun: false,
    // Webpack
    webpack: webpackConfig,
    webpackServer: {
      noInfo: true
    }
  });
};

function searchSpecEntryPoint(specEntryPoint) {
  if (specEntryPoint) {
    return path.resolve(specEntryPoint);
  }
  let defaultTypescriptIndex = path.resolve('test', 'test-index.ts');
  if (fs.existsSync(defaultTypescriptIndex)) {
    return defaultTypescriptIndex;
  }
  return path.resolve('test', 'test-index.js');
}
