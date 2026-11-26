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
    const content = listFiles(dir)
      .filter(fileName => fileListFilter(fileName))
      .map(file => path.relative(dir, file).replace(/\\/g, '/'))
      .map(fileName => `${fileName}\n`)
      .join('');
    const fileListPath = path.join(dir, scoutBuild.fileListName);
    if (content.length < 1) {
      fs.rmSync(fileListPath, {force: true});
      return;
    }
    fs.writeFileSync(fileListPath, content);
  }
};
