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
const {CleanWebpackPlugin} = require('clean-webpack-plugin');
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
  const outDir = path.resolve(scoutBuildConstants.outDir, outSubDir);
  const resDirArray = args.resDirArray || ['res'];
  console.log(`Webpack mode: ${args.mode}`);

  // # Copy static web-resources delivered by the modules
  const copyPluginConfig = [];
  for (const resDir of resDirArray) {
    copyPluginConfig.push(
      {
        from: resDir,
        to: '../res'
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
    output: {
      filename: jsFilename,
      path: outDir,
      libraryTarget: 'umd',
      globalObject: 'this',
      umdNamedDefine: true,
      devtoolModuleFilenameTemplate: devMode ? undefined : prodDevtoolModuleFilenameTemplate
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
            relativeUrls: false, // deprecated in future rewriteUrls is used
            rewriteUrls: 'off'
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
                  firefox: '35',
                  chrome: '40',
                  ie: '11',
                  edge: '12',
                  safari: '8'
                }
              }]
            ]
          }
        }
      }]
    },
    plugins: [
      // see: extracts css into separate files
      new MiniCssExtractPlugin({
        filename: cssFilename
      }),
      // run post-build script hook
      new AfterEmitWebpackPlugin({
        createFileList: !devMode,
        outDir: outDir
      }),
      // # Copy resources
      new CopyPlugin(copyPluginConfig)
    ],
    optimization: {
      splitChunks: {
        chunks: 'all',
        cacheGroups: {
          // Disable default behaviour so that only the junks defined below and the entry points are built.
          // This makes it easier if the dependencies are added manually to the html document because it is clear what the output will be (= it is clear which bundles will be created).
          // It is always possible to disable or change the Scout defaults by deleting or extending the Scout cache groups (e.g. delete config.optimization.splitChunks.cacheGroups.default).
          default: false,
          scout: {
            // Scout may be loaded as node module or may be part of the workspace
            // Also make sure the regex only matches *.js files to prevent the output from mixing with css
            // sourcemapped-stacktrace is included as well because it is not worth it to have a separate bundle for it.
            // Adding it to vendors.js would force the users to add the vendors.js to login.html as well, which may not be desired if they have custom third party libs not required by the login.html.
            test: /[\\/]node_modules[\\/]@eclipse-scout[\\/].*\.js|[\\/]eclipse-scout-core[\\/].*\.js|[\\/]org.eclipse.scout.rt.svg.ui.html[\\/].*\.js|[\\/]node_modules[\\/]sourcemapped-stacktrace[\\/].*\.js/,
            name: 'eclipse-scout',
            priority: -5,
            reuseExistingChunk: true,
            enforce: true
          },
          jquery: {
            test: /[\\/]node_modules[\\/]jquery[\\/]/,
            name: 'jquery',
            priority: -1,
            reuseExistingChunk: true,
            enforce: true
          },
          vendors: {
            /**
             * @param module.nameForCondition
             */
            test: module => {
              if (!module.nameForCondition) {
                return false; // raw or external modules do not have the method
              }
              const nameForCondition = module.nameForCondition();
              // Extract all other node_modules into vendors.js
              if (!nameForCondition.match(/[\\/]node_modules[\\/].*\.js/)) {
                return false;
              }
              // Exclude the webpack module to make sure the vendors.js only contains "real" runtime third party dependencies necessary for the actual application.
              // So, for a simple Scout app without dependencies, vendors.js won't be generated. This is also important for login.html and logout.html which don't require vendors.js as well.
              return !nameForCondition.match(/[\\/]node_modules[\\/]webpack[\\/]/);
            },
            name: 'vendors',
            priority: -10,
            reuseExistingChunk: true,
            enforce: true
          }
        }
      }
    }
  };

  if (nvl(args.progress, true)) {
    // Shows progress information in the console in dev mode
    const webpack = require('webpack');
    config.plugins.push(new webpack.ProgressPlugin({
      profile: args.profile
    }));
  }

  if (!devMode) {
    const OptimizeCssAssetsPlugin = require('optimize-css-assets-webpack-plugin');
    const TerserPlugin = require('terser-webpack-plugin');
    config.optimization.minimizer = [
      // minify css
      new OptimizeCssAssetsPlugin({
        assetNameRegExp: /\.min\.css$/g,
        cssProcessorPluginOptions: {
          preset: ['default', {
            discardComments: {removeAll: true}
          }]
        }
      }),
      // minify js
      new TerserPlugin({
        test: /\.js(\?.*)?$/i,
        cache: true,
        parallel: true
      })
    ];
  }

  if (nvl(args.clean, true)) {
    // see: https://webpack.js.org/guides/output-management/#cleaning-up-the-dist-folder
    config.plugins.push(new CleanWebpackPlugin());
  }

  if (devMode) {
    // Generating source maps for vendors.js may take some time and is not necessary
    // Use external source maps also in dev mode because the browser is very slow in displaying a file containing large lines which is the case if source maps are inlined
    config.plugins.push(new webpack.SourceMapDevToolPlugin({
      filename: '[file].map',
      exclude: ['vendors.js', 'jquery.js']
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

function ensureArray(array) {
  if (array === undefined || array === null) {
    return [];
  }
  if (!Array.isArray(array)) {
    return [array];
  }
  return array;
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
