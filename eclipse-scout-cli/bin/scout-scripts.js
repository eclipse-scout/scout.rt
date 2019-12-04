#!/usr/bin/env node
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

// Makes the script crash on unhandled rejections instead of silently
// ignoring them. In the future, promise rejections that are not handled will
// terminate the Node.js process with a non-zero exit code.
process.on('unhandledRejection', err => {
  throw err;
});

// argv[0] path to node
// argv[1] path to js file
// argv[2] name of called script
const script = process.argv[2];
const fs = require('fs');
const path = require('path');
const webpackConfigFileName = './webpack.config.js';
switch (script) {
  case 'test-server:start': {
    runKarma();
    break;
  }
  case 'test-server:stop': {
    const stopper = require('karma').stopper;
    // default port assumed
    stopper.stop({port: 9876}, exitCode => {
      if (exitCode === 0) {
        console.log('Server stop as initiated');
      }
      process.exitCode = exitCode;
    });
    break;
  }
  case 'test:ci': {
    runKarma(null, true);
    break;
  }
  case 'build:dev': {
    runWebpack({mode: 'development'});
    break;
  }
  case 'build:prod': {
    runWebpack({mode: 'production'});
    break;
  }
  case 'build:dev:watch': {
    runWebpackWatch({mode: 'development'});
    break;
  }
  default:
    console.log(`Unknown script "${script}"`);
    break;
}

function runKarma(configFileName, headless) {
  const cfg = require('karma').config;
  const cfgFile = configFileName || './karma.conf.js';

  let configFilePath = null;
  let autoSetupHeadlessConfig = headless;
  if (headless) {
    // try with CI config file first
    const ciConfigFilePath = path.resolve('./karma.conf.ci.js');
    if (fs.existsSync(ciConfigFilePath)) {
      configFilePath = ciConfigFilePath;
      autoSetupHeadlessConfig = false; // no need to autoconfig for headless mode because there is a specific CI config file
    }
  }
  if (configFilePath == null) {
    configFilePath = path.resolve(cfgFile);
  }
  if (!fs.existsSync(configFilePath)) {
    // No config file found -> abort
    console.log(`Skipping Karma test run (config file ${cfgFile} not found)`);
    return;
  }

  // load config
  const karmaConfig = cfg.parseConfig(configFilePath);
  if (autoSetupHeadlessConfig) {
    // only executed if headless and no specific CI config file is found: use headless defaults
    karmaConfig.set({
      singleRun: true,
      autoWatch: false,
      browsers: ['ChromeHeadless']
    });
  }

  const Server = require('karma').Server;
  const serverInstance = new Server(karmaConfig, exitCode => {
    if (exitCode === 0) {
      console.log('Karma has exited with 0');
    } else {
      console.log(`There are test failures. Karma has exited with ${exitCode}`);
    }
    process.exit(0); // do not set exitCode of karma to the process here because the build should continue even on failing tests. therefore always use exitCode zero.
  });
  console.log(`Starting Karma server using config file ${configFilePath}`);
  serverInstance.start();
}

function runWebpack(webPackArgs) {
  const configFilePath = path.resolve(webpackConfigFileName);
  if (!fs.existsSync(configFilePath)) {
    // No config file found -> abort
    console.log(`Skipping webpack build (config file ${webpackConfigFileName} not found)`);
    return;
  }
  const compiler = createWebpackCompiler(configFilePath, webPackArgs);
  compiler.run(logWebpack);
}

function runWebpackWatch(webPackArgs) {
  const configFilePath = path.resolve(webpackConfigFileName);
  webPackArgs = Object.assign(webPackArgs || {}, {watch: true});
  const compiler = createWebpackCompiler(configFilePath, webPackArgs);
  compiler.watch({}, logWebpack);
}

function createWebpackCompiler(configFilePath, webPackArgs) {
  const webpack = require('webpack');
  const conf = require(configFilePath);
  const webpackConfig = conf(process.env, webPackArgs);
  return webpack(webpackConfig);
}

function logWebpack(err, stats) {
  if (err) {
    console.error(err);
    return;
  }
  const info = stats.toJson();
  if (stats.hasErrors()) {
    console.error(info.errors);
    process.exitCode = 1; // let the webpack build fail on errors
  }
  if (stats.hasWarnings()) {
    console.warn(info.warnings);
  }
  console.log(stats.toString({
    colors: true
  }) + '\n\n');
}
