/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
const fs = require('fs');
const path = require('path');

const getAllFiles = dir => {
  if (!fs.existsSync(dir)) {
    return [];
  }

  return fs.readdirSync(dir).reduce((files, file) => {
    const filePath = path.join(dir, file);
    if (!fs.existsSync(filePath)) {
      return files;
    }
    return isDirectory(filePath) ? [...files, ...getAllFiles(filePath)] : [...files, filePath];
  }, []);
};

function isDirectory(filePath) {
  return fs.statSync(filePath).isDirectory();
}

const cleanFolder = dir => {
  if (!fs.existsSync(dir)) {
    return;
  }

  const files = fs.readdirSync(dir);
  for (const file of files) {
    try {
      let filePath = path.join(dir, file);
      if (isDirectory(filePath)) {
        fs.rmdirSync(filePath, {recursive: true});
      } else {
        file.unlink(path.resolve(dir, file));
      }
    } catch (err) {
      console.log(`${dir}/${file} could not be removed`, err);
    }
  }
};

const deleteFolder = dir => {
  if (!fs.existsSync(dir)) {
    return;
  }

  try {
    fs.rmdirSync(dir, {recursive: true});
  } catch (err) {
    console.log(`${dir} could not be deleted`, err);
  }
};

module.exports.listFiles = getAllFiles;
module.exports.cleanFolder = cleanFolder;
module.exports.deleteFolder = deleteFolder;
