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
const specIndex = path.resolve('test', 'test-index.js');
const jquery = require.resolve('jquery');

var webpackConfigProvider = require(path.resolve('webpack.config.js'));
const webpackConfig = webpackConfigProvider(null, {mode: scoutBuild.mode.development});
delete webpackConfig.entry;

const preprocessorObj = {};
preprocessorObj[specIndex] = ['webpack'];

module.exports = function(config) {
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
      require('karma-jasmine-jquery'),
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
