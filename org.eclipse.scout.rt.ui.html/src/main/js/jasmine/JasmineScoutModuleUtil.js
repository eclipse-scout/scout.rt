/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * Loads a module.js, replace all __include tags, and eval() the dynamic code
 */
function loadDynamicScript(modulePath) {
  var moduleText = loadAndResolveScriptContent(modulePath);

  // Add sourceURL marker to make the the dynamic code show up in the browser's dev tools debugger
  var matches = /^(.*)\/(.*?)$/.exec(window.location.href);
  if (matches !== null) {
    var sourceUrl = matches[1] + '/(' + (modulePath.substring(modulePath.lastIndexOf('/') + 1) + ')');
    moduleText += '\n//# sourceURL=' + sourceUrl;
  }

  eval(moduleText);
}

function loadAndResolveScriptContent(path) {
  var content = '';
  $.ajax({
    async: false,
    type: 'GET',
    dataType: 'text',
    contentType: 'application/javascript; charset=UTF-8',
    cache: false,
    url: path
  }).done(function(data) {
    content = data;
  }).fail(function(jqXHR, textStatus, errorThrown) {
    throw new Error('Script ' + path + ' could not be loaded. Status: ' + textStatus + '. Error thrown: ' + errorThrown);
  });
  //replace includes
  content = content.replace(
    /(?:\/\/\s*@|__)include\s*\(\s*(?:\"([^\"]+)\"|'([^']+)')\s*\)(?:;)?/g,
    function(match, p1, p2, offset, string) {
      var includePrefix = path.replace(/^(.*\/)[^\/]*$/, '$1');
      var includePath = includePrefix + (p1 ? p1 : p2);
      return insertLineNumbers(includePath, loadAndResolveScriptContent(includePath));
    }
  );

  return content;
}

// Copied from ScriptFileBuilder.java
function insertLineNumbers(filename, text) {
  if (text === undefined || text === null) {
    return text;
  }
  var index = filename.lastIndexOf('/');
  if (index >= 0) {
    filename = filename.substring(index + 1);
  }
  index = filename.lastIndexOf('.');
  if (index >= 0) {
    filename = filename.substring(0, index);
  }
  var lineNo = 1;
  var insideBlockComment = false;
  var buf = '';
  var lines = text.split('\n');
  for (var i = 0; i < lines.length; i++) {
    var line = lines[i];
    buf += (insideBlockComment ? '//' : '/*');
    buf += filename + ':';
    buf += lineNo;
    for (var j = (lineNo + '').length + 1; j < (lines.length + '').length; j++) {
      buf += ' ';
    }
    buf += (insideBlockComment ? '//' : '*/') + ' ';
    buf += line + '\n';
    if (lineIsBeginOfMultilineBlockComment(line, insideBlockComment)) {
      // also if line is endMLBC AND beginMLBC
      insideBlockComment = true;
    } else if (lineIsEndOfMultilineBlockComment(line, insideBlockComment)) {
      insideBlockComment = false;
    }
    lineNo++;
  }
  return buf;
}

// Copied from ScriptFileBuilder.java
function lineIsBeginOfMultilineBlockComment(line, insideBlockComment) {
  if (insideBlockComment) {
    return false;
  }
  var a = line.lastIndexOf("/*");
  var b = line.lastIndexOf("*/");
  var c = line.lastIndexOf("/*/");
  return (a >= 0 && (b < 0 || b < a || (c === a)));
}

// Copied from ScriptFileBuilder.java
function lineIsEndOfMultilineBlockComment(line, insideBlockComment) {
  if (!insideBlockComment) {
    return false;
  }
  var a = line.indexOf("/*");
  var b = line.indexOf("*/");
  var c = line.lastIndexOf("/*/");
  return (b >= 0 && (a < 0 || a < b || (c === a)));
}
