const path = require('path');

module.exports = {
  mode: 'development', // TODO [awe] toolstack: set dynamically, just added it to get rid of the warnings
  entry: './src/index.js',
  output: {
    filename: 'main.js',
    path: path.resolve(__dirname, 'dist')
  }
};