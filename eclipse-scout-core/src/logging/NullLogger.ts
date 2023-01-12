/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Logger, LogLevel, objects, strings} from '../index';

/**
 * A NullLogger instance is installed when Log4Javascript is not active (the popup
 * is not opened). In that case we write WARN, ERROR and FATAL to the console output
 * because otherwise some errors are hard to track. This is true especially for errors
 * that occur in a Promise. A developer should at least log these errors.
 */
export class NullLogger implements Logger {

  trace() {
    // NOP - don't log trace, we don't want to spam the browser console
  }

  debug() {
    // NOP - don't log debug, we don't want to spam the browser console
  }

  info(...logArgs: any[]) {
    this._log(LogLevel.INFO, logArgs);
  }

  warn(...logArgs: any[]) {
    this._log(LogLevel.WARN, logArgs);
  }

  error(...logArgs: any[]) {
    this._log(LogLevel.ERROR, logArgs);
  }

  fatal(...logArgs: any[]) {
    this._log(LogLevel.FATAL, logArgs);
  }

  isEnabledFor(): boolean {
    return false;
  }

  isTraceEnabled(): boolean {
    return false;
  }

  isDebugEnabled(): boolean {
    return false;
  }

  isInfoEnabled(): boolean {
    return false;
  }

  isWarnEnabled(): boolean {
    return false;
  }

  isErrorEnabled(): boolean {
    return false;
  }

  isFatalEnabled(): boolean {
    return false;
  }

  protected _log(level: LogLevel, logArgs: any[]) {
    // check if console is available
    let myConsole = objects.optProperty(window, 'console');
    if (!myConsole) {
      return;
    }

    // map level to log function
    let funcName;
    if (LogLevel.FATAL === level) {
      funcName = 'error';
    } else {
      funcName = level;
    }

    // check if log function exists on console
    let logFunc = myConsole[funcName];
    if (!logFunc) {
      return;
    }

    // log the message
    if (logArgs.length > 0) {
      logArgs[0] = this._formatTime() + ' [' + level.toUpperCase() + '] ' + logArgs[0];
    }
    try {
      logFunc.apply(myConsole, logArgs);
    } catch (e) {
      // NOP - this seems a bit paranoid, because we've already checked that the error function exists,
      // but some restrictive security settings in Internet Explorer may cause an Error when the function
      // is called. Our logger should not produce additional errors #249626.
    }
  }

  protected _formatTime(): string {
    let date = new Date();
    return strings.padZeroLeft(date.getHours(), 2) + ':' +
      strings.padZeroLeft(date.getMinutes(), 2) + ':' +
      strings.padZeroLeft(date.getSeconds(), 2) + '.' +
      strings.padZeroLeft(date.getMilliseconds(), 3);
  }
}
