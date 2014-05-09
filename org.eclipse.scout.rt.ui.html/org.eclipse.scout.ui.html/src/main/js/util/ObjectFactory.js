scout.ObjectFactory = function(session) {
  this.session = session;
  this._factories = {};
  this.defaultDeviceType = scout.UserAgent.DEVICE_TYPE_DESKTOP;
};

/**
 * @param model needs to contain property objectType
 */
scout.ObjectFactory.prototype.create = function(model) {
  var currentDeviceType = this.session.userAgent.deviceType,
    factories, factory;

  factories = this._factories[currentDeviceType] || {};
  factory = factories[model.objectType];
  if (!factory && currentDeviceType !== this.defaultDeviceType) {
    factories = this._factories[this.defaultDeviceType] || {};
    factory = factories[model.objectType];
  }
  if (!factory) {
    throw 'No factory registered for objectType ' + model.objectType;
  }

  return factory.create(this.session, model);
};

/**
 * @param single factory or array of factories with objectType and optional deviceType.
 */
scout.ObjectFactory.prototype.register = function(factories) {
  if (!factories) {
    return;
  }

  if (!Array.isArray(factories)) {
    factories = [ factories ];
  }

  var i, factory;
  for (i = 0; i < factories.length; i++) {
    factory = factories[i];
    if (!factory.deviceType) {
      factory.deviceType = this.defaultDeviceType;
    }
    if(!this._factories[factory.deviceType]) {
      this._factories[factory.deviceType] = {};
    }
    this._factories[factory.deviceType][factory.objectType] = factory;
  }
};

scout.defaultObjectFactories = [ {
  objectType : 'Table',
  create : function(session, model) {
    return new scout.Table(session, model);
  }
}, {
  objectType : 'Form',
  create : function(session, model) {
    return new scout.Form(session, model);
  }
}, {
  objectType : 'FormField',
  create : function(session, model) {
    return new scout.FormField(session, model);
  }
}, {
  objectType : 'CheckBoxField',
  create : function(session, model) {
    return new scout.CheckBoxField(session, model);
  }
}, {
  objectType : 'TableField',
  create : function(session, model) {
    return new scout.TableField(session, model);
  }
}, {
  objectType : 'GroupBox',
  create : function(session, model) {
    return new scout.GroupBox(session, model);
  }
}, {
  objectType : 'SequenceBox',
  create : function(session, model) {
    return new scout.SequenceBox(session, model);
  }
} ];
