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
const TerserPlugin = require('terser-webpack-plugin');

let path = require('path');
let webpack = require('webpack');

module.exports = (env, args) => {
  let devMode = args.mode !== 'production';
  let jsFilename = devMode ? '[name].js' : '[name]-[contenthash].min.js';
  console.log('Webpack mode:', args.mode);

  // FIXME [awe] toolstack: fix bug with double "eclipse-scout.js" files in output (one of them seems to contain jQuery)
  // FIXME [awe] toolstack: create "web.properties" with filenames from dist/, required for resource-loading in Java

  return {
    target: 'web',
    mode: 'none',
    devtool: undefined,
    /* ------------------------------------------------------
     * + Entry                                              +
     * ------------------------------------------------------ */
    entry: {
      'widgets-app': './index.js'
    },
    /* ------------------------------------------------------
     * + Output                                             +
     * ------------------------------------------------------ */
    output: {
      filename: jsFilename,
      path: path.join(__dirname, 'dist'),
      chunkFilename: jsFilename
    },
    /* ------------------------------------------------------
     * + Optimization                                       +
     * ------------------------------------------------------ */
    optimization: {
      // # Split Chunks
      // Note: we don't define jQuery and Eclipse Scout as 'externals', since we want to bundle
      // them with our code and also provide minify, content-hash etc. for these libraries
      splitChunks: {
        chunks: 'all',
        cacheGroups: {
          // # jQuery
          jquery: {
            name: 'jquery',
            test: /.*jquery/
          },
          // # Eclipse Scout
          'eclipse-scout': {
            name: 'eclipse-scout',
            test: /eclipse\-scout[\/|\\]/
          }
        }
      },
      minimizer: [
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
      rules: [{
        // # Babel
        test: /\.m?js$/,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader',
          options: {
            presets: ['@babel/preset-env']
          }
        }
      }]
    },
    /* ------------------------------------------------------
     * + Plugins                                            +
     * ------------------------------------------------------ */
    plugins: [
      // # Clean dist/ folder
      // see: https://webpack.js.org/guides/output-management/#cleaning-up-the-dist-folder
      new CleanWebpackPlugin(),
      // # Copy resources
      // https://www.npmjs.com/package/copy-webpack-plugin
      new CopyPlugin([{
        // # Copy CSS theme from eclipse-scout
        // Note: when our app enhances the standard styles from eclipse-scout by adding custom styles,
        // the app needs to define a LESS dependency to scout-theme.less and run a LESS/CSS build. Since
        // this app works with the default styles we can simply copy the pre-built CSS from eclipse-scout.
        from: 'node_modules/eclipse-scout/dist',
        test: /\.css$/,
        to: '.'
      }, {
        // # Copy static web-resources
        from: 'res',
        to: '.'
      }]),
      // # Shows progress information in the console
      new webpack.ProgressPlugin()
    ]
  };
};
