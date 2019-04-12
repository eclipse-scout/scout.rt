let path = require('path');

module.exports = {
  mode: 'none',
  entry: {
    index: './index.js'
  },
  output: {
    filename: 'eclipse-scout.js',
    path: path.join(__dirname, 'dist')
  },
  externals: {
    jquery: 'jquery'
  }
};
