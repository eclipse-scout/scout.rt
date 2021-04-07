/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
    createPattern(path.join(__dirname, 'scoutMatchers.js')),
    createPattern(path.join(__dirname, 'cloneMatchers.js')),
    createPattern(path.join(__dirname, 'jqueryExtensions.js')),
    createPattern(path.join(__dirname, 'JasmineScout.css')),
    createPattern(path.join(__dirname, 'jasmineScoutHelpers.js')));
};

initJasmine_scout.$inject = ['config.files'];

module.exports = {
  'framework:jasmine-scout': ['factory', initJasmine_scout]
};

