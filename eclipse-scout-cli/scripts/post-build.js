/*******************************************************************************
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/

const fs = require('fs');
const path = require('path');
const THEME_JS_OUT_FILTER = f => /.*theme.*\.js/.test(f);

function deleteFile(filename) {
  fs.access(filename, fs.constants.W_OK, (err) => {
    if (err) {
      console.error(`${filename} does not exist or cannot be deleted.`);
    } else {
      fs.unlink(filename, (err) => {
        if (err) {
          throw err;
        }
        console.log(`deleted ${filename}`);
      });
    }
  });
}

module.exports = {
  createFileList: function(dir) {
    const scoutBuild = require('./constants')
    let content = '';
    fs.readdirSync(dir, {withFileTypes: true})
      .filter(dirent => dirent.isFile())
      .map(dirent => dirent.name)
      .filter(fileName => fileName !== scoutBuild.fileListName)
      .filter(fileName => !THEME_JS_OUT_FILTER(fileName))
      .map(fileName => fileName + '\n')
      .forEach(line => content += line);
    fs.writeFileSync(path.join(dir, scoutBuild.fileListName), content, {flag: 'w'});
    console.log(`created ${scoutBuild.fileListName}:\n${content}`);
  },
  cleanOutDir: function(dir) {
    fs.readdirSync(dir)
      .filter(THEME_JS_OUT_FILTER)
      .forEach(f => deleteFile(path.join(dir, f)));
  }
};