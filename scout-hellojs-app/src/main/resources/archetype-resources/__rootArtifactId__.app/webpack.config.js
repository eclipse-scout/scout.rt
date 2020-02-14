const baseConfig = require('@eclipse-scout/cli/scripts/webpack-defaults');

module.exports = (env, args) => {
  args.resDirArray = ['src/main/resources/WebContent', 'node_modules/@eclipse-scout/core/res'];
  const config = baseConfig(env, args);

  config.entry = {
    '${simpleArtifactName}': './src/main/js/${simpleArtifactName}.js',
    '${simpleArtifactName}-theme': './src/main/js/${simpleArtifactName}-theme.less',
    '${simpleArtifactName}-theme-dark': './src/main/js/${simpleArtifactName}-theme-dark.less'
  };

  return config;
};
