const pluginName = 'AfterEmitWebpackPlugin';
const scoutPostBuild = require('./post-build');

module.exports = class AfterEmitWebpackPlugin {

  constructor(options = {}) {
    const {
      createFileList = false,
      outDir
    } = options;

    this.options = {createFileList, outDir};
  }

  // noinspection JSUnusedGlobalSymbols
  apply(compiler) {
    compiler.hooks.afterEmit.tap(pluginName, (compilation) => {
      scoutPostBuild.cleanOutDir(this.options.outDir);
      if (this.options.createFileList) {
        scoutPostBuild.createFileList(this.options.outDir);
      }
    });
  }
};
