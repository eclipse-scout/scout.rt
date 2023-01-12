/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AjaxError, App, arrays, icons, InitModelOf, LogLevel, NullLogger, ObjectModel, ObjectWithType, scout, Session, strings} from './index';
import $ from 'jquery';
import * as sourcemappedStacktrace from 'sourcemapped-stacktrace';

export interface ErrorHandlerModel extends ObjectModel<ErrorHandler> {
  /**
   * Default is true
   */
  logError?: boolean;

  /**
   * Default is true
   */
  displayError?: boolean;

  /**
   * Default is false
   */
  sendError?: boolean;

  session?: Session;
}

export interface ErrorInfo {
  log: string;
  level?: LogLevel;
  error?: any; // May be Error, AjaxError or any other type like string, number etc. since any object can be thrown.
  mappingError?: string;
  code?: string;
  message?: string;
  stack?: string;
  mappedStack?: string;
  debugInfo?: string;
}

export class ErrorHandler implements ErrorHandlerModel, ObjectWithType {
  declare model: ErrorHandlerModel;
  objectType: string;
  logError: boolean;
  displayError: boolean;
  sendError: boolean;
  session: Session;
  windowErrorHandler: OnErrorEventHandlerNonNull;

  constructor() {
    this.logError = true;
    this.displayError = true;
    this.sendError = false;
    this.windowErrorHandler = this._onWindowError.bind(this);
    this.session = null;
  }

  /**
   * Use this constant to configure whether all instances of the ErrorHandler should write
   * to the console. When you've installed a console appender to log4javascript you can set the
   * value to false, because the ErrorHandler also calls $.log.error and thus the appender has
   * already written the message to the console. We don't want to see it twice.
   */
  static CONSOLE_OUTPUT = true;

  init(options?: InitModelOf<this>) {
    $.extend(this, options);
  }

  // Signature matches the "window.onerror" event handler
  // https://developer.mozilla.org/en-US/docs/Web/API/GlobalEventHandlers/onerror
  protected _onWindowError(errorMessage: string, fileName?: string, lineNumber?: number, columnNumber?: number, error?: Error) {
    try {
      if (this._isIgnorableScriptError(errorMessage, fileName, lineNumber, columnNumber, error)) {
        this.handleErrorInfo({
          log: `Ignoring error. Message: ${errorMessage}`,
          level: LogLevel.INFO
        });
        return;
      }
      if (error instanceof Error) {
        this.handle(error)
          .catch(error => {
            console.error('Error in global JavaScript error handler', error);
          });
        return;
      }
      let code = 'J00';
      let log = errorMessage + ' at ' + fileName + ':' + lineNumber + '\n(' + 'Code ' + code + ')';
      this.handleErrorInfo({
        code: code,
        message: errorMessage,
        log: log
      });
    } catch (err) {
      throw new Error('Error in global JavaScript error handler: ' + err.message + ' (original error: ' + errorMessage + ' at ' + fileName + ':' + lineNumber + ')');
    }
  }

  protected _isIgnorableScriptError(message: string, fileName?: string, lineNumber?: number, columnNumber?: number, error?: Error): boolean {
    // Ignore errors caused by scripts from a different origin.
    // Example: Firefox on iOS throws an error, probably caused by an internal Firefox script.
    // The error does not affect the application and cannot be prevented by the app either since we don't know what script it is and what it does.
    // In that case the error must not be shown to the user, instead just log it silently.
    // https://developer.mozilla.org/en-US/docs/Web/API/GlobalEventHandlers/onerror
    return message && message.toLowerCase().indexOf('script error') > -1 && !fileName && !lineNumber && !columnNumber && !error;
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
   * @param errorOrArgs error or array or array-like object containing the error and other arguments
   * @returns the analyzed errorInfo
   */
  handle(errorOrArgs: any | IArguments | any[], ...args: any[]): JQuery.Promise<any> {
    let error = errorOrArgs;
    if (errorOrArgs && args.length === 0) {
      if ((String(errorOrArgs) === '[object Arguments]')) {
        error = errorOrArgs[0];
        args = [...errorOrArgs].slice(1);
      } else if (Array.isArray(errorOrArgs)) {
        error = errorOrArgs[0];
        args = errorOrArgs.slice(1);
      }
    }
    return this.analyzeError(error, ...args)
      .then(this.handleErrorInfo.bind(this));
  }

  /**
   * Returns an "errorInfo" object for the given arguments. The following cases are handled:
   * 1. Error objects           (code: computed by getJsErrorCode())
   * 2. jQuery AJAX errors      (code: 'X' + HTTP status code)
   * 3. Nothing                 (code: 'P3')
   * 4. Everything else         (code: 'P4')
   */
  analyzeError(error?: any, ...args: any[]): JQuery.Promise<any> {
    let errorInfo = {
      code: null,
      message: null,
      stack: null,
      mappedStack: null,
      debugInfo: null,
      log: null,
      error: error
    };

    return this._analyzeError(errorInfo, ...args);
  }

  protected _analyzeError(errorInfo: ErrorInfo, ...args: any[]): JQuery.Promise<ErrorInfo> {
    let error = errorInfo.error;
    // 1. Regular errors
    if (error instanceof Error) {
      // Map stack first before analyzing the error
      return this.mapStack(error.stack)
        .catch(result => {
          errorInfo.mappingError = result.message + '\n' + result.error.message + '\n' + result.error.stack;
          return null;
        })
        .then(mappedStack => {
          errorInfo.mappedStack = mappedStack;
          this._analyzeRegularError(errorInfo);
          return errorInfo;
        });
    }

    // 2. Ajax errors
    if ($.isJqXHR(error) || (Array.isArray(error) && $.isJqXHR(error[0])) || error instanceof AjaxError) {
      this._analyzeAjaxError(errorInfo, ...args);
      return $.resolvedPromise(errorInfo);
    }

    // 3. No reason provided
    if (!error) {
      this._analyzeNoError(errorInfo);
      return $.resolvedPromise(errorInfo);
    }

    // 4. Everything else (e.g. when strings are thrown)
    this._analyzeOtherError(errorInfo);
    return $.resolvedPromise(errorInfo);
  }

  protected _analyzeRegularError(errorInfo: ErrorInfo) {
    let error = errorInfo.error as Error;
    errorInfo.code = this.getJsErrorCode(error);
    errorInfo.message = String(error.message || error);
    if (error.stack) {
      errorInfo.stack = String(error.stack);
    }
    if (error['debugInfo']) { // scout extension
      errorInfo.debugInfo = error['debugInfo'];
    }
    let stack = errorInfo.mappedStack || errorInfo.stack;
    let log = [];
    if (!stack || stack.indexOf(errorInfo.message) === -1) {
      // Only log message if not already included in stack
      log.push(errorInfo.message);
    }
    if (stack) {
      log.push(stack);
    }
    if (errorInfo.mappingError) {
      log.push(errorInfo.mappingError);
    }
    if (errorInfo.debugInfo) {
      // Error throwers may put a "debugInfo" string on the error object that is then added to the log string (this is a scout extension).
      log.push('----- Additional debug information: -----\n' + errorInfo.debugInfo);
    }
    errorInfo.log = arrays.format(log, '\n');
  }

  protected _analyzeAjaxError(errorInfo: ErrorInfo, ...args: any[]) {
    let error = errorInfo.error;
    let jqXHR, errorThrown, requestOptions;
    if (error instanceof AjaxError) {
      // Scout Ajax Error
      jqXHR = error.jqXHR;
      errorThrown = error.errorThrown;
      requestOptions = error.requestOptions; // scout extension
    } else {
      // jQuery $.ajax() error (arguments of the fail handler are: jqXHR, textStatus, errorThrown, requestOptions)
      // The first argument (jqXHR) is stored in errorInfo.error (may even be an array) -> create args array again and extract the parameters
      args = arrays.ensure(error).concat(args);
      jqXHR = args[0];
      errorThrown = args[2];
      requestOptions = args[3]; // scout extension
    }

    let ajaxRequest = (requestOptions ? strings.join(' ', requestOptions.type, requestOptions.url) : '');
    let ajaxStatus = (jqXHR.status ? strings.join(' ', jqXHR.status, errorThrown) : 'Connection error');

    errorInfo.code = 'X' + (jqXHR.status || '0');
    errorInfo.message = 'AJAX call' + strings.box(' "', ajaxRequest, '"') + ' failed' + strings.box(' [', ajaxStatus, ']');
    errorInfo.log = errorInfo.message;
    if (jqXHR.responseText) {
      errorInfo.debugInfo = 'Response text:\n' + jqXHR.responseText;
      errorInfo.log += '\n' + errorInfo.debugInfo;
    }
  }

  protected _analyzeOtherError(errorInfo: ErrorInfo) {
    let error = errorInfo.error;
    // Everything else (e.g. when strings are thrown)
    let s = (typeof error === 'string' || typeof error === 'number') ? String(error) : null;
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

  protected _analyzeNoError(errorInfo: ErrorInfo) {
    errorInfo.code = 'P3';
    errorInfo.message = 'Unknown error';
    errorInfo.log = 'Unexpected error (no reason provided)';
  }

  mapStack(stack: string): JQuery.Promise<string, { message: string; error: Error }> {
    let deferred = $.Deferred();
    try {
      sourcemappedStacktrace.mapStackTrace(stack, mappedStack => {
        deferred.resolve(arrays.format(mappedStack, '\n'));
      });
    } catch (e) {
      return $.rejectedPromise({message: 'Exception mapping failed', error: e});
    }

    return deferred.promise();
  }

  /**
   * Expects an object as returned by {@link analyzeError} and handles it:
   * - If the flag "logError" is set, the log message is printed to the console
   * - If there is a scout session and the flag "displayError" is set, the error is shown in a message box.
   * - If there is a scout session and the flag "sendError" is set, the error is sent to the UI server.
   */
  handleErrorInfo(errorInfo: ErrorInfo): ErrorInfo {
    errorInfo.level = scout.nvl(errorInfo.level, LogLevel.ERROR);
    if (this.logError && errorInfo.log) {
      this._logErrorInfo(errorInfo);
    }

    // Note: The error handler is installed globally, and we cannot tell in which scout session the error happened.
    // We simply use the first scout session to display the message box and log the error. This is not ideal in the
    // multi-session-case (portlet), but currently there is no other way. Besides, this feature is not in use yet.
    let session = this.session || App.get().sessions[0];
    if (session) {
      if (this.displayError && errorInfo.level === LogLevel.ERROR) {
        this._showMessageBox(session, errorInfo.message, errorInfo.code, errorInfo.log);
      }
      if (this.sendError) {
        this._sendErrorMessage(session, errorInfo.log, errorInfo.level);
      }
    }
    return errorInfo;
  }

  protected _logErrorInfo(errorInfo: ErrorInfo) {
    switch (errorInfo.level) {
      case LogLevel.TRACE:
        $.log.trace(errorInfo.log);
        break;
      case LogLevel.DEBUG:
        $.log.debug(errorInfo.log);
        break;
      case LogLevel.INFO:
        $.log.info(errorInfo.log);
        break;
      case LogLevel.WARN:
        $.log.warn(errorInfo.log);
        break;
      default:
        $.log.error(errorInfo.log);
    }

    // Note: when the null-logger is active it has already written the error to the console
    // when the $.log.error function has been called above, so we don't have to log again here.
    let writeToConsole = ErrorHandler.CONSOLE_OUTPUT;
    if ($.log instanceof NullLogger) {
      writeToConsole = false;
    }
    if (writeToConsole && window && window.console) {
      if (errorInfo.level === LogLevel.ERROR && window.console.error) {
        window.console.error(errorInfo.log);
      } else if (errorInfo.level === LogLevel.WARN && window.console.warn) {
        window.console.warn(errorInfo.log);
      } else if (window.console.log) {
        window.console.log(errorInfo.log);
      }
    }
  }

  /**
   * Generate a "cool looking" error code from the JS error object, that
   * does not reveal too much technical information, but at least indicates
   * that a JS runtime error has occurred. (In contrast, fatal errors from
   * the server have numeric error codes.)
   */
  getJsErrorCode(error?: Error): string {
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

  protected _showMessageBox(session: Session, errorMessage: string, errorCode: string, logMessage: string) {
    let options = {
      header: session.optText('ui.UnexpectedProblem', 'Internal UI Error'),
      body: strings.join('\n\n',
        session.optText('ui.InternalUiErrorMsg', errorMessage, ' (' + session.optText('ui.ErrorCodeX', 'Code ' + errorCode, errorCode) + ')'),
        session.optText('ui.UiInconsistentMsg', '')),
      yesButtonText: session.optText('ui.Reload', 'Reload'),
      yesButtonAction: scout.reloadPage,
      noButtonText: undefined,
      hiddenText: logMessage,
      iconId: icons.SLIPPERY
    };

    if (session.inDevelopmentMode) {
      options.noButtonText = session.optText('ui.Ignore', 'Ignore');
    }

    session.showFatalMessage(options, errorCode);
  }

  protected _sendErrorMessage(session: Session, logMessage: string, logLevel: LogLevel) {
    session.sendLogRequest(logMessage, logLevel);
  }
}
