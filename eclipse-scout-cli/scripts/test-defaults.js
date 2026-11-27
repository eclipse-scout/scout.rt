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
const path = require('path');
const scoutBuildConstants = require('./constants');
const KeepNonceVariablePlugin = require('./webpack-build/KeepNonceVariablePlugin');
const {findTsLoaderOptions} = require('./webpack-defaults');
const {SourceMapDevToolPlugin} = require('webpack');

/**
 * Default test configuration of a Scout module. Call it from the 'test.config.js' of the module:
 *
 * <pre>
 * const baseConfig = require('@eclipse-scout/cli/scripts/test-defaults');
 * module.exports = config => {
 *   baseConfig(config, './src/test/js/test-index.ts');
 *   config.resources['/_res/'] = path.resolve('src/test/resources/_res'); // optional: serve static test resources
 * };
 * </pre>
 *
 * Properties of the config object:
 * <ul>
 *   <li>webpackArgs: webpack arguments (input, may be passed on the command line using --webpackArgs.xy)</li>
 *   <li>webpack: webpack configuration used to build the spec bundle</li>
 *   <li>specEntryPoint: absolute path of the spec entry point (e.g. test-index.ts)</li>
 *   <li>helpers: absolute paths of JS and CSS files loaded in the browser before the spec bundle</li>
 *   <li>resources: map of URL path prefixes to directories served by the test server</li>
 *   <li>jasmine: Jasmine environment configuration</li>
 *   <li>headless: whether Chrome is started headless by test:ci</li>
 *   <li>openBrowser: whether test-server:start opens Chrome</li>
 *   <li>port: port of the test server for test-server:start/stop</li>
 *   <li>junitReportDir: directory of the JUnit report (test-results.xml)</li>
 * </ul>
 *
 * @param {object} config the config object to fill
 * @param {string} [specEntryPoint] path of the spec entry point. Default: test/test-index.ts or test/test-index.js
 */
module.exports = (config, specEntryPoint) => {
  const webpackConfigFilePath = path.resolve('webpack.config.js');
  if (!fs.existsSync(webpackConfigFilePath)) {
    const message = 'Tests require a webpack config file at location "' + webpackConfigFilePath + '" but it could not be found.';
    console.error(message);
    throw new Error(message);
  }
  const webpackConfigProvider = require(webpackConfigFilePath);

  const webpackArgs = Object.assign({
    mode: scoutBuildConstants.mode.development,
    watch: false
  }, config.webpackArgs);
  let webpackConfig = webpackConfigProvider(null, webpackArgs);
  if (Array.isArray(webpackConfig)) {
    webpackConfig = webpackConfig[0];
  }

  const tsLoaderOptions = findTsLoaderOptions(webpackConfig);
  if (tsLoaderOptions) {
    tsLoaderOptions.compilerOptions = {
      ...tsLoaderOptions.compilerOptions,
      // No need to create declarations for tests
      declaration: false,
      declarationMap: false,
      // The spec bundle also contains the TypeScript sources of linked packages (e.g. in a monorepo), the file system root contains all of them
      rootDir: path.parse(process.cwd()).root
    };
  }

  if (webpackConfig.optimization) {
    // create a single spec bundle
    delete webpackConfig.optimization.splitChunks;
    delete webpackConfig.optimization.runtimeChunk;
  }

  // Don't create a library, it would create an error during test run (module not found)
  delete webpackConfig.output?.library;
  delete webpackConfig.experiments?.outputModule;

  if (webpackConfig.externals) {
    // Remove externals, so they don't have to be provided.
    // Add jquery as the only external, it is loaded as helper.
    webpackConfig.externals = {
      'jquery': 'global jQuery'
    };
  }

  const resolvedSpecEntryPoint = searchSpecEntryPoint(specEntryPoint);
  webpackConfig.entry = {[scoutBuildConstants.testBundleName]: resolvedSpecEntryPoint};

  // use a separate output directory so that the regular output directories are not polluted with test artifacts
  webpackConfig.output = webpackConfig.output || {};
  webpackConfig.output.path = path.resolve(scoutBuildConstants.outDir.target, scoutBuildConstants.outDir.distTest, scoutBuildConstants.outSubDir.testSpec);
  webpackConfig.output.filename = '[name].js';
  webpackConfig.output.clean = true;
  webpackConfig.watch = !!webpackArgs.watch;

  const sourceMapPlugin = webpackConfig.plugins.find(plugin => plugin instanceof SourceMapDevToolPlugin);
  if (sourceMapPlugin) {
    // inline source maps, used by stackTraceMapper.js
    delete sourceMapPlugin.sourceMapFilename;
  }

  // nonce-variable must always be processed as the build-output is used in the browser
  webpackConfig.plugins = webpackConfig.plugins.filter(p => !(p instanceof KeepNonceVariablePlugin));

  const helpers = [
    require.resolve('jasmine-ajax'),
    require.resolve('jquery'),
    require.resolve('sourcemapped-stacktrace'),
    path.join(__dirname, 'test-support', 'resources', 'stackTraceMapper.js'),
    path.join(__dirname, 'test-support', 'resources', 'JasmineScout.css')
  ];

  const scoutExternals = findScoutExternals();
  if (scoutExternals.length) {
    for (const external of scoutExternals) {
      // mark scout external as webpack external and load the prebuilt file as helper
      webpackConfig.externals ||= {};
      webpackConfig.externals[external.moduleName] = 'scout';
      helpers.push(external.path);
    }
  }

  Object.assign(config, {
    webpack: webpackConfig,
    specEntryPoint: resolvedSpecEntryPoint,
    helpers,
    resources: {},
    jasmine: {
      random: false
    },
    headless: true,
    openBrowser: true,
    port: 9876,
    junitReportDir: 'test-results'
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

function findScoutExternals() {
  const esmJsFileExtension = '.esm.js';
  const jsFileExtension = '.js';

  // check whether scout browser build exists
  const scoutEsmPath = require.resolve('@eclipse-scout/core');
  if (!scoutEsmPath.endsWith(esmJsFileExtension)) {
    return [];
  }
  const scoutExternalPath = scoutEsmPath.substring(0, scoutEsmPath.length - esmJsFileExtension.length) + jsFileExtension;
  if (!fs.existsSync(scoutExternalPath)) {
    return [];
  }

  // check whether testing browser build exists
  const testingEsmPath = require.resolve('@eclipse-scout/core/testing');
  if (!testingEsmPath.endsWith(esmJsFileExtension)) {
    return [];
  }
  const testingExternalPath = testingEsmPath.substring(0, testingEsmPath.length - esmJsFileExtension.length) + jsFileExtension;
  if (!fs.existsSync(testingExternalPath)) {
    return [];
  }

  return [
    {path: scoutExternalPath, moduleName: '@eclipse-scout/core'},
    {path: testingExternalPath, moduleName: '@eclipse-scout/core/testing'}
  ];
}
