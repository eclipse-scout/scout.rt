const path = require('path');

var specIndex = path.resolve(__dirname, 'test/test-module.js');
var preprocessorObj = {};
preprocessorObj[specIndex] = ['webpack'];

module.exports = function(config) {
  config.set({
    // browsers: ['PhantomJS'],
    files: [{
      pattern: specIndex,
      watched: false
    }],
    frameworks: ['jasmine'],
    preprocessors: preprocessorObj,
    /*
    webpack: {
        module: {
            loaders: [{
                test: /\.js/,
                exclude: /node_modules/,
                loader: 'babel-loader'
            }]
        },
        watch: true
    },
    */
    webpackServer: {
      noInfo: true
    }
  });
};
