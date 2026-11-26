/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

const fs = require('fs');
const path = require('path');
const {listFiles} = require('../files');
const scoutBuild = require('../constants');

function fileListFilter(fileName) {
  return path.basename(fileName) !== scoutBuild.fileListName
    && !fileName.endsWith('.LICENSE')
    && !fileName.endsWith('d.ts')
    && !fileName.endsWith('d.ts.map');
}

module.exports = {
  createFileList: dir => {
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
  }
};
