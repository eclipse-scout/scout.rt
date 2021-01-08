/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
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
  getConstantsForMode: buildMode => {
    if (buildMode !== mode.production) {
      return {
        devMode: true,
        jsFilename: jsFilename.development,
        cssFilename: cssFilename.development,
        outSubDir: outSubDir.development
      };
    }
    return {
      devMode: false,
      jsFilename: jsFilename.production,
      cssFilename: cssFilename.production,
      outSubDir: outSubDir.production
    };
  }
};
