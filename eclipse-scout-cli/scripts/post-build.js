/*
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

const fs = require('fs');
const path = require('path');
const themeJsOutFilter = f => /.*theme.*\.js/.test(f);
const {listFiles} = require('./files');
const scoutBuild = require('./constants');

function deleteFile(filename) {
  if (!fs.existsSync(filename)) {
    return;
  }
  try {
    fs.accessSync(filename, fs.constants.W_OK);
  } catch (err) {
    console.error(`No right to delete ${filename}.`, err);
    return;
  }
  fs.unlink(filename, unlinkErr => {
    if (unlinkErr) {
      throw unlinkErr;
    }
  });
}

function fileListFilter(fileName) {
  return fileName !== scoutBuild.fileListName
    && !fileName.endsWith('.LICENSE')
    && !themeJsOutFilter(fileName)
    && !fileName.endsWith('d.ts')
    && !fileName.endsWith('d.ts.map');
}

module.exports = {
  createFileList: dir => {
    const scoutBuild = require('./constants');
    let content = '';
    listFiles(dir)
      .filter(fileName => fileListFilter(fileName))
      .map(file => file.substring(dir.length + 1))
      .map(path => path.replace(/\\/g, '/'))
      .map(fileName => `${fileName}\n`)
      .forEach(line => {
        content += line;
      });
    if (content.length < 1) {
      return;
    }
    fs.writeFileSync(path.join(dir, scoutBuild.fileListName), content, {flag: 'w'});
    console.log(`created ${scoutBuild.fileListName}:\n${content}`);
  },
  cleanOutDir: dir => {
    if (!fs.existsSync(dir)) {
      return;
    }
    listFiles(dir)
      .filter(themeJsOutFilter)
      .forEach(f => deleteFile(f));
  }
};
