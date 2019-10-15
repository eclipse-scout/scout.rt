/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
const fs = require('fs');
const path = require('path');

const _getAllFiles = dir => {
  if (!fs.existsSync(dir)) {
    return [];
  }

  return fs.readdirSync(dir).reduce((files, file) => {
    const name = path.join(dir, file);
    const isDirectory = fs.statSync(name).isDirectory();
    return isDirectory ? [...files, ..._getAllFiles(name)] : [...files, name];
  }, []);
};

module.exports = _getAllFiles;
