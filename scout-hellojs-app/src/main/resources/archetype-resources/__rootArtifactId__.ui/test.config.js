const baseConfig = require('@eclipse-scout/cli/scripts/test-defaults');
module.exports = config => baseConfig(config, './src/test/js/test-index.ts');
