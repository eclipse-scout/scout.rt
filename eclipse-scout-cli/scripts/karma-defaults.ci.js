module.exports = function(config, specEntryPoint) {
  const baseConfig = require('./karma-defaults');

  baseConfig(config, specEntryPoint);

  config.set({
    singleRun: true,
    autoWatch: false,
    browsers: ['ChromeHeadless']
  });
};
