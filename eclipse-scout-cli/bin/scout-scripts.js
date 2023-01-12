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
const StatsExtractWebpackPlugin = require('../scripts/StatsExtractWebpackPlugin');
const webpackYargsOptions = {
  boolean: ['progress', 'profile', 'clean'],
  array: ['resDirArray', 'themes']
};
const buildYargsOptions = {
  array: ['run'],
  default: {run: []}
};
const karmaYargsOptions = prepareWebpackYargsOptionsForKarma();

let buildArgs = parser(argv, buildYargsOptions);
if (buildArgs.run.length > 1) {
  runBuilds(buildArgs);
  return;
}

switch (script) {
  case 'test-server:start': {
    runKarma(null, false, parser(argv, karmaYargsOptions));
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
    let args = parser(argv, karmaYargsOptions);
    args.webpackArgs = args.webpackArgs || {};
    if (args.webpackArgs.progress === undefined) {
      args.webpackArgs.progress = false;
    }
    if (args.webpackArgs.watch === undefined) {
      args.webpackArgs.watch = false;
    }
    runKarma(null, true, args);
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

function runKarma(configFileName, headless, args) {
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
  const karmaConfig = cfg.parseConfig(configFilePath, args);
  if (autoSetupHeadlessConfig) {
    // only executed if headless and no specific CI config file is found: use headless defaults
    karmaConfig.set({
      singleRun: true,
      autoWatch: false,
      browsers: ['ChromeHeadless']
    });
  }

  let exitCode = 100;
  const Server = require('karma').Server;
  const serverInstance = new Server(karmaConfig, karmaExitCode => {
    if (exitCode === 0) {
      console.log('Karma has exited with 0.');
      process.exitCode = exitCode; // all fine: tests could be executed and no failures
    } else if (exitCode === 10) {
      console.log('Webpack build failed. See webpack output for details.');
      process.exitCode = exitCode; // webpack error
    } else if (exitCode === 4) {
      console.log('There are test failures.');
      process.exitCode = 0; // test could be executed but there are test failures: do not set exitCode to something other than 0 here because the build should continue even on failing tests.
    } else {
      console.log(`Error in test execution. Exit code: ${exitCode}.`);
      process.exitCode = exitCode; // tests could not be executed because of an error. Let the process fail.
    }
  });

  serverInstance.on('run_complete', (browsers, results) => {
    // compute exit code based on webpack stats and karma result.
    // Karma exitCode alone is not detailed enough (all problems have exitCode=1).
    let webpackStats = null;
    let statsExtractPlugins = karmaConfig.webpack.plugins.filter(p => p instanceof StatsExtractWebpackPlugin);
    if (statsExtractPlugins && statsExtractPlugins.length) {
      webpackStats = statsExtractPlugins[0].stats;
    }
    exitCode = computeExitCode(results, webpackStats);
  });

  console.log(`Starting Karma server using config file ${configFilePath}`);
  serverInstance.start();
}

/**
 * Inspired by Karma.BrowserCollection.calculateExitCode().
 *
 * @param karmaResults The Karma results object
 * @param webpackStats The Webpack build stats object
 * @returns {number} The custom exit code
 */
function computeExitCode(karmaResults, webpackStats) {
  if (webpackStats && webpackStats.hasErrors()) {
    return 10; // webpack build failed
  }
  if (karmaResults.disconnected) {
    return 2; // browser disconnected
  }
  if (karmaResults.error) {
    return 3; // karma error
  }
  if (karmaResults.failed > 0) {
    return 4; // tests could be executed but there are test failures (Karma uses exitCode=1 here which is not what we want)
  }
  return karmaResults.exitCode;
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
  statsConfig = statsConfig || {};
  if (typeof statsConfig === 'string') {
    statsConfig = stats.compilation.createStatsOptions(statsConfig);
  }
  statsConfig.colors = true;
  console.log(stats.toString(statsConfig) + '\n\n');
}

/**
 * Prepends the values of the arrays in the options with webpackArgs.
 */
function prepareWebpackYargsOptionsForKarma() {
  let result = {};
  for (let [key, value] of Object.entries(webpackYargsOptions)) {
    if (Array.isArray(value)) {
      value = value.map(elem => 'webpackArgs.' + elem);
    }
    result[key] = value;
  }
  return result;
}
