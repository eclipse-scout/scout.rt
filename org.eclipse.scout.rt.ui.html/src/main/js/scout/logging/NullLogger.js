scout.NullLogger = function() {

};

scout.NullLogger.prototype = {
  trace: function () {
  },
  debug: function () {
  },
  info: function () {
  },
  warn: function () {
  },
  error: function () {
  },
  fatal: function () {
  },
  isEnabledFor: function () {
    return false;
  },
  isTraceEnabled: function () {
    return false;
  },
  isDebugEnabled: function () {
    return false;
  },
  isInfoEnabled: function () {
    return false;
  },
  isWarnEnabled: function () {
    return false;
  },
  isErrorEnabled: function () {
    return false;
  },
  isFatalEnabled: function () {
    return false;
  }
};