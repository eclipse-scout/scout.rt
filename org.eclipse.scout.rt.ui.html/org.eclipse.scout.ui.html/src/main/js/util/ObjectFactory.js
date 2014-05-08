scout.ObjectFactory = function(session) {
  this.session = session;
  this._factories = {};

  this._init();
};

scout.ObjectFactory.prototype._init = function() {
  var that = this;
  this._factories['Table'] = {
    create : function(model) {
      return new scout.Table(that.session, model);
    }
  };
  this._factories['Form'] = {
    create : function(model) {
      return new scout.Form(that.session, model);
    }
  };
  this._factories['FormField'] = {
    create : function(model) {
      return new scout.FormField(that.session, model);
    }
  };
  this._factories['TableField'] = {
    create : function(model) {
      return new scout.TableField(that.session, model);
    }
  };
  this._factories['GroupBox'] = {
    create : function(model) {
      return new scout.GroupBox(that.session, model);
    }
  };

};

scout.ObjectFactory.prototype.create = function(model) {
  var factory = this._factories[model.objectType];
  if (!factory) {
    throw 'No factory registered for objectType ' + model.objectType;
  }

  return factory.create(model);
};

scout.ObjectFactory.prototype.register = function(objectType, factory) {
  this._factories[objectType] = factory;
};