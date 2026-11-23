/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
const fs = require('fs');
const os = require('os');
const path = require('path');
const scoutBuildConstants = require('../constants');
const CdpDriver = require('./CdpDriver');
const JasmineJUnitReporter = require('./JasmineJUnitReporter');

const STOP_PATH = '/__scout__/stop';
// request header required by the stop endpoint
const STOP_HEADER = 'x-scout-stop-test-server';
// IPv4 address the test server listens on and receives its requests at
const HOST = '127.0.0.1';
// files of the resources directory loaded before jasmine-core
const PRELOAD_FILES = ['unhandledRejectionFilter.js'];
// subdirectory of the URL path of jasmine-core ('/__jasmine__') serving the PRELOAD_FILES
const PRELOAD_DIR = 'scout';

const exitCodes = {
  success: 0,
  error: 3,
  testFailures: 4,
  webpackError: 10
};

/**
 * Builds the specs of the module in the current working directory, runs them once in the browser and writes a JUnit report.
 * Logs the result and sets the exit code of the process: 0 if the tests could be executed (even if some failed), otherwise one of the exitCodes.
 *
 * @param {object} args command line arguments (see test-defaults.js for the available properties)
 */
async function runTests(args) {
  let exitCode;
  try {
    exitCode = await executeTests(args);
  } catch (err) {
    console.error(err);
    exitCode = exitCodes.error;
  }
  if (exitCode === null) {
    return;
  }
  if (exitCode === exitCodes.success) {
    console.log('All tests passed.');
  } else if (exitCode === exitCodes.testFailures) {
    console.log('There are test failures.');
    // the tests could be executed: the build continues even if tests fail
    process.exitCode = exitCodes.success;
  } else if (exitCode === exitCodes.webpackError) {
    console.log('Webpack build failed. See webpack output for details.');
    process.exitCode = exitCode;
  } else {
    console.log(`Error in test execution. Exit code: ${exitCode}.`);
    process.exitCode = exitCode;
  }
}

/**
 * @returns {Promise<number|null>} one of the exitCodes, null if the module has no test config
 */
async function executeTests(args) {
  const config = loadConfig(args);
  if (!config) {
    console.log(`Skipping tests (config file ${scoutBuildConstants.testConfigFileName} not found)`);
    return null;
  }
  const helperFiles = copyHelpers(config.helpers);
  try {
    await build(config);
  } catch (err) {
    return exitCodes.webpackError;
  }

  const jbr = require('jasmine-browser-runner');
  const runnerConfig = createRunnerConfig(config, helperFiles);
  const junitReporter = new JasmineJUnitReporter(path.resolve(config.junitReportDir, 'test-results.xml'));
  const reporters = [createConsoleReporter(jbr), junitReporter];
  const result = await runWithChrome(jbr, runnerConfig, reporters, config.headless);
  console.log(`JUnit report: ${junitReporter.file}`);
  return computeExitCode(result, junitReporter);
}

/**
 * Builds the specs in watch mode, starts the test server and opens Chrome. The server is started after the first successful build,
 * the specs are executed again after every rebuild. Use the argument --openBrowser=false to start the server only.
 */
async function startTestServer(args) {
  const config = loadConfig({...args, webpackArgs: {...args.webpackArgs, watch: true}});
  if (!config) {
    console.log(`Test server not started (config file ${scoutBuildConstants.testConfigFileName} not found)`);
    return;
  }
  const jbr = require('jasmine-browser-runner');
  let server = null;
  let driver = null;
  let watching = null;
  let runnerConfig = null;

  let stopping = null;
  const stop = () => {
    stopping ||= (async () => {
      console.log('Stopping test server');
      if (watching) {
        await new Promise(resolve => watching.close(resolve));
      }
      if (driver) {
        await driver.close();
      }
      if (server) {
        await server.stop();
      }
    })();
    return stopping;
  };
  onTermination(stop);

  let runQueue = Promise.resolve();
  const runSpecs = () => {
    runQueue = runQueue.then(async () => {
      if (!driver) {
        return;
      }
      try {
        const runner = new jbr.Runner({webdriver: driver, reporters: [createConsoleReporter(jbr)], host: serverUrl(server)});
        await runner.run(runnerConfig);
      } catch (err) {
        if (stopping) {
          return;
        }
        if (!driver.connectionClosed) {
          console.error('Test run failed', err);
          return;
        }
        console.log(`The browser has been closed. Open ${serverUrl(server)}/ in a browser to run the specs.`);
        const closedDriver = driver;
        driver = null;
        await closedDriver.close();
      }
    });
  };

  const helperFiles = copyHelpers(config.helpers);
  watching = await build(config, () => runSpecs());
  runnerConfig = createRunnerConfig(config, helperFiles, {
    [STOP_PATH]: (req, res) => {
      if (req.method !== 'POST' || req.headers[STOP_HEADER] !== 'true') {
        res.statusCode = 403;
        res.end();
        return;
      }
      res.end('Stopping test server');
      stop()
        .then(() => process.exit(exitCodes.success))
        .catch(err => {
          console.error(err);
          process.exit(exitCodes.error);
        });
    }
  }, config.port);
  server = new jbr.Server(runnerConfig);
  await server.start();
  console.log(`Test server running at ${serverUrl(server)}/ (append ?spec=<text> to run selected specs only)`);

  if (config.openBrowser) {
    try {
      driver = await CdpDriver.launch({headless: false});
    } catch (err) {
      console.warn(`Unable to start Chrome, open ${serverUrl(server)}/ in a browser manually.`, err.message);
    }
  }
  runSpecs();
}

/**
 * Stops a test server started with startTestServer. The port is read from the test config of the module in the current working directory.
 */
async function stopTestServer(args) {
  const config = loadConfig(args);
  if (!config) {
    console.log(`No test server stopped (config file ${scoutBuildConstants.testConfigFileName} not found)`);
    return;
  }
  const port = config.port;
  try {
    // noinspection HttpUrlsUsage
    const response = await fetch(`http://${HOST}:${port}${STOP_PATH}`, {method: 'POST', headers: {[STOP_HEADER]: 'true'}});
    console.log(await response.text());
  } catch (err) {
    console.log(`No test server running on port ${port}`);
  }
}

/**
 * Calls the function and sets the exit code of the process to {@link exitCodes.error} if it fails.
 */
async function withErrorExitCode(fn) {
  try {
    await fn();
  } catch (err) {
    console.error(err);
    process.exitCode = exitCodes.error;
  }
}

/**
 * @returns {object|null} the test config of the module in the current working directory, null if there is no config file
 */
function loadConfig(args) {
  const configFile = path.resolve(scoutBuildConstants.testConfigFileName);
  if (!fs.existsSync(configFile)) {
    return null;
  }
  const config = {webpackArgs: args.webpackArgs || {}};
  require(configFile)(config);
  // command line arguments override the values of the config file
  for (const key of ['filter', 'headless', 'openBrowser', 'port', 'junitReportDir']) {
    if (args[key] !== undefined) {
      config[key] = args[key];
    }
  }
  return config;
}

/**
 * Builds the spec bundle. In watch mode, the returned promise is resolved after the first successful build, errors are logged only.
 * @param {object} config
 * @param {function} [onRebuild] called after each successful rebuild in watch mode
 * @returns {Promise<object>} the webpack Watching instance in watch mode, null otherwise
 */
function build(config, onRebuild) {
  const webpack = require('webpack');
  // watch mode is started with compiler.watch()
  const {watch, ...webpackConfig} = config.webpack;
  const compiler = webpack(webpackConfig);
  return new Promise((resolve, reject) => {
    let firstBuild = true;
    let watching = null;
    const done = (err, stats) => {
      if (err || stats.hasErrors()) {
        console.error(err || stats.toString({all: false, errors: true, colors: true}));
        if (watch) {
          console.log('Waiting for file changes');
        } else {
          reject(new Error('Webpack build failed'));
        }
        return;
      }
      console.log(`Spec bundle built in ${stats.endTime - stats.startTime} ms`);
      if (firstBuild) {
        firstBuild = false;
        resolve(watching);
      } else if (onRebuild) {
        onRebuild();
      }
    };
    if (watch) {
      watching = compiler.watch({}, done);
    } else {
      compiler.run((err, stats) => compiler.close(() => done(err, stats)));
    }
  });
}

/**
 * Copies the helper files to the helpers directory of the test output. The file names are prefixed with their index to keep the order.
 * @returns {string[]} the names of the copied helper files in the order of loading
 */
function copyHelpers(helpers) {
  const helpersDir = path.resolve(scoutBuildConstants.outDir.target, scoutBuildConstants.outDir.distTest, scoutBuildConstants.outSubDir.testHelpers);
  fs.rmSync(helpersDir, {recursive: true, force: true});
  fs.mkdirSync(helpersDir, {recursive: true});
  return helpers.map((file, index) => {
    const fileName = `${String(index).padStart(2, '0')}-${path.basename(file)}`;
    fs.copyFileSync(file, path.join(helpersDir, fileName));
    if (fs.existsSync(file + '.map')) {
      fs.copyFileSync(file + '.map', path.join(helpersDir, path.basename(file) + '.map'));
    }
    return fileName;
  });
}

/**
 * @param {object} config the test config
 * @param {string[]} helperFiles names of the helper files in the order of loading
 * @param {object} [additionalMiddleware] map of URL paths to middleware functions
 * @param {number} [port] port of the server, 0 to use a free port
 */
function createRunnerConfig(config, helperFiles, additionalMiddleware = {}, port = 0) {
  const serveStatic = require('serve-static');
  const distTestDir = path.resolve(scoutBuildConstants.outDir.target, scoutBuildConstants.outDir.distTest);
  const middleware = {
    ...additionalMiddleware,
    [`/__jasmine__/${PRELOAD_DIR}`]: serveStatic(path.join(__dirname, 'resources'))
  };
  for (const [url, dir] of Object.entries(config.resources)) {
    // the middleware paths of jasmine-browser-runner must not end with a slash
    middleware[url.replace(/(.)\/+$/, '$1')] = serveStatic(dir);
  }
  return {
    jasmineCore: createJasmineCore(),
    projectBaseDir: distTestDir,
    srcDir: scoutBuildConstants.outSubDir.testHelpers,
    srcFiles: helperFiles.filter(f => f.endsWith('.js')),
    cssFiles: helperFiles.filter(f => f.endsWith('.css')),
    specDir: scoutBuildConstants.outSubDir.testSpec,
    specFiles: [`${scoutBuildConstants.testBundleName}.js`],
    env: config.jasmine,
    middleware,
    listenAddress: HOST,
    hostname: HOST,
    port,
    filter: config.filter
  };
}

/**
 * Returns the jasmine-core used by jasmine-browser-runner with the files of {@link PRELOAD_FILES} prepended to its scripts.
 * The server serves the scripts of jasmine-core below '/__jasmine__', the preload files are served by the middleware at '/__jasmine__/{@link PRELOAD_DIR}'.
 */
function createJasmineCore() {
  const jbrDir = path.dirname(require.resolve('jasmine-browser-runner'));
  const jasmineCore = require(require.resolve('jasmine-core', {paths: [jbrDir]}));
  return {
    ...jasmineCore,
    files: {
      ...jasmineCore.files,
      jsFiles: [...PRELOAD_FILES.map(file => `${PRELOAD_DIR}/${file}`), ...jasmineCore.files.jsFiles]
    }
  };
}

async function runWithChrome(jbr, runnerConfig, reporters, headless) {
  const server = new jbr.Server(runnerConfig);
  await server.start();
  let driver = null;
  const removeTerminationHandler = onTermination(async () => {
    if (driver) {
      await driver.close();
    }
  });
  try {
    driver = await CdpDriver.launch({headless});
    const runner = new jbr.Runner({webdriver: driver, reporters, host: serverUrl(server)});
    console.log('Running tests in the browser...');
    return await runner.run(runnerConfig);
  } finally {
    removeTerminationHandler();
    if (driver) {
      await driver.close();
    }
    await server.stop();
  }
}

/**
 * Calls the cleanup function and exits the process when SIGINT (Ctrl+C) or SIGTERM is received. A second signal exits immediately.
 * @param {function(): Promise} cleanup
 * @returns {function} removes the signal handlers
 */
function onTermination(cleanup) {
  let terminating = false;
  const handler = signal => {
    const exitCode = 128 + os.constants.signals[signal];
    if (terminating) {
      process.exit(exitCode);
    }
    terminating = true;
    console.log(`Received ${signal}, cleaning up`);
    cleanup()
      .catch(err => console.error(err))
      .finally(() => process.exit(exitCode));
  };
  process.on('SIGINT', handler);
  process.on('SIGTERM', handler);
  return () => {
    process.off('SIGINT', handler);
    process.off('SIGTERM', handler);
  };
}

function createConsoleReporter(jbr) {
  const reporter = new jbr.ConsoleReporter();
  reporter.configure({});
  return reporter;
}

function serverUrl(server) {
  return `${server.scheme()}://${HOST}:${server.port()}`;
}

function computeExitCode(result, junitReporter) {
  if (result.overallStatus === 'passed') {
    return exitCodes.success;
  }
  const loadError = (result.failedExpectations || []).some(f => f.globalErrorType === 'load');
  if (result.overallStatus === 'failed' && !loadError && junitReporter.stats.total > 0) {
    return exitCodes.testFailures;
  }
  return exitCodes.error;
}

module.exports = {
  runTests,
  startTestServer: args => withErrorExitCode(() => startTestServer(args)),
  stopTestServer: args => withErrorExitCode(() => stopTestServer(args))
};
