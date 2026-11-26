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

/**
 * @returns {string[]} the paths of all files in the given directory and its subdirectories (excluding the directories)
 */
function listFiles(dir) {
  if (!fs.existsSync(dir)) {
    return [];
  }
  return fs.readdirSync(dir, {recursive: true, withFileTypes: true})
    .filter(entry => !entry.isDirectory())
    .map(entry => path.join(entry.parentPath, entry.name));
}

module.exports.listFiles = listFiles;
