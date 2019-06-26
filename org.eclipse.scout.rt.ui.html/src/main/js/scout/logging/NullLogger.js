/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/

/**
 * A NullLogger instance is installed when Log4Javascript is not active (the popup
 * is not opened). In that case we write WARN, ERROR and FATAL to the console output
 * because otherwise some errors are hard to track. This is true especially for errors
 * that occur in a Promise. A developer should at least log these errors.
 */
scout.NullLogger = function() {
};

scout.NullLogger.prototype = {
  trace: function() {
    // NOP - don't log trace, we don't want to spam the browser console
  },
  debug: function() {
    // NOP - don't log debug, we don't want to spam the browser console
  },
  info: function() {
    this._log('info', scout.objects.argumentsToArray(arguments));
  },
  warn: function() {
    this._log('warn', scout.objects.argumentsToArray(arguments));
  },
  error: function(logArgs) {
    this._log('error', scout.objects.argumentsToArray(arguments));
  },
  fatal: function(logArgs) {
    this._log('fatal', scout.objects.argumentsToArray(arguments));
  },
  isEnabledFor: function() {
    return false;
  },
  isTraceEnabled: function() {
    return false;
  },
  isDebugEnabled: function() {
    return false;
  },
  isInfoEnabled: function() {
    return false;
  },
  isWarnEnabled: function() {
    return false;
  },
  isErrorEnabled: function() {
    return false;
  },
  isFatalEnabled: function() {
    return false;
  },
  _log: function(level, logArgs) {
    // check if console is available
    var myConsole = scout.objects.optProperty(window, 'console');
    if (!myConsole) {
      return;
    }

    // map level to log function
    var funcName;
    if ('fatal' === level) {
      funcName = 'error';
    } else {
      funcName = level;
    }

    // check if log function exists on console
    var logFunc = myConsole[funcName];
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
  },
  _formatTime: function() {
    var date = new Date();
    return scout.strings.padZeroLeft(date.getHours(), 2) + ':' +
      scout.strings.padZeroLeft(date.getMinutes(), 2) + ':' +
      scout.strings.padZeroLeft(date.getSeconds(), 2) + '.' +
      scout.strings.padZeroLeft(date.getMilliseconds(), 3);
  }
};
