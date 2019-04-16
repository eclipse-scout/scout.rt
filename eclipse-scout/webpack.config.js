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
const CleanWebpackPlugin = require('clean-webpack-plugin');
const CopyPlugin = require('copy-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const OptimizeCssAssetsPlugin = require('optimize-css-assets-webpack-plugin');
const WebpackShellPlugin = require('webpack-shell-plugin');
const TerserPlugin = require('terser-webpack-plugin');

let path = require('path');
let webpack = require('webpack');

module.exports = (env, args) => {
  let devMode = args.mode !== 'production';
  let cssFilename = devMode ? '[name].css' : '[name]-[contenthash].min.css';
  let jsFilename = devMode ? '[name].js' : '[name]-[contenthash].min.js';
  console.log('Webpack mode:', args.mode);

  return {
    target: 'web',
    mode: 'none',
    /* ------------------------------------------------------
     * + Entry                                              +
     * ------------------------------------------------------ */
    entry: {
      'eclipse-scout': './index.js',
      'scout-theme': './src/scout/scout-theme.less' /*,
      'theme-dark': './src/theme-dark.less'*/
    },
    /* ------------------------------------------------------
     * + Output                                             +
     * ------------------------------------------------------ */
    output: {
      filename: jsFilename,
      path: path.join(__dirname, 'dist')
    },
    /* ------------------------------------------------------
     * + Optimization                                       +
     * ------------------------------------------------------ */
    optimization: {
      minimizer: [
        // # Minify CSS
        // Used to minify CSS assets (by default, run when mode is 'production')
        // see: https://github.com/NMFR/optimize-css-assets-webpack-plugin
        new OptimizeCssAssetsPlugin({
          assetNameRegExp: /\.min\.css$/g
        }),
        // # Minify JS
        new TerserPlugin({
          test: /\.js(\?.*)?$/i,
          sourceMap: true,
          cache: true,
          parallel: true
        })
      ]
    },
    /* ------------------------------------------------------
     * + Module                                             +
     * ------------------------------------------------------ */
    module: {
      // LESS
      rules: [{
        test: /\.less$/,
        use: [{
          // Extracts CSS into separate files. It creates a CSS file per JS file which contains CSS.
          // It supports On-Demand-Loading of CSS and SourceMaps.
          // see: https://webpack.js.org/plugins/mini-css-extract-plugin/
          //
          // TODO [awe] toolstack: discuss with MVI - unnecessary .js files
          // Note: this creates some useless *.js files, like dark-theme.js
          // This seems to be an issue in webpack, workaround is to remove the files later
          // see: https://github.com/webpack-contrib/mini-css-extract-plugin/issues/151
          loader: MiniCssExtractPlugin.loader
        }, {
          // Interprets @import and url() like import/require() and will resolve them.
          // see: https://webpack.js.org/loaders/css-loader/
          loader: 'css-loader', options: {
            sourceMap: true,
            modules: false, // We don't want to work with CSS modules
            url: false      // Don't resolve URLs in LESS, because relative path does not match /res/fonts
          }
        }, {
          // Compiles Less to CSS.
          // see: https://webpack.js.org/loaders/less-loader/
          loader: 'less-loader', options: {
            sourceMap: true
          }
        }]
      }]
    },
    /* ------------------------------------------------------
     * + Devtool                                            +
     * ------------------------------------------------------ */
    // This option controls if and how source maps are generated.
    // see: https://webpack.js.org/configuration/devtool/
    devtool: devMode ? 'source-map' : undefined,
    /* ------------------------------------------------------
     * + Externals                                          +
     * ------------------------------------------------------ */
    // see: https://webpack.js.org/configuration/externals/
    externals: {
      jquery: 'jquery'
    },
    /* ------------------------------------------------------
     * + Plugins                                            +
     * ------------------------------------------------------ */
    plugins: [
      // see: https://webpack.js.org/plugins/mini-css-extract-plugin/
      new MiniCssExtractPlugin({
        filename: cssFilename
      }),
      // see: https://webpack.js.org/guides/output-management/#cleaning-up-the-dist-folder
      new CleanWebpackPlugin(),
      // see: https://www.npmjs.com/package/webpack-shell-plugin
      new WebpackShellPlugin({
        onBuildEnd: ['node post-build.js']
      }),
      // # Copy resources
      // https://www.npmjs.com/package/copy-webpack-plugin
      new CopyPlugin([{
        // # Copy static web-resources
        from: 'res',
        to: '.'
      }]),
      // Shows progress information in the console
      new webpack.ProgressPlugin()
    ]
  };
};
