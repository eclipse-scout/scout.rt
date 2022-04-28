/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
const CopyPlugin = require('copy-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const AfterEmitWebpackPlugin = require('./AfterEmitWebpackPlugin');

const path = require('path');
const scoutBuildConstants = require('./constants');
const webpack = require('webpack');

/**
 * @param {string} args.mode development or production
 * @param {boolean} args.clean true, to clean the dist folder before each build. Default is true.
 * @param {boolean} args.progress true, to show build progress in percentage. Default is true.
 * @param {boolean} args.profile true, to show timing information for each build step. Default is false.
 * @param {[]} args.resDirArray an array containing directories which should be copied to dist/res
 */
module.exports = (env, args) => {
  const {devMode, outSubDir, cssFilename, jsFilename} = scoutBuildConstants.getConstantsForMode(args.mode);
  const isMavenModule = scoutBuildConstants.isMavenModule();
  const outDir = getOutputDir(isMavenModule, outSubDir);
  const resDirArray = args.resDirArray || ['res'];
  console.log(`Webpack mode: ${args.mode}`);

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

  const config = {
    target: 'web',
    mode: args.mode,
    // In dev mode 'inline-source-map' is used (devtool is false because we use SourceMapDevToolPlugin)
    // Other source map types may increase build performance but decrease debugging experience
    // (e.g. wrong this in arrow functions with inline-cheap-module-source-map or not having original source code at all (code after babel transpilation instead of before) with eval types).
    // In production mode create external source maps without source code to map stack traces.
    // Otherwise stack traces would point to the minified source code which makes it quite impossible to analyze productive issues.
    devtool: devMode ? false : 'nosources-source-map',
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
      }
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
      devtoolModuleFilenameTemplate: devMode ? undefined : prodDevtoolModuleFilenameTemplate,
      clean: nvl(args.clean, true)
    },
    performance: {
      hints: false
    },
    profile: args.profile,
    module: {
      // LESS
      rules: [{
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
        // # Babel
        test: /\.m?js$/,
        exclude: [],
        use: {
          loader: require.resolve('babel-loader'),
          options: {
            compact: false,
            cacheDirectory: true,
            cacheCompression: false,
            plugins: [
              require.resolve('@babel/plugin-transform-object-assign'),
              require.resolve('@babel/plugin-proposal-class-properties')],
            presets: [
              [require.resolve('@babel/preset-env'), {
                debug: false,
                targets: {
                  firefox: '69',
                  chrome: '71',
                  safari: '12.1'
                }
              }]
            ]
          }
        }
      }, {
        // to support css imports (currently not used by Scout but might be used by included 3rd party libs)
        test: /\.css$/i,
        use: [{
          loader: require.resolve('style-loader')
        }, {
          loader: require.resolve('css-loader'),
          options: {
            sourceMap: devMode,
            modules: false, // We don't want to work with CSS modules
            url: false // Don't resolve URLs in LESS, because relative path does not match /res/fonts
          }
        }]
      }]
    },
    plugins: [
      // see: extracts css into separate files
      new MiniCssExtractPlugin({filename: cssFilename}),
      // run post-build script hook
      new AfterEmitWebpackPlugin({outDir: outDir})
    ],
    optimization: {
      splitChunks: {
        chunks: 'all',
        name: (module, chunks, cacheGroupKey) => computeChunkName(module, chunks, cacheGroupKey)
      }
    }
  };

  // # Copy resources
  if (copyPluginConfig.length > 0) {
    // only add the plugin if there are resources to copy. Otherwise the plugin fails.
    config.plugins.push(new CopyPlugin({patterns: copyPluginConfig}));
  }

  if (nvl(args.progress, true)) {
    // Shows progress information in the console in dev mode
    const webpack = require('webpack');
    config.plugins.push(new webpack.ProgressPlugin({profile: args.profile}));
  }

  if (!devMode) {
    const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');
    const TerserPlugin = require('terser-webpack-plugin');
    config.optimization.minimizer = [
      // minify css
      new CssMinimizerPlugin({
        test: /\.min\.css$/g,
        minimizerOptions: {
          preset: ['default', {
            discardComments: {removeAll: true}
          }]
        }
      }),
      // minify js
      new TerserPlugin({
        test: /\.js(\?.*)?$/i,
        parallel: 4
      })
    ];
  }

  if (devMode) {
    // Use external source maps also in dev mode because the browser is very slow in displaying a file containing large lines which is the case if source maps are inlined
    config.plugins.push(new webpack.SourceMapDevToolPlugin({
      filename: '[file].map'
    }));
  }

  return config;
};

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

function getOutputDir(isMavenModule, outSubDir) {
  if (isMavenModule) {
    return path.resolve(scoutBuildConstants.outDir.target, scoutBuildConstants.outDir.dist, outSubDir);
  }
  return path.resolve(scoutBuildConstants.outDir.dist);
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

module.exports.addThemes = addThemes;
