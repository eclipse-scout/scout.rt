const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const OptimizeCssAssetsPlugin = require('optimize-css-assets-webpack-plugin');

module.exports = (env, args) => {
  var devMode = args.mode !== 'production';
  var cssFilename = devMode ? '[name].css' : '[name]-[contenthash].min.css';

  return {
    /* ------------------------------------------------------
     * + Entry                                              +
     * ------------------------------------------------------ */
    entry: {
      scout: './src/index.js',
      'theme-default': './src/theme-default.less',
      'theme-dark': './src/theme-dark.less'
    },
    /* ------------------------------------------------------
     * + Output                                             +
     * ------------------------------------------------------ */
    output: {
      filename: '[name].js',
      path: __dirname + '/dist'
    },
    /* ------------------------------------------------------
     * + Optimization                                       +
     * ------------------------------------------------------ */
    optimization: {
      minimizer: [
        // Used to minify CSS assets (by default, run when mode is 'production')
        // see: https://github.com/NMFR/optimize-css-assets-webpack-plugin
        new OptimizeCssAssetsPlugin({
          assetNameRegExp: /\.min\.css$/g
        })
      ]
    },
    /* ------------------------------------------------------
     * + Modules                                            +
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
          loader: 'css-loader'
        }, {
          // Compiles Less to CSS.
          // see: https://webpack.js.org/loaders/less-loader/
          loader: 'less-loader'
        }]
      }]
    },
    /* ------------------------------------------------------
     * + Plugins                                            +
     * ------------------------------------------------------ */
    plugins: [
      // see: https://webpack.js.org/plugins/mini-css-extract-plugin/
      new MiniCssExtractPlugin({
        filename: cssFilename
      })
    ]
  };
}
