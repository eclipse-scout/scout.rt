/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/* global log4javascript */
scout.logging = {
  defaultLevel: 'trace',

  /***
   * Loads log4javascript.min.js if logging is enabled.
   *
   * @returns $.deferred
   */
  bootstrap: function() {
    var deferred;
    var location = new scout.URL();
    var enabled = location.getParameter('logging');
    if (enabled) {
      var initLog4Javascript = function() {
        var logLevel = scout.logging.parseLevel(location.getParameter('logLevel'));
        if (!logLevel) {
          logLevel = scout.logging.parseLevel(scout.logging.defaultLevel);
        }
        log4javascript.getDefaultLogger().setLevel(logLevel);
        $.log = log4javascript.getDefaultLogger();
      };
      if (typeof log4javascript === 'undefined') {
        // If log4javascript is not yet installed, dynamically load the library
        deferred = $.injectScript('res/log4javascript.min.js')
          .done(function(script, textStatus) {
            initLog4Javascript();
          });
      } else {
        initLog4Javascript();
      }
    } else {
      $.log = new scout.NullLogger();
    }
    return deferred;
  },

  parseLevel: function(level) {
    if (!level) {
      return;
    }
    level = level.toLowerCase();
    switch (level) {
      case 'trace':
        return log4javascript.Level.TRACE;
      case 'debug':
        return log4javascript.Level.DEBUG;
      case 'info':
        return log4javascript.Level.INFO;
      case 'warn':
        return log4javascript.Level.WARN;
      case 'error':
        return log4javascript.Level.ERROR;
      case 'fatal':
        return log4javascript.Level.FATAL;
    }
  }
};

scout.NullLogger = function() {
  // empty
};
scout.NullLogger.prototype = {
  trace: function() {},
  debug: function() {},
  info: function() {},
  warn: function() {},
  error: function() {},
  fatal: function() {},
  isEnabledFor: function(level) {
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
  }
};
