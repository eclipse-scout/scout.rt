const mode = {
  production: 'production',
  development: 'development'
};

const outSubDir = {
  production: 'prod',
  development: 'dev'
};

const jsFilename = {
  production: '[name]-[contenthash].min.js',
  development: '[name].js'
};

const cssFilename = {
  production: '[name]-[contenthash].min.css',
  development: '[name].css'
};

module.exports = {
  mode: mode,
  outDir: 'dist',
  outSubDir: outSubDir,
  fileListName: 'file-list',
  jsFilename: jsFilename,
  cssFilename: cssFilename,
  getConstantsForMode: (buildMode) => {
    if (buildMode !== mode.production) {
      return {
        devMode: true,
        jsFilename: jsFilename.development,
        cssFilename: cssFilename.development,
        outSubDir: outSubDir.development
      };
    } else {
      return {
        devMode: false,
        jsFilename: jsFilename.production,
        cssFilename: cssFilename.production,
        outSubDir: outSubDir.production
      };
    }
  }
};