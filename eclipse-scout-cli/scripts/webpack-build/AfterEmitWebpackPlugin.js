/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
const pluginName = 'AfterEmitWebpackPlugin';
const scoutPostBuild = require('./post-build');

module.exports = class AfterEmitWebpackPlugin {
  constructor(options = {}) {
    const {outDir, createFileList} = options;
    this.options = {outDir, createFileList};
  }

  // noinspection JSUnusedGlobalSymbols
  apply(compiler) {
    compiler.hooks.afterEmit.tap(pluginName, compilation => {
      scoutPostBuild.cleanOutDir(this.options.outDir);
      if (this.options.createFileList ?? true) {
        scoutPostBuild.createFileList(this.options.outDir);
      }
    });
  }
};
