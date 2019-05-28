/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
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
  trace: function() {},
  debug: function() {},
  info: function() {},
  warn: function() {
    this._log('WARN', scout.objects.argumentsToArray(arguments));
  },
  error: function(logArgs) {
    this._log('ERROR', scout.objects.argumentsToArray(arguments));
  },
  fatal: function(logArgs) {
    this._log('FATAL', scout.objects.argumentsToArray(arguments));
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
    if (logArgs.length > 0) {
      logArgs[0] = this._formatTime() + ' [' + level + '] ' + logArgs[0];
    }
    if (window.console) {
      window.console.error.apply(window.console, logArgs);
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
