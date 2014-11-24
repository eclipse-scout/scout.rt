/* global log4javascript */
scout.logging = {
  defaultLevel: 'trace',

  /***
   * Loads log4javascript.min.js if logging is enabled.
   *
   * @returns $.deferred
   */
  init: function() {
    var $deferred;
    var location = new scout.URL();
    var enabled = location.getParameter('logging');
    if (enabled) {
      $deferred = $.getCachedScript("res/log4javascript.min.js")
        .done(function(script, textStatus) {
          var logLevel = scout.logging.parseLevel(location.getParameter('logLevel'));
          if (!logLevel) {
            logLevel = scout.logging.parseLevel(scout.logging.defaultLevel);
          }
          log4javascript.getDefaultLogger().setLevel(logLevel);
          $.log = log4javascript.getDefaultLogger();
        });
    } else {
      $.log = new scout.NullLogger();
    }
    return $deferred;
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
