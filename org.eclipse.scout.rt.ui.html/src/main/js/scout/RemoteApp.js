scout.RemoteApp = function() {
  scout.RemoteApp.parent.call(this);
  this.remote = true;
};
scout.inherits(scout.RemoteApp, scout.App);

/**
 * @override
 */
scout.RemoteApp.prototype._doBootstrap = function(options) {
  return [
    scout.device.bootstrap(),
    scout.defaultValues.bootstrap(),
    scout.fonts.bootstrap(options.fonts)
  ];
};

scout.RemoteApp.prototype._createErrorHandler = function() {
  return scout.create('ErrorHandler', {
    sendError: true
  });
};

/**
 * @override
 */
scout.RemoteApp.prototype._loadSession = function($entryPoint, options) {
  options = options || {};
  options.remote = true;
  options.$entryPoint = $entryPoint;
  var session = this._createSession(options);
  session.start();
  return session;
};
