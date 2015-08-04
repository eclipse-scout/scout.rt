scout.OfflineSession = function($entryPoint, uiSessionId) {
  this.$entryPoint = $entryPoint;
  this.uiSessionId = uiSessionId;
  this.detachHelper = new scout.DetachHelper(this);
  this._sendHandler;
};

scout.OfflineSession.createFromSession = function(session) {
  return new scout.OfflineSession(session.$entryPoint, session.uiSessionId);
};

/**
 * Override this method to do something when send is called.
 */
scout.OfflineSession.prototype.send = function(vararg, type, data, delay) {
  if (typeof vararg === 'function') {
    this._sendHandler = vararg;
  } else {
    this._sendHandler(/*target*/ vararg, type, data, delay);
  }
};

scout.OfflineSession.prototype.registerModelAdapter = function(modelAdapter) {
  // NOP
};
