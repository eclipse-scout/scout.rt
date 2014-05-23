scout.ObjectFactory = function(session) {
  this.session = session;
  this._factories = {};

  this.deviceTypeLookupOrder = ['TABLET', 'MOBILE', 'DESKTOP'];
};

/**
 * @param model needs to contain property objectType
 */
scout.ObjectFactory.prototype.create = function(model) {
  var currentDeviceType = this.session.userAgent.deviceType,
    factories, factory, index, deviceType;

  index = this.deviceTypeLookupOrder.indexOf(currentDeviceType);

  for (index = index; index < this.deviceTypeLookupOrder.length || factory; index++) {
    deviceType = this.deviceTypeLookupOrder[index];
    factories = this._factories[deviceType] || {};
    factory = factories[model.objectType];
    if (factory) {
      break;
    }
  }

  if (!factory) {
    throw 'No factory registered for objectType ' + model.objectType;
  }

  return factory.create(model, this.session);
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

scout.defaultObjectFactories = [{
  objectType: 'Desktop',
  create: function(model, session) {
    return new scout.Desktop(model, session);
  }
}, {
  objectType: 'Table',
  create: function(model, session) {
    return new scout.Table(model, session);
  }
}, {
  objectType: 'Form',
  create: function(model, session) {
    return new scout.Form(model, session);
  }
}, {
  objectType: 'Menu',
  create: function(model, session) {
    return new scout.Menu(model, session);
  }
}, {
  objectType: 'FormField',
  create: function(model, session) {
    return new scout.FormField(model, session);
  }
}, {
  objectType: 'CheckBoxField',
  create: function(model, session) {
    return new scout.CheckBoxField(model, session);
  }
}, {
  objectType: 'TableField',
  create: function(model, session) {
    return new scout.TableField(model, session);
  }
}, {
  objectType: 'GroupBox',
  create: function(model, session) {
    return new scout.GroupBox(model, session);
  }
}, {
  objectType: 'SequenceBox',
  create: function(model, session) {
    return new scout.SequenceBox(model, session);
  }
}];
