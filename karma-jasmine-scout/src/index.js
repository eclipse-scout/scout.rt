/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
let createPattern = path => ({pattern: path, included: true, served: true, watched: false});

let findJqueryPosition = files => {
  if (files.length <= 1) {
    return 0;
  }
  for (let index = 0; index < files.length; index++) {
    let entry = files[index];
    if (entry.pattern && entry.pattern.endsWith('/jquery.js')) {
      return index + 1; // right after the jquery
    }
  }
  return files.length - 1; // default: second last
};

let initJasmine_scout = files => {
  let path = require('path');
  // jqueryExtensions must be loaded after the jquery file
  let insertPos = findJqueryPosition(files);
  files.splice(insertPos, 0,
    createPattern(require.resolve('sourcemapped-stacktrace')),
    createPattern(path.join(__dirname, 'stackTraceMapper.js')),
    createPattern(path.join(__dirname, 'JasmineScout.css')));
};

initJasmine_scout.$inject = ['config.files'];

module.exports = {
  'framework:jasmine-scout': ['factory', initJasmine_scout]
};

