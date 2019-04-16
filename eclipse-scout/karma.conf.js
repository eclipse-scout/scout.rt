/*******************************************************************************
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
let path = require('path');

var specIndex = path.resolve(__dirname, 'test/test-index.js');
var preprocessorObj = {};
preprocessorObj[specIndex] = ['webpack'];

module.exports = function(config) {
  config.set({
    browsers: ['Chrome'],
    files: [{
      pattern: specIndex,
      watched: false
    }],
    frameworks: ['jasmine'],
    // Reporter for "Jasmine Spec Runner" results in browser
    // https://www.npmjs.com/package/karma-jasmine-html-reporter
    reporters: ['kjhtml'],
    client: {
      // Leave "Jasmine Spec Runner" output visible in browser
      clearContext: false
    },
    preprocessors: preprocessorObj,
    // If true, Karma will start and capture all configured browsers, run tests and then exit with an exit code of 0 or 1
    // depending on whether all tests passed or any tests failed.
    singleRun: false,
    // Webpack
    webpack: {
      mode: 'development'
    },
    webpackServer: {
      noInfo: true
    }
  });
};
