let path = require('path');

module.exports = {
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
  module: {
    rules: [{
      test: /\.m?js$/,
      exclude: /node_modules/,
      use: {
        loader: 'babel-loader',
        options: {
          presets: ['@babel/preset-env']
        }
      }
    }]
  }
};
