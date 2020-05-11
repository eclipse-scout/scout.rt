/*
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
const path = require('path');
const scoutBuild = require('./constants');

const jquery = require.resolve('jquery');
const fs = require('fs');
const webpack = require('webpack');

module.exports = (config, specEntryPoint) => {
  const webpackConfigFilePath = path.resolve('webpack.config.js');
  if (!fs.existsSync(webpackConfigFilePath)) {
    const message = 'Karma requires a webpack config file at location "' + webpackConfigFilePath + '" but it could not be found.';
    console.error(message);
    throw new Error(message);
  }
  let webpackConfigProvider = require(webpackConfigFilePath);

  const webpackArgs = Object.assign({mode: scoutBuild.mode.development}, config.webpackArgs);
  const webpackConfig = webpackConfigProvider(null, webpackArgs);
  delete webpackConfig.entry;

  const sourceMapPlugin = webpackConfig.plugins.find(plugin => plugin instanceof webpack.SourceMapDevToolPlugin);
  if (sourceMapPlugin) {
    // Use inline source maps because external source maps are not supported by karma (https://github.com/webpack-contrib/karma-webpack/issues/224)
    delete sourceMapPlugin.sourceMapFilename;
  }

  const specIndex = specEntryPoint ? path.resolve(specEntryPoint) : path.resolve('test', 'test-index.js');
  const preprocessorObj = {};
  preprocessorObj[specIndex] = ['webpack'];

  config.set({
    browsers: ['Chrome'],
    files: [
      {
        pattern: jquery,
        watched: false
      }, {
        pattern: specIndex,
        watched: false
      }],
    frameworks: ['jasmine-scout', 'jasmine-jquery', 'jasmine-ajax', 'jasmine'], /* order of the frameworks is relevant! */
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
