/**
 * The LocalSession is used in place of the real, live Session.js in cases where
 * you want to create model-adapters on the client-side only, without a model
 * living on the client-server-side. Use the <code>send(function)</code> method to
 * register a callback which is called when the model-adapter calls a server-side
 * method.
 */
scout.LocalSession = function($entryPoint, uiSessionId, objectFactory, userAgent) {
  this.$entryPoint = $entryPoint;
  this.uiSessionId = uiSessionId;
  this.objectFactory = objectFactory;
  this.userAgent = userAgent;
  this.detachHelper = new scout.DetachHelper(this);
  this._sendHandler;
};

/**
 * Creates an offline session from the real, live Session instance. This is useful because it
 * sets the uiSessionId property which is also required by Focus- and KeyStrokeManager to work
 * properly. Also the objectFactory is copied, which is required when you want to use the
 * <code>createUiObject</code> method.
 */
scout.LocalSession.createFromSession = function(session) {
  return new scout.LocalSession(
      session.$entryPoint,
      session.uiSessionId,
      session.objectFactory,
      session.userAgent);
};

/**
 * Override this method to do something when send is called.
 */
scout.LocalSession.prototype.send = function(vararg, type, data, delay) {
  if (typeof vararg === 'function') {
    this._sendHandler = vararg;
  } else {
    this._sendHandler(/*target*/ vararg, type, data, delay);
  }
};

/**
 * LocalSession doesn't register its adapters.
 */
scout.LocalSession.prototype.registerModelAdapter = function(modelAdapter) {
  // NOP
};

/**
 * LocalSession doesn't register its adapters.
 */
scout.LocalSession.prototype.unregisterModelAdapter = function(modelAdapter) {
  // NOP
};

/**
 * Creates a new object instance based on the given model by using the object-factory.
 * This method should be used when you create Widgets or Adapters in the UI without a
 * model from the server-side client.
 *
 * The only model property required is 'objectType'. A unique ID is generated automatically,
 * when it is not provided by the model.
 */
scout.LocalSession.prototype.createUiObject = function(model) {
  if (!model || !model.objectType) {
    throw new Error('Missing objectType');
  }
  if (!this.objectFactory) {
    throw new Error('No objectFactory is registered for LocalSession. ' +
        'Try to create on LocalSession by calling the createFromSession(session) method');
  }
  if (model.id === undefined) {
    model.id = scout.createUniqueId();
  }
  return this.objectFactory.create(model, this);
};
