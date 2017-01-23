scout.ErrorHandler = function() {
  this.displayError = true;
  this.sendError = false;
};

scout.ErrorHandler.prototype.init = function(options) {
  $.extend(this, options);
};

scout.ErrorHandler.prototype.handle = function(errorMessage, fileName, lineNumber, columnNumber, error) {
  try {
    var errorCode = this.getJsErrorCode(error),
      logStr = this.createLogMessage(errorMessage, fileName, lineNumber, columnNumber, error, errorCode);

    if (error) {
      $.log.error(logStr, error);
    } else {
      $.log.error(logStr);
    }
    if (window.console) {
      window.console.log(logStr);
    }

    // FIXME bsh: Improve this! Accessing session at index 0 is not a good idea when a window has multiple scout instances (portlet use-case)
    if (scout.sessions.length > 0) {
      var session = scout.sessions[0];
      if (this.displayError) {
        this._showMessageBox(session, errorMessage, errorCode, logStr);
      }
      if (this.sendError) {
        this._sendErrorMessage(session, logStr);
      }
    }
  } catch (err) {
    throw new Error('Error in global JavaScript error handler: ' + err.message + ' (original error: ' + errorMessage + ' at ' + fileName + ':' + lineNumber + ')');
  }
};

scout.ErrorHandler.prototype.createLogMessage = function(errorMessage, fileName, lineNumber, columnNumber, error, errorCode) {
  var logStr = errorMessage + ' at ' + fileName + ':' + lineNumber;
  if (error && error.stack) {
    logStr += '\n' + error.stack;
  }
  logStr += '\n(' + 'Code ' + errorCode + ')';
  return logStr;
};

/**
 * Generate a "cool looking" error code from the JS error object, that
 * does not reveal too much technical information, but at least indicates
 * that a JS runtime error has occurred. (In contrast, fatal errors from
 * the server have numeric error codes.)
 */
scout.ErrorHandler.prototype.getJsErrorCode = function(error) {
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

scout.ErrorHandler.prototype._showMessageBox = function(session, errorMessage, errorCode, logMessage) {
  var options = {
    header: session.optText('ui.UnexpectedProblem', 'Internal UI Error'),
    body: scout.strings.join('\n\n',
      session.optText('ui.InternalUiErrorMsg', errorMessage, ' (' + session.optText('ui.ErrorCodeX', 'Code ' + errorCode, errorCode) + ')'),
      session.optText('ui.UiInconsistentMsg', '')),
    yesButtonText: session.optText('ui.Reload', 'Reload'),
    yesButtonAction: scout.reloadPage,
    noButtonText: session.optText('ui.Ignore', 'Ignore'),
    hiddenText: logMessage
  };

  session.showFatalMessage(options, errorCode);
};

scout.ErrorHandler.prototype._sendErrorMessage = function(session, logMessage) {
  session.sendLogRequest(logMessage);
};
