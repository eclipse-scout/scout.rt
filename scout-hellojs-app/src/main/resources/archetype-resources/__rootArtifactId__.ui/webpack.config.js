const baseConfig = require('@eclipse-scout/cli/scripts/webpack-defaults');
module.exports = (env, args) => {
  args.resDirArray = [];
  const config = baseConfig(env, args);
  return {
    entry: {
      '${simpleArtifactName}-ui': './src/main/js/index.js'
    },
    ...baseConfig.libraryConfig(config)
  };
};
