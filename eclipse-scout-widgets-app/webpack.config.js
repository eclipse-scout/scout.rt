let path = require('path');
let webpack = require('webpack');

const CleanWebpackPlugin = require('clean-webpack-plugin');
const CopyPlugin = require('copy-webpack-plugin');

module.exports = (env, args) => {
  let devMode = args.mode !== 'production';

  return {
    mode: 'none',
    entry: {
      index: './index.js'
    },
    output: {
      filename: 'widgets-app.js',
      path: path.join(__dirname, 'dist'),
      chunkFilename: '[name].js'
    },
    optimization: {
      splitChunks: {
        chunks: 'all',
        cacheGroups: {
          jquery: {
            test: /.*jquery/,
            name: 'jquery'
          }
        }
      }
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
