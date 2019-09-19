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
var createPattern = function(path) {
  return {pattern: path, included: true, served: true, watched: false};
};

var findJqueryPosition = function(files) {
  if(files.length <= 1) {
    return 0;
  }
  for(var index = 0; index < files.length; index++) {
    var entry = files[index];
    if(entry.pattern && entry.pattern.endsWith('/jquery.js')) {
      return index + 1; // right after the jquery
    }
  }
  return files.length - 1; // default: second last
};

var initJasmine_scout = function(files) {
  // jqueryExtensions must be loaded after the jquery file
  var insertPos = findJqueryPosition(files);
  files.splice(insertPos, 0,
    createPattern(__dirname + '/scoutMatchers.js'),
    createPattern(__dirname + '/cloneMatchers.js'),
    createPattern(__dirname + '/jqueryExtensions.js'),
    createPattern(__dirname + '/JasmineScout.css'),
    createPattern(__dirname + '/jasmineScoutHelpers.js'));
};

initJasmine_scout.$inject = ['config.files'];

module.exports = {
  'framework:jasmine-scout': ['factory', initJasmine_scout]
};

