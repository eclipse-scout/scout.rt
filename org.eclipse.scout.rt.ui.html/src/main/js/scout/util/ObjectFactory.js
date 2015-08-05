scout.ObjectFactory = function(session) {
  this.session = session;
  this._factories = {};

  this.deviceTypeLookupOrder = ['TABLET', 'MOBILE', 'DESKTOP'];
};

/**
 * @param model needs to contain property objectType
 * @param session (optional) if not set <code>this.session</code> is used as default. Basically this argument
 *   is used to replace the real, live session with an instance of OfflineSession.
 */
scout.ObjectFactory.prototype.create = function(model, session) {
  session = scout.helpers.nvl(session, this.session);

  var factories, factory, deviceType,
    index = this.deviceTypeLookupOrder.indexOf(currentDeviceType),
    currentDeviceType = session.userAgent.deviceType;

  for (index = index; index < this.deviceTypeLookupOrder.length || factory; index++) {
    deviceType = this.deviceTypeLookupOrder[index];
    factories = this._factories[deviceType] || {};
    factory = factories[model.objectType];
    if (factory) {
      break;
    }
  }

  if (!factory) {
    throw new Error('No factory registered for objectType ' + model.objectType);
  }
  var object = factory.create(model);
  object.init(model, session);
  return object;
};

/**
 * @param single factory or array of factories with objectType and optional deviceType.
 */
scout.ObjectFactory.prototype.register = function(factories) {
  if (!factories) {
    return;
  }

  if (!Array.isArray(factories)) {
    factories = [factories];
  }

  var i, factory;
  for (i = 0; i < factories.length; i++) {
    factory = factories[i];
    if (!factory.deviceType) {
      factory.deviceType = this.deviceTypeLookupOrder[this.deviceTypeLookupOrder.length - 1];
    }
    if (!this._factories[factory.deviceType]) {
      this._factories[factory.deviceType] = {};
    }
    this._factories[factory.deviceType][factory.objectType] = factory;
  }
};
