module.exports = function(config) {
  const baseConfig = require('./karma-defaults');

  baseConfig(config);

  config.set({
    singleRun: true,
    autoWatch: false,
    browsers: ['ChromeHeadless']
  });
};
