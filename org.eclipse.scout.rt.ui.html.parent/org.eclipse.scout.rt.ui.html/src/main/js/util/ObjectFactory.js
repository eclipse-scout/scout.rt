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
    throw new Error('No factory registered for objectType ' + model.objectType);
  }

  var object = factory.create();
  object.init(model, this.session);
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

scout.defaultObjectFactories = [{
  objectType: 'Desktop',
  create: function() {
    return new scout.Desktop();
  }
}, {
  objectType: 'Outline',
  create: function() {
    return new scout.Outline();
  }
}, {
  objectType: 'ViewButton',
  create: function() {
    return new scout.DesktopViewButton();
  }
}, {
  objectType: 'ToolButton',
  create: function() {
    return new scout.DesktopToolButton();
  }
}, {
  objectType: 'DataModel',
  create: function() {
    return new scout.DataModel();
  }
},{
  objectType: 'ChartTableControl',
  create: function() {
    return new scout.ChartTableControl();
  }
}, {
  objectType: 'MapTableControl',
  create: function() {
    return new scout.MapTableControl();
  }
}, {
  objectType: 'GraphTableControl',
  create: function() {
    return new scout.GraphTableControl();
  }
}, {
  objectType: 'AnalysisTableControl',
  create: function() {
    return new scout.AnalysisTableControl();
  }
}, {
  objectType: 'Table',
  create: function() {
    return new scout.Table();
  }
}, {
  objectType: 'TableControl',
  create: function() {
    return new scout.TableControl();
  }
}, {
  objectType: 'TableOrganizeMenu',
  create: function() {
    return new scout.TableOrganizeMenu();
  }
}, {
  objectType: 'Tree',
  create: function() {
    return new scout.Tree();
  }
}, {
  objectType: 'Form',
  create: function() {
    return new scout.Form();
  }
}, {
  objectType: 'MessageBox',
  create: function() {
    return new scout.MessageBox();
  }
}, {
  objectType: 'Menu',
  create: function() {
    return new scout.Menu();
  }
}, {
  objectType: 'FormField',
  create: function() {
    return new scout.FormField();
  }
}, {
  objectType: 'Button',
  create: function() {
    return new scout.Button();
  }
}, {
  objectType: 'CheckBoxField',
  create: function() {
    return new scout.CheckBoxField();
  }
}, {
  objectType: 'NumberField',
  create: function() {
    return new scout.NumberField();
  }
}, {
  objectType: 'StringField',
  create: function() {
    return new scout.StringField();
  }
}, {
  objectType: 'SmartField',
  create: function() {
    return new scout.SmartField();
  }
}, {
  objectType: 'DateField',
  create: function() {
    return new scout.DateField();
  }
}, {
  objectType: 'TableField',
  create: function() {
    return new scout.TableField();
  }
},  {
  objectType: 'TreeField',
  create: function() {
    return new scout.TreeField();
  }
}, {
  objectType: 'GroupBox',
  create: function() {
    return new scout.GroupBox();
  }
}, {
  objectType: 'TabBox',
  create: function() {
    return new scout.TabBox();
  }
}, {
  objectType: 'SequenceBox',
  create: function() {
    return new scout.SequenceBox();
  }
}, { //FIXME CGU only needed temporarily, remove when switched to FormToolButton2
  objectType: 'Null',
  create: function() {
    return new scout.NullAdapter();
  }
}];
