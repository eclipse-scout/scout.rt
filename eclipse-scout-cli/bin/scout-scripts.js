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
    runKarma('./karma.conf.js');
    break;
  }
  case 'test-server:stop': {
    // TODO default port wird angenommen
    const stopper = require('karma').stopper;
    stopper.stop({ port: 9876 }, exitCode => {
      if (exitCode === 0) {
        console.log('Server stop as initiated');
      }
      process.exit(exitCode);
    });
    break;
  }
  case 'test:ci': {
    runKarma('./karma.conf.ci.js');
    break;
  }
  case 'build:dev': {
    runWebpack({ mode: 'development' });
    break;
  }
  case 'build:prod': {
    runWebpack({ mode: 'production' });
    break;
  }
  case 'build:dev:watch': {
    runWebpackWatch({ mode: 'development' });
    break;
  }
  case 'snapshot-version': {
    require('../scripts/snapshot-version');
    break;
  }
  default:
    console.log(`Unknown script "${script}"`);
    break;
}

function runKarma(configFileName) {
  const cfg = require('karma').config;
  const configFilePath = path.resolve(configFileName);
  if (!fs.existsSync(configFilePath)) {
    // No config file found -> abort
    console.log(`Skipping karma test run (config file ${configFileName} not found)`);
    return;
  }
  const karmaConfig = cfg.parseConfig(configFilePath);
  const Server = require('karma').Server;
  const serverInstance = new Server(karmaConfig, exitCode => {
    console.log(`Karma has exited with ${exitCode}`);
    process.exit(exitCode);
  });
  console.log(`Starting karma server using config file ${configFileName}`);
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
  }
  if (stats.hasWarnings()) {
    console.warn(info.warnings);
  }
  console.log(stats.toString({
    colors: true
  }) + '\n\n');
}
