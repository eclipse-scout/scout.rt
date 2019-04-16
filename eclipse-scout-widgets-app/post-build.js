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
const errno = require('errno');

deleteFile('./dist/theme-default.js');
deleteFile('./dist/theme-dark.js');

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
