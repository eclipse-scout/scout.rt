/**
 * Loads a module.js, replace all __include tags, and eval() the dynamic code
 */
function loadDynamicScript(modulePath) {
  var moduleText = _loadAndResolveScriptContent(modulePath);

  // Add sourceURL marker to make the the dynamic code show up in the browser's dev tools debugger
  var matches = /^(.*)\/(.*?)$/.exec(window.location.href);
  if (matches !== null) {
    var sourceUrl = matches[1] + '/(' + (modulePath.substring(modulePath.lastIndexOf('/') + 1) + ')');
    moduleText += '\n//# sourceURL=' + sourceUrl;
  }

  eval(moduleText);
}

function _loadAndResolveScriptContent(path) {
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
    throw new Error(textStatus);
  });
  //replace includes
  content = content.replace(
    /(?:\/\/\s*@|__)include\s*\(\s*(?:\"([^\"]+)\"|'([^']+)')\s*\)(?:;)?/g,
    function(match, p1, p2, offset, string) {
      var includePrefix = path.replace(/^(.*\/)[^\/]*$/, '$1');
      var includePath = includePrefix + (p1 ? p1 : p2);
      return _insertLineNumbers(includePath, _loadAndResolveScriptContent(includePath));
    }
  );

  return content;
}

// Copied from ScriptFileBuilder.java
function _insertLineNumbers(filename, text) {
  if (typeof text === 'undefined' || text === null) {
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
    if (_lineIsBeginOfMultilineBlockComment(line, insideBlockComment)) {
      // also if line is endMLBC AND beginMLBC
      insideBlockComment = true;
    }
    else if (_lineIsEndOfMultilineBlockComment(line, insideBlockComment)) {
      insideBlockComment = false;
    }
    lineNo++;
  }
  return buf;
}

// Copied from ScriptFileBuilder.java
function _lineIsBeginOfMultilineBlockComment(line, insideBlockComment) {
  if (insideBlockComment) {
    return false;
  }
  var a = line.lastIndexOf("/*");
  var b = line.lastIndexOf("*/");
  var c = line.lastIndexOf("/*/");
  return (a >= 0 && (b < 0 || b < a || (c == a)));
}

// Copied from ScriptFileBuilder.java
function _lineIsEndOfMultilineBlockComment(line, insideBlockComment) {
  if (!insideBlockComment) {
    return false;
  }
  var a = line.indexOf("/*");
  var b = line.indexOf("*/");
  var c = line.lastIndexOf("/*/");
  return (b >= 0 && (a < 0 || a < b || (c == a)));
}
