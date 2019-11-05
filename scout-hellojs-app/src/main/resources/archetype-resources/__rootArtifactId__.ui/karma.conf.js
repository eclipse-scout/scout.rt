const baseConfig = require('@eclipse-scout/cli/scripts/karma-defaults');
module.exports = config => baseConfig(config, './src/test/js/test-index.js');
