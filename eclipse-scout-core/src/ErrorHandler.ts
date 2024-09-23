/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  AjaxError, App, arrays, DoEntity, icons, InitModelOf, LogLevel, MessageBox, MessageBoxActionEvent, ModelOf, NullLogger, NullWidget, numbers, ObjectModel, objects, ObjectWithType, scout, Session, Status, StatusSeverity, strings, texts
} from './index';
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
  /**
   * The original error. May be Error, AjaxError or any other type like string, number etc. since any object can be thrown.
   */
  error?: any;
  /**
   * Specifies if the error should be shown as fatal error or not.
   * If true, a fatal error is shown which forces the user to reload the page (unless in dev mode where it can be ignored).
   * If false, a normal messagebox with an ok button is shown and after confirmation the user is allowed to use the app again without reload.
   * The default is false.
   */
  showAsFatalError?: boolean;
  /**
   * The message of the error
   */
  message?: string;
  /**
   * The error code
   */
  code?: string;
  /**
   * The HTTP status code if the error comes from an HTTP request
   */
  httpStatus?: number;
  /**
   * If the error contains an ErrorDo (see ErrorDo.java)
   */
  errorDo?: ErrorDo;
  /**
   * The original stack trace
   */
  stack?: string;
  /**
   * Sourcemapped stack trace
   */
  mappedStack?: string;
  /**
   * If there stacktrace mapping to source-maps fails this field contains the cause.
   */
  mappingError?: string;
  /**
   * The full message to log. Typically consists of the message and e.g. a stack trace.
   */
  log?: string;
  /**
   * Specifies the level errors are logged to the console (and sent to the backend for logging). Default is {@link LogLevel.ERROR}.
   */
  level?: LogLevel;
  /**
   * Additional custom debug info. May come from an extra field on an Error thrown (Scout extension) or the response text from an ajax call.
   */
  debugInfo?: string;
}

/**
 * See org.eclipse.scout.rt.rest.error.ErrorDo
 */
export interface ErrorDo extends DoEntity {
  httpStatus?: number;
  errorCode?: string;
  title?: string;
  message?: string;
  correlationId?: string;
  severity?: string;
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
          showAsFatalError: true,
          level: LogLevel.INFO
        });
        return;
      }
      if (error instanceof Error) {
        this.analyzeError(error)
          .then(info => {
            info.showAsFatalError = true;
            return this.handleErrorInfo(info);
          })
          .catch(error => console.error('Error in global JavaScript error handler', error));
        return;
      }

      let code = 'J00';
      let log = errorMessage + ' at ' + fileName + ':' + lineNumber + '\n(' + 'Code ' + code + ')';
      this.handleErrorInfo({
        message: errorMessage,
        showAsFatalError: true,
        code: code,
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
  handle(errorOrArgs: any | IArguments | any[], ...args: any[]): JQuery.Promise<ErrorInfo> {
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
  analyzeError(error?: any, ...args: any[]): JQuery.Promise<ErrorInfo> {
    let errorInfo: ErrorInfo = {
      error: error,
      message: null,
      code: null,
      httpStatus: null,
      errorDo: null,
      stack: null,
      mappedStack: null,
      mappingError: null,
      log: null,
      debugInfo: null
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
    errorInfo.showAsFatalError = true;
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
    let jqXHR: JQuery.jqXHR, errorThrown: string, requestOptions: JQuery.AjaxSettings, errorDo: ErrorDo = null;
    if (error instanceof AjaxError) {
      // Scout Ajax Error
      jqXHR = error.jqXHR;
      errorThrown = error.errorThrown;
      requestOptions = error.requestOptions; // scout extension
      errorDo = error.errorDo;
    } else {
      // jQuery $.ajax() error (arguments of the fail handler are: jqXHR, textStatus, errorThrown, requestOptions)
      // The first argument (jqXHR) is stored in errorInfo.error (may even be an array) -> create args array again and extract the parameters
      args = arrays.ensure(error).concat(args);
      jqXHR = args[0];
      errorThrown = args[2];
      requestOptions = args[3]; // scout extension
      let errorDoCandidate = jqXHR?.responseJSON?.error;
      if (AjaxError.isErrorDo(errorDoCandidate)) {
        errorDo = errorDoCandidate;
      }
    }

    let ajaxRequest = (requestOptions ? strings.join(' ', requestOptions.type, requestOptions.url) : '');
    let ajaxStatus = (jqXHR.status ? strings.join(' ', jqXHR.status + '', errorThrown) : 'Connection error');

    errorInfo.httpStatus = jqXHR.status || 0;
    errorInfo.code = 'X' + errorInfo.httpStatus;
    errorInfo.errorDo = errorDo;

    if (errorDo) {
      errorInfo.message = errorDo.message;
      errorInfo.level = this._severityToLogLevel(errorDo.severity);
    }

    // logging info
    let req = 'AJAX call' + strings.box(' "', ajaxRequest, '"') + ' failed' + strings.box(' [', ajaxStatus, ']');
    let log = [];
    if (errorInfo.message) {
      log.push(errorInfo.message);
    } else {
      errorInfo.message = req;
    }
    log.push(req);
    if (jqXHR.responseText) {
      errorInfo.debugInfo = 'Response text:\n' + jqXHR.responseText;
      log.push(errorInfo.debugInfo);
    }
    errorInfo.log = arrays.format(log, '\n');
  }

  protected _severityToLogLevel(severity: string): LogLevel {
    switch (severity) {
      case 'ok':
        return LogLevel.DEBUG;
      case 'info':
        return LogLevel.INFO;
      case 'warning':
        return LogLevel.WARN;
      default:
        return LogLevel.ERROR;
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
      return $.rejectedPromise({message: 'stacktrace mapping failed', error: e});
    }

    return deferred.promise();
  }

  /**
   * Expects an object as returned by {@link analyzeError} and handles it:
   * - If the flag "logError" is set, the log message is printed to the console
   * - If there is a scout session and the flag "displayError" is set, the error is shown in a message box.
   * - If there is a scout session and the flag "sendError" is set, the error is sent to the UI server.
   */
  handleErrorInfo(errorInfo: ErrorInfo): JQuery.Promise<ErrorInfo> {
    errorInfo.level = scout.nvl(errorInfo.level, LogLevel.ERROR);
    if (this.logError && errorInfo.log) {
      this._logErrorInfo(errorInfo);
    }

    // Note: The error handler is installed globally, and we cannot tell in which scout session the error happened.
    // We simply use the first scout session to display the message box and log the error. This is not ideal in the
    // multi-session-case (portlet), but currently there is no other way. Besides, this feature is not in use yet.
    let session = this.session || App.get().sessions[0];
    if (session) {
      if (this.sendError) {
        this._sendErrorMessage(session, errorInfo.log, errorInfo.level);
      }
      if (this.displayError) {
        if (errorInfo.showAsFatalError) {
          if (errorInfo.level === LogLevel.ERROR) {
            return this._showInternalUiErrorMessageBox(session, errorInfo.message, errorInfo.code, errorInfo.log)
              .then(() => errorInfo);
          }
        } else {
          return this._showErrorMessageBox(session, errorInfo)
            .then(() => errorInfo);
        }
      }
    }
    return $.resolvedPromise(errorInfo);
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

  protected _showErrorMessageBox(session: Session, errorInfo: ErrorInfo): JQuery.Promise<MessageBoxActionEvent> {
    const parent = session.desktop || new NullWidget();
    const msgBoxModel: InitModelOf<MessageBox> = {
      parent,
      session,
      ...this._buildErrorMessageBoxModel(session, errorInfo)
    };
    const messageBox = scout.create(MessageBox, msgBoxModel);
    messageBox.on('action', event => messageBox.close());
    messageBox.render(session.$entryPoint); // do not use messageBox.open() as the Desktop might not yet be ready (e.g. if there is an error in the App startup)
    return messageBox.when('action');
  }

  protected _buildErrorMessageBoxModel(session: Session, errorInfo: ErrorInfo): ModelOf<MessageBox> {
    const status = this.errorInfoToStatus(session, errorInfo);
    let code: string = null;
    if (errorInfo.error instanceof Status && errorInfo.error.code !== 0) {
      code = errorInfo.error.code + '';
    }
    if (errorInfo?.errorDo?.errorCode && errorInfo.errorDo.errorCode !== '0') {
      code = errorInfo.errorDo.errorCode;
    }
    let body = status.message || session.optText('ui.UnexpectedProblem', 'Unexpected problem');
    if (code) {
      body += ' (' + session.optText('ui.ErrorCodeX', 'Code ' + code, code) + ').';
    }
    let html = null;
    if (errorInfo?.errorDo?.correlationId) {
      let $container = session.desktop?.$container || session.$entryPoint;
      let correlationIdText = session.optText('CorrelationId', 'Correlation ID');
      html = $container.makeDiv('error-popup-correlation-id', correlationIdText + ': ' + errorInfo.errorDo.correlationId)[0].outerHTML;
    }

    return {
      header: errorInfo.errorDo?.title,
      body: body,
      html: html,
      iconId: status.iconId,
      severity: status.severity,
      hiddenText: errorInfo.log,
      yesButtonText: session.optText('Ok', 'Ok')
    };
  }

  protected _showInternalUiErrorMessageBox(session: Session, errorMessage: string, errorCode: string, logMessage: string): JQuery.Promise<void> {
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

    return session.showFatalMessage(options, errorCode);
  }

  protected _sendErrorMessage(session: Session, logMessage: string, logLevel: LogLevel) {
    session.sendLogRequest(logMessage, logLevel);
  }

  errorInfoToStatus(session: Session, errorInfo: ErrorInfo): Status {
    if (errorInfo.error instanceof Status) {
      // if a Status is thrown
      return errorInfo.error;
    }
    return Status.ensure({
      message: this._errorInfoToStatusMessage(session, errorInfo),
      severity: this._errorInfoToStatusSeverity(errorInfo),
      code: this._errorInfoToStatusCode(errorInfo)
    });
  }

  protected _errorInfoToStatusCode(errorInfo: ErrorInfo): number {
    let errorCode = errorInfo?.errorDo?.errorCode;
    if (errorCode && errorCode !== '0' && objects.isNumber(errorCode)) {
      return numbers.ensure(errorCode);
    }
    return errorInfo.httpStatus;
  }

  protected _errorInfoToStatusSeverity(errorInfo: ErrorInfo): StatusSeverity {
    switch (errorInfo?.errorDo?.severity) {
      case 'ok':
        return Status.Severity.OK;
      case 'info':
        return Status.Severity.INFO;
      case 'warning':
        return Status.Severity.WARNING;
      default:
        return Status.Severity.ERROR;
    }
  }

  protected _errorInfoToStatusMessage(session: Session, errorInfo: ErrorInfo): string {
    if (typeof errorInfo.error === 'string') {
      return errorInfo.error;
    }
    const errorDo = errorInfo.errorDo;
    if (errorDo) {
      let body = errorDo.message;
      if (body && body !== 'undefined') {
        // ProcessingException has default text 'undefined' which is not very helpful -> don't use it
        return body;
      }
    }
    if (errorInfo.error?.message) {
      return errorInfo.error.message;
    }
    if (errorInfo.message) {
      return errorInfo.message;
    }
    return this.getMessageBodyForHttpStatus(errorInfo?.httpStatus, session);
  }

  /**
   * Gets the default error message body for the given HTTP status error code.
   * @param httpStatus The HTTP status error code. E.g. 503 (Service Unavailable)
   * @param session An optional Session for a message in the language of the user. The default language is used if omitted.
   * @returns The error message for the given error HTTP status.
   */
  getMessageBodyForHttpStatus(httpStatus: number, session?: Session): string {
    let body: string;
    let textMap = session ? session.textMap : texts.get('default');
    switch (httpStatus) {
      case 404: // Not Found
        body = textMap.optGet('TheRequestedResourceCouldNotBeFound', 'The requested resource could not be found.');
        break;
      case 502: // Bad Gateway
      case 503: // Service Unavailable
      case 504: // Gateway Timeout
        body = textMap.optGet('NetSystemsNotAvailable', 'The system is partially unavailable at the moment.')
          + '\n\n'
          + textMap.optGet('PleaseTryAgainLater', 'Please try again later.');
        break;
      case 403: // Forbidden
        body = textMap.optGet('YouAreNotAuthorizedToPerformThisAction', 'You are not authorized to perform this action.');
        break;
      default:
        body = textMap.optGet('ui.UnexpectedProblem', 'Unexpected problem');
    }
    return body;
  }
}
