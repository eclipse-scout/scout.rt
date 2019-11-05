const baseConfig = require('@eclipse-scout/cli/scripts/webpack-defaults');
module.exports = (env, args) => {
  args.resDirArray = [];
  return baseConfig(env, args);
};
