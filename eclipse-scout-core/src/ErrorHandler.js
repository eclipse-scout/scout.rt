/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {strings} from './index';
import {AjaxError} from './index';
import {scout} from './index';
import {NullLogger} from './index';
import * as $ from 'jquery';
import {App} from './index';

export default class ErrorHandler {

constructor() {
  this.logError = true;
  this.displayError = true;
  this.sendError = false;
  this.windowErrorHandler = this._onWindowError.bind(this);
}

/**
 * Use this constant to configure whether or not all instances of the ErrorHandler should write
 * to the console. When you've installed a console appender to log4javascript you can set the
 * value to false, because the ErrorHandler also calls $.log.error and thus the appender has
 * already written the message to the console. We don't want to see it twice.
 */
static CONSOLE_OUTPUT = true;

init(options) {
  $.extend(this, options);
}

// Signature matches the "window.onerror" event handler
// https://developer.mozilla.org/en-US/docs/Web/API/GlobalEventHandlers/onerror
_onWindowError(errorMessage, fileName, lineNumber, columnNumber, error) {
  try {
    if (error instanceof Error) {
      this.handle(error);
    } else {
      var code = 'J00';
      var log = errorMessage + ' at ' + fileName + ':' + lineNumber + '\n(' + 'Code ' + code + ')';
      this.handleErrorInfo({
        code: code,
        message: errorMessage,
        log: log
      });
    }
  } catch (err) {
    throw new Error('Error in global JavaScript error handler: ' + err.message + ' (original error: ' + errorMessage + ' at ' + fileName + ':' + lineNumber + ')');
  }
}

/**
 * Handles unexpected JavaScript errors. The arguments are first analyzed and then handled.
 *
 * This method may be called by passing the arguments individually or as an array (or array-like object)
 * in the first argument.
 * Examples:
 *   1. try { ... } catch (err) { handler.handle(err); }
 *   2. $.get().fail(function(jqXHR, textStatus, errorThrown) { handler.handle(jqXHR, textStatus, errorThrown); }
 *   3. $.get().fail(function(jqXHR, textStatus, errorThrown) { handler.handle(arguments); } // <-- recommended
 *
 * @return {object} the analyzed errorInfo
 */
handle() {
  var args = arguments;
  if (args.length === 1 && args[0] && (String(args[0]) === '[object Arguments]' || Array.isArray(args[0]))) {
    args = args[0];
  }
  var errorInfo = this.analyzeError.apply(this, args);
  this.handleErrorInfo(errorInfo);
  return errorInfo;
}

/**
 * Returns an "errorInfo" object for the given arguments. The following cases are handled:
 * 1. Error objects           (code: computed by getJsErrorCode())
 * 2. jQuery AJAX errors      (code: 'X' + HTTP status code)
 * 3. Nothing                 (code: 'P3')
 * 4. Everything else         (code: 'P4')
 */
analyzeError(error) {
  var errorInfo = {
    code: null,
    message: null,
    location: null,
    stack: null,
    debugInfo: null,
    log: null
  };

  if (error instanceof Error) {
    // 1. Errors
    errorInfo.code = this.getJsErrorCode(error);
    errorInfo.message = String(error.message || error);
    if (error.fileName) {
      errorInfo.location = error.fileName + strings.join('', strings.box(':', error.lineNumber), strings.box(':', error.columnNumber));
    }
    if (error.stack) {
      errorInfo.stack = String(error.stack);
    }
    if (error.debugInfo) { // scout extension
      errorInfo.debugInfo = error.debugInfo;
    }
    errorInfo.log = 'Unexpected error: ' + errorInfo.message;
    if (errorInfo.location) {
      errorInfo.log += ' at ' + errorInfo.location;
    }
    if (errorInfo.stack) {
      errorInfo.log += '\n' + errorInfo.stack;
    }
    if (errorInfo.debugInfo) {
      // Error throwers may put a "debugInfo" string on the error object that is then added to the log string (this is a scout extension).
      errorInfo.log += '\n----- Additional debug information: -----\n' + errorInfo.debugInfo;
    }

  } else if ($.isJqXHR(error) || (Array.isArray(error) && $.isJqXHR(error[0])) || error instanceof AjaxError) {
    // 2. jQuery $.ajax() error (arguments: jqXHR, textStatus, errorThrown, requestOptions)
    var jqXHR, errorThrown, requestOptions;
    if (error instanceof AjaxError) {
      jqXHR = error.jqXHR;
      errorThrown = error.errorThrown;
      requestOptions = error.requestOptions; // scout extension
    } else {
      var args = (Array.isArray(error) ? error : arguments);
      jqXHR = args[0];
      errorThrown = args[2];
      requestOptions = args[3]; // scout extension
    }

    var ajaxRequest = (requestOptions ? strings.join(' ', requestOptions.type, requestOptions.url) : '');
    var ajaxStatus = (jqXHR.status ? strings.join(' ', jqXHR.status, errorThrown) : 'Connection error');

    errorInfo.code = 'X' + (jqXHR.status || '0');
    errorInfo.message = 'AJAX call' + strings.box(' "', ajaxRequest, '"') + ' failed' + strings.box(' [', ajaxStatus, ']');
    errorInfo.log = errorInfo.message;
    if (jqXHR.responseText) {
      errorInfo.debugInfo = 'Response text:\n' + jqXHR.responseText;
      errorInfo.log += '\n' + errorInfo.debugInfo;
    }

  } else if (!error) {
    // 3. No reason provided
    errorInfo.code = 'P3';
    errorInfo.message = 'Unknown error';
    errorInfo.log = 'Unexpected error (no reason provided)';

  } else {
    // 4. Everything else (e.g. when strings are thrown)
    var s = (typeof error === 'string' || typeof error === 'number') ? String(error) : null;
    errorInfo.code = 'P4';
    errorInfo.message = s || 'Unexpected error';
    if (!s) {
      try {
        s = JSON.stringify(error); // may throw "cyclic object value" error
      } catch (err) {
        s = String(error);
      }
    }
    errorInfo.log = 'Unexpected error: ' + s;

  }

  return errorInfo;
}

/**
 * Expects an object as returned by analyzeError() and handles it:
 * - If the flag "logError" is set, the log message is printed to the console
 * - If there is a scout session and the flag "displayError" is set, the error is shown in a a message box.
 * - If there is a scout session and the flag "sendError" is set, the error is sent to the UI server.
 */
handleErrorInfo(errorInfo) {
  if (this.logError && errorInfo.log) {
    $.log.error(errorInfo.log);

    // Note: when the null-logger is active it has already written the error to the console
    // when the $.log.error function has been called above, so we don't have to log again here.
    var writeToConsole = ErrorHandler.CONSOLE_OUTPUT;
    if ($.log instanceof NullLogger) {
      writeToConsole = false;
    }
    if (writeToConsole && window && window.console) {
      if (window.console.error) {
        window.console.error(errorInfo.log);
      } else if (window.console.log) {
        window.console.log(errorInfo.log);
      }
    }
  }

  // Note: The error handler is installed globally and we cannot tell in which scout session the error happened.
  // We simply use the first scout session to display the message box and log the error. This is not ideal in the
  // multi-session-case (portlet), but currently there is no other way. Besides, this feature is not in use yet.
  if (App.get().sessions.length > 0) {
    var session = App.get().sessions[0];
    if (this.displayError) {
      this._showMessageBox(session, errorInfo.message, errorInfo.code, errorInfo.log);
    }
    if (this.sendError) {
      this._sendErrorMessage(session, errorInfo.log);
    }
  }
}

/**
 * Generate a "cool looking" error code from the JS error object, that
 * does not reveal too much technical information, but at least indicates
 * that a JS runtime error has occurred. (In contrast, fatal errors from
 * the server have numeric error codes.)
 */
getJsErrorCode(error) {
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
}

_showMessageBox(session, errorMessage, errorCode, logMessage) {
  var options = {
    header: session.optText('ui.UnexpectedProblem', 'Internal UI Error'),
    body: strings.join('\n\n',
      session.optText('ui.InternalUiErrorMsg', errorMessage, ' (' + session.optText('ui.ErrorCodeX', 'Code ' + errorCode, errorCode) + ')'),
      session.optText('ui.UiInconsistentMsg', '')),
    yesButtonText: session.optText('ui.Reload', 'Reload'),
    yesButtonAction: scout.reloadPage,
    noButtonText: session.optText('ui.Ignore', 'Ignore'),
    hiddenText: logMessage
  };

  session.showFatalMessage(options, errorCode);
}

_sendErrorMessage(session, logMessage) {
  session.sendLogRequest(logMessage);
}
}
