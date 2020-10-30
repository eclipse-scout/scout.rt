/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
const pluginName = 'AfterEmitWebpackPlugin';
const scoutPostBuild = require('./post-build');

module.exports = class AfterEmitWebpackPlugin {
  constructor(options = {}) {
    const {outDir} = options;
    this.options = {outDir};
  }

  // noinspection JSUnusedGlobalSymbols
  apply(compiler) {
    compiler.hooks.afterEmit.tap(pluginName, compilation => {
      scoutPostBuild.cleanOutDir(this.options.outDir);
      scoutPostBuild.createFileList(this.options.outDir);
    });
  }
};
