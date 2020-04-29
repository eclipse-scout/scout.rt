/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {objects, strings} from '../index';

/**
 * A NullLogger instance is installed when Log4Javascript is not active (the popup
 * is not opened). In that case we write WARN, ERROR and FATAL to the console output
 * because otherwise some errors are hard to track. This is true especially for errors
 * that occur in a Promise. A developer should at least log these errors.
 */
export default class NullLogger {

  constructor() {
  }

  trace() {
    // NOP - don't log trace, we don't want to spam the browser console
  }

  debug() {
    // NOP - don't log debug, we don't want to spam the browser console
  }

  info(...logArgs) {
    this._log('info', logArgs);
  }

  warn(...logArgs) {
    this._log('warn', logArgs);
  }

  error(...logArgs) {
    this._log('error', logArgs);
  }

  fatal(...logArgs) {
    this._log('fatal', logArgs);
  }

  isEnabledFor() {
    return false;
  }

  isTraceEnabled() {
    return false;
  }

  isDebugEnabled() {
    return false;
  }

  isInfoEnabled() {
    return false;
  }

  isWarnEnabled() {
    return false;
  }

  isErrorEnabled() {
    return false;
  }

  isFatalEnabled() {
    return false;
  }

  _log(level, logArgs) {
    // check if console is available
    let myConsole = objects.optProperty(window, 'console');
    if (!myConsole) {
      return;
    }

    // map level to log function
    let funcName;
    if ('fatal' === level) {
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

  _formatTime() {
    let date = new Date();
    return strings.padZeroLeft(date.getHours(), 2) + ':' +
      strings.padZeroLeft(date.getMinutes(), 2) + ':' +
      strings.padZeroLeft(date.getSeconds(), 2) + '.' +
      strings.padZeroLeft(date.getMilliseconds(), 3);
  }
}
