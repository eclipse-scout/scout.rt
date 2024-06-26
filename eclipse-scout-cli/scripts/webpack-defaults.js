/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
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
// FIXME mvi [js-bookmark] enable transformer when fully tested
// const transformers = require('./transformers');
const CopyPlugin = require('copy-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const AfterEmitWebpackPlugin = require('./AfterEmitWebpackPlugin');
const {SourceMapDevToolPlugin, WatchIgnorePlugin, ProgressPlugin} = require('webpack');

/**
 * @param {string} args.mode development or production
 * @param {boolean} args.clean true, to clean the dist folder before each build. Default is false.
 * @param {boolean} args.progress true, to show build progress in percentage. Default is true.
 * @param {boolean} args.profile true, to show timing information for each build step. Default is false.
 * @param {boolean} args.watch true, if webpack runs in watch mode. Default is false.
 * @param {[]} args.resDirArray an array containing directories which should be copied to dist/res
 * @param {object} args.tsOptions a config object to be passed to the ts-loader
 * @param {boolean|'fork'} args.typeCheck
 *    true: let the TypeScript compiler check the types.
 *    false: let the TypeScript compiler only transpile the TypeScript code without checking types, which makes it faster.
 *    fork: starts a separate process to run the type checks so that the build won't be blocked until type check completes.
 *          Also shows a notification if type check fails. This mode needs more memory.
 *    auto:
 *    - In prod mode, types won't be checked (typeCheck is false).
 *    - In dev mode, types will be checked (typeCheck is true).
 *    - In watch mode: types will be checked in a separate process (typeCheck is fork).
 */
module.exports = (env, args) => {
  const buildMode = args.mode;
  const {devMode, cssFilename, jsFilename} = scoutBuildConstants.getConstantsForMode(buildMode);
  const isMavenModule = scoutBuildConstants.isMavenModule();
  const isWatchMode = nvl(args.watch, false);
  const outDir = scoutBuildConstants.getOutputDir(buildMode);
  const resDirArray = args.resDirArray || ['res'];
  let typeCheck = computeTypeCheck(args.typeCheck, devMode, isWatchMode);
  console.log(`Webpack mode: ${buildMode}`);
  if (isWatchMode) {
    console.log('File watching enabled');
  }
  console.log(`Type check: ${typeCheck}`);

  // # Copy static web-resources delivered by the modules
  const copyPluginConfig = [];
  const copyTarget = isMavenModule ? '../res' : '.';
  for (const resDir of resDirArray) {
    copyPluginConfig.push(
      {
        from: resDir,
        to: copyTarget
      });
  }

  const minimizerTarget = ['firefox69', 'chrome71', 'safari13'];
  const babelOptions = {
    compact: false,
    cacheDirectory: true,
    cacheCompression: false,
    presets: [
      [require.resolve('@babel/preset-env'), {
        debug: false,
        targets: {
          firefox: '69',
          chrome: '71',
          safari: '13'
        }
      }]
    ]
  };

  const transpileOnly = typeCheck === 'fork' ? true : !typeCheck;
  const tsOptions = {
    ...args.tsOptions,
    transpileOnly: transpileOnly,
    compilerOptions: {
      noEmit: false,
      ...args.tsOptions?.compilerOptions
    }/* ,
    // FIXME mvi [js-bookmark] enable transformer when fully tested
    getCustomTransformers: program => ({before: [ctx => transformers.dataObjectTransformer(program, ctx)]})*/
  };

  const config = {
    mode: buildMode,
    devtool: false, // disabled because SourceMapDevToolPlugin is used (see below)
    ignoreWarnings: [(webpackError, compilation) => isWarningIgnored(devMode, webpackError, compilation)],
    resolve: {
      // no automatic polyfills. clients must add the desired polyfills themselves.
      fallback: {
        assert: false,
        buffer: false,
        console: false,
        constants: false,
        crypto: false,
        domain: false,
        events: false,
        http: false,
        https: false,
        os: false,
        path: false,
        punycode: false,
        process: false,
        querystring: false,
        stream: false,
        string_decoder: false,
        sys: false,
        timers: false,
        tty: false,
        url: false,
        util: false,
        vm: false,
        zlib: false
      },
      extensions: ['.ts', '.js', '.json', '.wasm', '.tsx', '.jsx']
    },
    // expect these apis in the browser
    externals: {
      'crypto': 'crypto',
      'canvas': 'canvas',
      'fs': 'fs',
      'http': 'http',
      'https': 'https',
      'url': 'url',
      'zlib': 'zlib'
    },
    output: {
      filename: jsFilename,
      path: outDir,
      clean: nvl(args.clean, false)
    },
    performance: {
      hints: false
    },
    profile: args.profile,
    module: {
      rules: [{
        // LESS
        test: /\.less$/,
        use: [{
          // Extracts CSS into separate files. It creates a CSS file per JS file which contains CSS.
          // It supports On-Demand-Loading of CSS and SourceMaps.
          // see: https://webpack.js.org/plugins/mini-css-extract-plugin/
          //
          // Note: this creates some useless *.js files, like dark-theme.js
          // This seems to be an issue in webpack, workaround is to remove the files later
          // see: https://github.com/webpack-contrib/mini-css-extract-plugin/issues/151
          // seems to be fixed in webpack 5, workaround to manually delete js files can be removed as soon as webpack 5 is released
          loader: MiniCssExtractPlugin.loader
        }, {
          // Interprets @import and url() like import/require() and will resolve them.
          // see: https://webpack.js.org/loaders/css-loader/
          loader: require.resolve('css-loader'),
          options: {
            sourceMap: devMode,
            modules: false, // We don't want to work with CSS modules
            url: false // Don't resolve URLs in LESS, because relative path does not match /res/fonts
          }
        }, {
          // Compiles Less to CSS.
          // see: https://webpack.js.org/loaders/less-loader/
          loader: require.resolve('less-loader'),
          options: {
            sourceMap: devMode,
            lessOptions: {
              relativeUrls: false,
              rewriteUrls: 'off',
              math: 'always'
            }
          }
        }]
      }, {
        test: /\.tsx?$/,
        exclude: /node_modules/,
        use: [{
          loader: require.resolve('babel-loader'),
          options: babelOptions
        }, {
          loader: require.resolve('ts-loader'),
          options: tsOptions
        }]
      }, {
        test: /\.jsx?$/,
        use: [{
          loader: require.resolve('babel-loader'),
          options: babelOptions
        }]
      }, {
        test: /\.jsx?$/,
        enforce: 'pre',
        use: [{
          loader: require.resolve('source-map-loader')
        }]
      }]
    },
    plugins: [
      new WatchIgnorePlugin({paths: [/\.d\.ts$/]}),
      // see: extracts css into separate files
      new MiniCssExtractPlugin({filename: cssFilename}),
      // run post-build script hook
      new AfterEmitWebpackPlugin({outDir: outDir}),
      new SourceMapDevToolPlugin({
        // Use external source maps in all modes because the browser is very slow in displaying a file containing large lines which is the case if source maps are inlined
        filename: '[file].map',

        // Don't create maps for static resources.
        // They may already have maps which could lead to "multiple assets emit different content to the same file" exception.
        exclude: /\/res\/.*/,

        // In production mode create external source maps without source code to map stack traces.
        // Otherwise, stack traces would point to the minified source code which makes it quite impossible to analyze productive issues.
        noSources: !devMode,
        moduleFilenameTemplate: devMode ? undefined : prodDevtoolModuleFilenameTemplate
      })
    ],
    optimization: {
      splitChunks: {
        chunks: 'all',
        name: (module, chunks, cacheGroupKey) => computeChunkName(module, chunks, cacheGroupKey)
      }
    }
  };

  // Copy resources only add the plugin if there are resources to copy. Otherwise, the plugin fails.
  if (copyPluginConfig.length > 0) {
    config.plugins.push(new CopyPlugin({patterns: copyPluginConfig}));
  }

  // Shows progress information in the console in dev mode
  if (nvl(args.progress, true)) {
    config.plugins.push(new ProgressPlugin({profile: args.profile}));
  }

  if (typeCheck === 'fork') {
    // perform type checks asynchronously in a separate process
    const ForkTsCheckerWebpackPlugin = require('fork-ts-checker-webpack-plugin');
    const ForkTsCheckerNotifierWebpackPlugin = require('fork-ts-checker-notifier-webpack-plugin');

    let forkTsCheckerConfig = {
      typescript: {
        memoryLimit: 4096
      }
    };
    if (!fs.existsSync('./tsconfig.json')) {
      // if the module has no tsconfig: use default from Scout.
      // Otherwise, each module would need to provide a tsconfig even if there is no typescript code in the module.
      forkTsCheckerConfig = {
        typescript: {
          ...forkTsCheckerConfig.typescript,
          configFile: require.resolve('@eclipse-scout/tsconfig'),
          context: process.cwd(),
          configOverwrite: {
            compilerOptions: {skipLibCheck: true, sourceMap: false, inlineSourceMap: false, declarationMap: false, allowJs: true},
            include: isMavenModule ? ['./src/main/js/**/*.ts', './src/main/js/**/*.js', './src/test/js/**/*.ts', './src/test/js/**/*.js']
              : ['./src/**/*.ts', './src/**/*.js', './test/**/*.ts', './test/**/*.js']
          }
        }
      };
    }
    config.plugins.push(new ForkTsCheckerWebpackPlugin(forkTsCheckerConfig));
    config.plugins.push(new ForkTsCheckerNotifierWebpackPlugin({
      title: getModuleName(),
      skipSuccessful: true, // no notification for successful builds
      excludeWarnings: true // no notification for warnings
    }));
  }

  if (!devMode) {
    const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');
    const TerserPlugin = require('terser-webpack-plugin');
    config.optimization.minimizer = [
      // minify css
      new CssMinimizerPlugin({
        test: /\.min\.css$/i, // only minimize required files
        exclude: /res[\\/]/i, // exclude resources output directory from minimizing as these files are copied
        parallel: 4, // best ratio between memory consumption and performance on most systems
        minify: CssMinimizerPlugin.esbuildMinify,
        minimizerOptions: {
          logLevel: 'error', // show messages directly to see the details. The message passed to webpack is only an object which is ignored in isWarningIgnored
          sourcemap: false, // no sourcemaps for css in prod build (needs more heap memory instead)
          charset: 'utf8', // default is ASCII which requires more escaping. UTF-8 allows for more compact code.
          target: minimizerTarget
        }
      }),
      // minify js
      new TerserPlugin({
        test: /\.min\.js$/i, // only minimize required files
        exclude: [/log4javascript-1\.4\.9[\\/]/i, /res[\\/]/i], // exclude resources output directory from minimizing as these files are copied
        parallel: 4, // best ratio between memory consumption and performance on most systems
        minify: TerserPlugin.esbuildMinify,
        terserOptions: {
          legalComments: 'none',
          logLevel: 'error', // show messages directly to see the details. The message passed to webpack is only an object which is ignored in isWarningIgnored
          charset: 'utf8', // default is ASCII which requires more escaping. UTF-8 allows for more compact code.
          target: minimizerTarget
        }
      })
    ];
  }

  return config;
};

/**
 * Creates a new object that contains the same keys as the given object. The values are replaced with the keys.
 * So the resulting object looks like: {key1: key1, key2: key2}.
 */
function toExternals(src, dest) {
  if (!src) {
    return;
  }
  return Object.keys(src).reduce((obj, current) => {
    obj[current] = current;
    return obj;
  }, dest);
}

/**
 * Converts the given base config to a library config meaning that all dependencies declared in the package.json are externalized by default.
 *
 * @param {object} config base config to convert to a library config
 * @param {object} [options]
 * @param {object} [options.externals] object holding custom externals for the module. See https://webpack.js.org/configuration/externals/ for details about supported formats and types.
 * @param {boolean} [options.externalizeDevDeps] Add devDependencies as externals. Default is true.
 * @param {boolean} [options.externalizePeerDeps] Add peerDependencies as externals. Default is true.
 * @param {boolean} [options.externalizeBundledDeps] Add bundledDependencies as externals. Default is true.
 * @param {boolean} [options.externalizeOptionalDeps] Add optionalDependencies as externals. Default is true.
 */
function libraryConfig(config, options = {}) {
  const packageJson = require(path.resolve('./package.json'));
  const packageJsonExternals = {};
  toExternals(packageJson.dependencies, packageJsonExternals);
  if (options.externalizeDevDeps ?? true) {
    toExternals(packageJson.devDependencies, packageJsonExternals);
  }
  if (options.externalizePeerDeps ?? true) {
    toExternals(packageJson.peerDependencies, packageJsonExternals);
  }
  if (options.externalizeBundledDeps ?? true) {
    toExternals(packageJson.bundledDependencies, packageJsonExternals);
  }
  if (options.externalizeOptionalDeps ?? true) {
    toExternals(packageJson.optionalDependencies, packageJsonExternals);
  }
  packageJsonExternals.jquery = 'commonjs jquery'; // Make synthetic default import work (import $ from 'jquery') by importing jquery as commonjs module
  const customExternals = options.externals || {};
  const allExternals = {...packageJsonExternals, ...config.externals, ...customExternals};

  // FileList is not necessary in library mode
  let plugins = config.plugins.map(plugin => {
    if (plugin instanceof AfterEmitWebpackPlugin) {
      return new AfterEmitWebpackPlugin({outDir: plugin.options.outDir, createFileList: false});
    }
    return plugin;
  });

  return {
    ...config,
    optimization: {
      ...config.optimization,
      splitChunks: undefined // disable splitting
    },
    output: {
      ...config.output,
      library: {
        type: 'module'
      }
    },
    experiments: {
      // required for library.type = 'module'
      outputModule: true
    },
    plugins,
    externals: (context, callback) => markExternals(allExternals, context, callback)
  };
}

function markExternals(allExternals, context, callback) {
  const request = context.request;
  if (request.startsWith('.')) {
    // fast check: continue without externalizing the import for relative paths
    return callback();
  }

  if (allExternals[request]) {
    // import matches exactly a declared dependency
    return callback(null, allExternals[request]);
  }

  // check for files in sub-folders of an external
  for (const [key, value] of Object.entries(allExternals)) {
    if (request.startsWith(key + '/')) {
      let result = request;
      let spacePos = value.indexOf(' ');
      if (spacePos > 0) {
        result = value.substring(0, spacePos + 1) + result;
      }
      return callback(null, result);
    }
  }

  callback(); // Continue without externalizing the import
}

/**
 * @param {object} entry the webpack entry object
 * @param {object} options the options object to configure which themes should be built and how
 * @param {[string]} options.themes one or more themes of the availableThemes that should be built. Use 'all' to build all available themes, or 'none' to build no themes. Default is 'all'.
 * @param {[string]} options.availableThemes the themes that are available.
 * @param {function} options.generator a function that returns an array containing the key and value for the generated entry. The function will be called for each theme with the theme name as argument.
 */
function addThemes(entry, options = {}) {
  let themes = ensureArray(nvl(options.themes, 'all'));
  let availableThemes = options.availableThemes;
  if (!availableThemes) {
    throw 'Please specify the availableThemes';
  }
  let generator = options.generator;
  if (!generator) {
    throw 'Please specify a theme entry generator (themeEntryGen) that returns and array containing the key and value of the entry to generate for each theme.';
  }
  if (themes.includes('all')) {
    themes = availableThemes;
  }
  themes = themes.filter(theme => availableThemes.includes(theme));
  if (themes.length === 0) {
    return;
  }
  console.log(`Themes: ${themes}`);
  themes.forEach(theme => {
    let name = theme === 'default' ? '' : `-${theme}`;
    let [key, value] = generator(name);
    entry[key] = value;
  });
}

function computeChunkName(module, chunks, cacheGroupKey) {
  const entryPointDelim = '~';
  const allChunksNames = chunks
    .map(chunk => chunk.name)
    .filter(chunkName => !!chunkName)
    .join(entryPointDelim);
  let fileName = cacheGroupKey === 'defaultVendors' ? 'vendors' : cacheGroupKey;

  if (allChunksNames.length < 1) {
    // there is no chunk name (e.g. lazy loaded module): derive chunk-name from filename
    const segmentDelim = '-';
    if (fileName.length > 0) {
      fileName += segmentDelim;
    }
    return fileName + computeModuleId(module);
  }

  if (fileName.length > 0) {
    fileName += entryPointDelim;
  }
  return fileName + allChunksNames;
}

function computeModuleId(module) {
  const nodeModules = 'node_modules';
  // noinspection JSUnresolvedVariable
  let id = module.userRequest;
  const nodeModulesPos = id.lastIndexOf(nodeModules);
  if (nodeModulesPos < 0) {
    // use file name
    id = path.basename(id, '.js');
  } else {
    // use js-module name
    id = id.substring(nodeModulesPos + nodeModules.length + path.sep.length);
    let end = id.indexOf(path.sep);
    if (end >= 0) {
      if (id.startsWith('@')) {
        const next = id.indexOf(path.sep, end + 1);
        if (next >= 0) {
          end = next;
        }
      }
      id = id.substring(0, end);
    }
  }

  return id.replace(/[/\\\-@:_.|]+/g, '').toLowerCase();
}

function ensureArray(array) {
  if (array === undefined || array === null) {
    return [];
  }
  if (Array.isArray(array)) {
    return array;
  }
  const isIterable = typeof array[Symbol.iterator] === 'function' && typeof array !== 'string';
  if (isIterable) {
    return Array.from(array);
  }
  return [array];
}

function nvl(arg, defaultValue) {
  if (arg === undefined || arg === null) {
    return defaultValue;
  }
  return arg;
}

function isWarningIgnored(devMode, webpackError) {
  if (devMode || !webpackError) {
    return false;
  }
  // Ignore warnings from esbuild minifier.
  // One warning is 'Converting "require" to "esm" is currently not supported' which is not of interest.
  // Others may be created by third party libs which are not of interest as well.
  return webpackError.name === 'Warning';
}

/**
 * Don't reveal absolute file paths in production mode -> only return the file name relative to its module.
 * @param info.resourcePath
 */
function prodDevtoolModuleFilenameTemplate(info) {
  let path = info.resourcePath || '';
  // Search for the last /src/ in the path and return the fragment starting from its parent
  let result = path.match(/.*\/(.*\/src\/.*)/);
  if (result) {
    return result[1];
  }
  // Match everything after the /last node_modules/ in the path
  result = path.match(/.*\/node_modules\/(.*)/);
  if (result) {
    return result[1];
  }
  // Return only the file name (the part after the last /)
  result = path.match(/([^/\\]*)$/);
  if (result) {
    return result[1];
  }
}

function getModuleName() {
  let packageJsonFile = path.resolve('./package.json');
  if (fs.existsSync(packageJsonFile)) {
    let name = require(packageJsonFile).name;
    if (name) {
      return name;
    }
  }
  return path.basename(process.cwd());
}

/**
 * Externalize every import to the main index and replace it with newImport
 * Keep imports to the excludedFolder.
 * @param {string} newImport new name of the replaced import, typically the module name
 * @param {string} excludedFolder imports to that folder won't be replaced
 * @returns a function that should be added to the webpack externals
 */
function rewriteIndexImports(newImport, excludedFolder) {
  // If an import ends by one of these names, it is considered an index import.
  let indexImports = ['index', 'main/js'];

  return ({context, request, contextInfo}, callback) => {
    // Externalize every import to the main index and replace it with newImport
    // Keep imports pointing to excludedFolder
    if (isIndexImport(request) && !path.resolve(context, request).includes(excludedFolder)) {
      return callback(null, newImport);
    }

    // Continue without externalizing the import
    callback();
  };

  function isIndexImport(path) {
    for (let imp of indexImports) {
      if (path.endsWith(imp)) {
        return true;
      }
    }
    return false;
  }
}

function computeTypeCheck(typeCheck, devMode, watchMode) {
  typeCheck = nvl(typeCheck, 'auto');
  if (typeCheck !== 'auto' && typeCheck !== 'fork') {
    typeCheck = typeCheck.toLowerCase() === 'true';
  }
  if (typeCheck !== 'auto') {
    return typeCheck;
  }
  if (!devMode) {
    return false;
  }
  if (watchMode) {
    return 'fork';
  }
  return true;
}

module.exports.addThemes = addThemes;
module.exports.libraryConfig = libraryConfig;
module.exports.markExternals = markExternals;
module.exports.rewriteIndexImports = rewriteIndexImports;
