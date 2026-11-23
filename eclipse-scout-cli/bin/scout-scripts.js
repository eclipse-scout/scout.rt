#!/usr/bin/env node
/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
// argv[3..n] arguments for the script
const script = process.argv[2];
const argv = process.argv.slice(3);
const parser = require('yargs-parser');
const fs = require('fs');
const path = require('path');
const scoutBuildConstants = require('./../scripts/constants');
const webpackConfigFileName = './webpack.config.js';
const webpackCustomConfigFileName = './webpack.config.custom.js';
const webpackYargsOptions = {
  boolean: ['progress', 'profile', 'clean', 'watch', 'cyclonedxSkip'],
  array: ['resDirArray', 'themes']
};
const buildYargsOptions = {
  array: ['run'],
  default: {run: []}
};
const testYargsOptions = prepareTestYargsOptions();

let buildArgs = parser(argv, buildYargsOptions);
if (buildArgs.run.length > 1) {
  runBuilds(buildArgs);
  return;
}

switch (script) {
  case 'test-server:start': {
    require('../scripts/test-support/test-runner').startTestServer(parser(argv, testYargsOptions));
    break;
  }
  case 'test-server:stop': {
    require('../scripts/test-support/test-runner').stopTestServer(parser(argv, testYargsOptions));
    break;
  }
  case 'test:ci': {
    const args = parser(argv, testYargsOptions);
    args.webpackArgs = {progress: false, ...args.webpackArgs, watch: false};
    require('../scripts/test-support/test-runner').runTests(args);
    break;
  }
  case 'build:dev': {
    const args = parser(argv, webpackYargsOptions);
    args.mode = scoutBuildConstants.mode.development;
    runWebpack(args);
    break;
  }
  case 'build:prod': {
    const args = parser(argv, webpackYargsOptions);
    args.mode = scoutBuildConstants.mode.production;
    runWebpack(args);
    break;
  }
  case 'build:dev:watch': {
    const args = parser(argv, webpackYargsOptions);
    args.mode = scoutBuildConstants.mode.development;
    args.watch = true;
    args.clean = true; // prevents errors because of old output folders in the development environment
    runWebpack(args);
    break;
  }
  default:
    console.log(`Unknown script "${script}"`);
    break;
}

function runBuilds(args) {
  const execSync = require('child_process').execSync;
  let argStr = '';
  for (let [key, value] of Object.entries(args)) {
    if (key !== 'run' && key !== '_') {
      argStr += `--${key} ${value} `;
    }
  }
  for (let type of args.run) {
    console.log(`Starting ${type} build` + (argStr ? ` with args ${argStr}` : ''));
    execSync(`scout-scripts ${script} --run ${type} ${argStr}`, {stdio: 'inherit'});
  }
}

function runWebpack(args) {
  const configFilePath = readWebpackConfig();
  if (!configFilePath) {
    return;
  }
  const {compiler, statsConfig} = createWebpackCompiler(configFilePath, args);
  if (args.watch) {
    compiler.watch({}, (err, stats) => logWebpack(err, stats, statsConfig));
  } else {
    compiler.run((err, stats) => logWebpack(err, stats, statsConfig));
  }
}

function readWebpackConfig() {
  let configFilePath;
  const devConfigFilePath = path.resolve(webpackCustomConfigFileName);
  if (fs.existsSync(devConfigFilePath)) {
    console.log(`Reading config from ${webpackCustomConfigFileName}`);
    configFilePath = devConfigFilePath;
  } else {
    configFilePath = path.resolve(webpackConfigFileName);
  }
  if (!fs.existsSync(configFilePath)) {
    // No config file found -> abort
    console.log(`Skipping webpack build (config file ${webpackConfigFileName} not found)`);
    return;
  }
  return configFilePath;
}

function createWebpackCompiler(configFilePath, args) {
  const webpack = require('webpack');
  const conf = require(configFilePath);
  const webpackConfig = conf(args.env, args);
  const statsConfig = args.stats || webpackConfig.stats;
  const compiler = webpack(webpackConfig);
  return {compiler, statsConfig};
}

function logWebpack(err, stats, statsConfig) {
  if (err) {
    console.error(err);
    process.exitCode = 2; // let the webpack build fail on errors
    return;
  }
  const info = stats.toJson();
  if (stats.hasErrors()) {
    console.error(info.errors);
    process.exitCode = 1; // let the webpack build fail on errors
  }
  if (info.warnings && info.warnings.length > 0) {
    console.warn(info.warnings);
  }
  statsConfig = statsConfig || {};
  if (typeof statsConfig === 'string') {
    statsConfig = stats.compilation.createStatsOptions(statsConfig);
  }
  statsConfig.colors = true;
  console.log(stats.toString(statsConfig) + '\n\n');
}

/**
 * Options of the test arguments: the webpack arguments prefixed with webpackArgs and the test arguments (see test-defaults.js).
 */
function prepareTestYargsOptions() {
  const result = {
    string: ['filter', 'junitReportDir'],
    boolean: ['headless', 'openBrowser'],
    number: ['port']
  };
  for (let [key, value] of Object.entries(webpackYargsOptions)) {
    value = value.map(elem => 'webpackArgs.' + elem);
    result[key] = [...(result[key] || []), ...value];
  }
  return result;
}
