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
const DIST_DIR = './dist';
const LIST_FILE = 'file-list';

const fs = require('fs');
const errno = require('errno');
const path = require('path');

deleteFile(path.join(DIST_DIR, 'theme-default.js'));
deleteFile(path.join(DIST_DIR, 'theme-dark.js'));
createFileList();

function deleteFile(filename) {
  fs.unlink(filename, (err) => {
    if (err) {
      if (err.errorno === errno.ENOENT) {
        console.log('file does not exist', filename);
        return;
      } else {
        throw err;
      }
    }
    console.log('deleted ', filename);
  });
}

function createFileList() {
  let content = '';
  fs.readdirSync(DIST_DIR, {withFileTypes: true})
    .filter(dirent => dirent.isFile() && dirent.name !== LIST_FILE)
    .forEach(dirent => content += dirent.name + '\n');
  fs.writeFileSync(path.join(DIST_DIR, LIST_FILE), content, {flag: 'w'});
  console.log('# created file-list:\n' + content);
}
