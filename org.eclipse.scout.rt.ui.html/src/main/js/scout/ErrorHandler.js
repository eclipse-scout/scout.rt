scout.ErrorHandler = function() {
};

scout.ErrorHandler.prototype.handle = function(errorMessage, fileName, lineNumber, columnNumber, error) {
  try {
    var errorCode = this.getJsErrorCode(error),
      logStr = errorMessage + ' at ' + fileName + ':' + lineNumber;
    if (error && error.stack) {
      logStr += '\n' + error.stack;
    }
    logStr += '\n(' + 'Code ' + errorCode + ')';
    $.log.error(logStr);
    if (window.console) {
      window.console.log(logStr);
    }
    // FIXME bsh: Improve this! Accessing session at index 0 is not a good idea when a window has multiple scout instances (portlet use-case)
    if (scout.sessions.length > 0) {
      var session = scout.sessions[0],
        boxOptions = {
          header: session.optText('ui.UnexpectedProblem', 'Internal UI Error'),
          body: scout.strings.join('\n\n',
            session.optText('ui.InternalUiErrorMsg', errorMessage, ' (' + session.optText('ui.ErrorCodeX', 'Code ' + errorCode, errorCode) + ')'),
            session.optText('ui.UiInconsistentMsg', '')),
          yesButtonText: session.optText('ui.Reload', 'Reload'),
          yesButtonAction: scout.reloadPage,
          noButtonText: session.optText('ui.Ignore', 'Ignore'),
          hiddenText: logStr
        };

      session.showFatalMessage(boxOptions, errorCode);
      session.sendLogRequest(logStr);
    }
  } catch (err) {
    throw new Error('Error in global JavaScript error handler: ' + err.message + ' (original error: ' + errorMessage + ' at ' + fileName + ':' + lineNumber + ')');
  }
};

/**
 * Generate a "cool looking" error code from the JS error object, that
 * does not reveal too much technical information, but at least indicates
 * that a JS runtime error has occurred. (In contrast, fatal errors from
 * the server have numeric error codes.)
 */
scout.ErrorHandler.prototype.getJsErrorCode = function(errorMessage, fileName, lineNumber, columnNumber, error) {
  if (error) {
    if (error.name === 'EvalError') {
      return 'E1';
    }
    if (error.name === 'InternalError') {
      return 'I2';
    }
    if (error.name === 'RangeError') {
      return 'A3';
    }
    if (error.name === 'ReferenceError') {
      return 'R4';
    }
    if (error.name === 'SyntaxError') {
      return 'S5';
    }
    if (error.name === 'TypeError') {
      return 'T6';
    }
    if (error.name === 'URIError') {
      return 'U7';
    }
  }
  return 'J0';
};
