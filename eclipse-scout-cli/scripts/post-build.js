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
const listFiles = require('./list-files');

function deleteFile(filename) {
  fs.access(filename, fs.constants.W_OK, err => {
    if (err) {
      console.error(`${filename} does not exist or cannot be deleted.`);
    } else {
      fs.unlink(filename, unlinkErr => {
        if (unlinkErr) {
          throw unlinkErr;
        }
      });
    }
  });
}

module.exports = {
  createFileList: dir => {
    const scoutBuild = require('./constants');
    let content = '';
    listFiles(dir)
      .filter(fileName => fileName !== scoutBuild.fileListName)
      .filter(fileName => !fileName.endsWith('.LICENSE'))
      .filter(fileName => !themeJsOutFilter(fileName))
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
