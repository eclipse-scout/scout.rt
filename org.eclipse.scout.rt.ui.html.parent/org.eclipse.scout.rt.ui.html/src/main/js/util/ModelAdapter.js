scout.ModelAdapter = function() {
  this.session;
  this.parent;
  this.children = [];
  this._adapterProperties = [];
  this.rendered = false;
  this.destroyed = false;
};

scout.ModelAdapter.prototype.init = function(model, session) {
  this.session = session;

  // copy all properties from model to this adapter instance
  this._eachProperty(model, function(propertyName, value) {
    this[propertyName] = value;
  }.bind(this));

  this.session.registerModelAdapter(this);
};

// TODO AWE: underscore bei setter-func names entfernen, oder eventuell auf _render umbenennen?

scout.ModelAdapter.prototype.render = function($parent) {
  if (this.rendered) {
    throw 'Already rendered';
  }
  if (this.destroyed) {
    throw 'Object is destroyed';
  }
  this._render($parent);
  this._callSetters();
  this.rendered = true;
  if (this.session.offline) {
    this.goOffline();
  }
};

/**
 * Adds property name(s) of model properties which must be converted automatically to a model adapter.
 *
 * @param properties String or String-array with property names.
 */
scout.ModelAdapter.prototype._addAdapterProperties = function(properties) {
  if (Array.isArray(properties)) {
    this._adapterProperties = this._adapterProperties.concat(properties);
  } else {
    this._adapterProperties.push(properties);
  }
};

/**
 * This method creates the UI through DOM manipulation. At this point we should not apply model
 * properties on the UI, since sub-classes may need to contribute to the DOM first. You must not
 * apply model values to the UI here, since this is done in the _callSetters method later.
 * The default impl. does nothing.
 */
scout.ModelAdapter.prototype._render = function() {
  // NOP
};

/**
 * This method calls the UI setter methods after the _render method has been executed.
 * Here values of the model are applied to the DOM / UI. The default impl. does nothing.
 */
scout.ModelAdapter.prototype._callSetters = function() {
  // NOP
};

scout.ModelAdapter.prototype.remove = function() {
  var i, child;

  if (this.rendered) {
    for (i = 0; i < this.children.length; i++) {
      child = this.children[i];
      child.remove();
    }

    this._remove();
    this.rendered = false;
  }
  this.dispose();
};

scout.ModelAdapter.prototype._remove = function() {
  if (this.$container) {
    this.$container.remove();
    this.$container = null;
  }
};

scout.ModelAdapter.prototype.dispose = function() {
  // NOP implement to free resources
};

scout.ModelAdapter.prototype.destroy = function() {
  this.remove();

  //Disconnect from parent
  if (this.parent) {
    this.parent.removeChild(this);
    this.parent = null;
  }

  this.session.unregisterModelAdapter(this); //FIXME CGU unregister children?
  this.destroyed = true;
};

scout.ModelAdapter.prototype.addChild = function(childAdapter) {
  this.children.push(childAdapter);
};

scout.ModelAdapter.prototype.removeChild = function(childAdapter) {
  scout.arrays.remove(this.children, childAdapter);
};

/**
 * Loops through all properties of the given model (optional ignores the given properties).
 * Creates an ModelAdapter instance for the given property when the propertyName is in the
 * _adapterProperties array.
 */
scout.ModelAdapter.prototype._eachProperty = function(model, func, ignore) {
  var propertyName, value, i, j, adapter, adapters;

  //Loop through primitive properties
  for (propertyName in model) {
    if (ignore !== undefined && ignore.indexOf(propertyName) >= 0) {
      continue;
    }
    if (this._adapterProperties.indexOf(propertyName) > -1) {
      continue;
    }

    value = model[propertyName];
    func(propertyName, value);
  }

  //Loop through adapter properties in the order specified by the list _adapterProperties
  //Important: The server resolves the model adapters in alphabetic order. Currently we need to define the order by ourself. Maybe we should change to alphabetical order as well.
  for (i = 0; i < this._adapterProperties.length; i++) {
    propertyName = this._adapterProperties[i];
    value = model[propertyName];
    if (!value) {
      continue;
    }
    if (Array.isArray(value)) {
      adapters = [];
      for (j = 0; j < value.length; j++) {
        adapter = this.session.getOrCreateModelAdapter(value[j], this);
        this.onChildAdapterCreated(propertyName, adapter);
        adapters.push(adapter);
      }
      value = adapters;
    } else {
      value = this.session.getOrCreateModelAdapter(value, this);
      this.onChildAdapterCreated(propertyName, value);
    }

    func(propertyName, value);
  }
};

/**
 * Processes the JSON event from the server and sets dynamically properties on the adapter (-model)
 * and calls the right function to update the UI. For each property a corresponding function-name
 * must exist (property-name 'myValue', function-name 'setMyValue').
 *
 * This happes in two steps:
 * 1.) Synchronizing: Apply properties on adapter
 * 2.) Rendering: Call setter function to update UI
 *
 * You can always rely that these two steps are processed in that order, but you cannot rely that
 * individual properties are processed in a certain order.
 */
scout.ModelAdapter.prototype.onModelPropertyChange = function(event) {
  var oldValues = {};

  // step 1 synchronizing - apply properties on adapter or calls syncPropertyName if it exists
  this._syncProperties(oldValues, event.properties);

  // step 2 rendering - call setter methods to update UI, but only if it is displayed (rendered)
  if (this.rendered) {
    this._renderProperties(oldValues, event.properties);
  }
}; // TODO AWE: (form) jasmine-test this!

scout.ModelAdapter.prototype._syncProperties = function(oldValues, newValues, ignore) {
  this._eachProperty(newValues, function(propertyName, value) {
    var onFuncName = '_sync' + scout.ModelAdapter.preparePropertyNameForFunctionCal(propertyName);
    oldValues[propertyName] = value;

    if (this[onFuncName]) {
      this[onFuncName](value);
    } else {
      this[propertyName] = value;
    }
  }.bind(this), ignore);
};

scout.ModelAdapter.prototype._renderProperties = function(oldValues, newValues, ignore) {
  this._eachProperty(newValues, function(propertyName, value) {
    var setterFuncName = '_set' + scout.ModelAdapter.preparePropertyNameForFunctionCal(propertyName);
    $.log('call ' + setterFuncName + '(' + value + ')');

    if (this._adapterProperties.indexOf(propertyName) > -1 && this[propertyName]) {
      this.onChildAdapterChange(propertyName, oldValues[propertyName], value);
    }
    else {
      //Call the setter for regular properties, for adapters see onChildAdapterChange
      this[setterFuncName](value);
    }

  }.bind(this), ignore);
};

scout.ModelAdapter.preparePropertyNameForFunctionCal = function(propertyName) {
  return propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
};

/**
 * Removes the existing adapter specified by oldValue. Renders the new adapters if this.$container is set.<br>
 * To prevent this behavior just implement the method _setPropertyName or _unsetPropertyName (e.g _unsetTable).
 */
scout.ModelAdapter.prototype.onChildAdapterChange = function(propertyName, oldValue, newValue) {
  var funcName = scout.ModelAdapter.preparePropertyNameForFunctionCal(propertyName);
  var setFuncName = '_set' + funcName;
  var unsetFuncName = '_unset' + funcName;
  var i;

  if (!this[unsetFuncName]) {
    if (Array.isArray(oldValue)) {
      for (i = 0; i < oldValue.length; i++) {
        oldValue[i].remove();
      }
    } else {
      oldValue.remove();
    }
  } else {
    this[unsetFuncName]();
  }

  if (!this[setFuncName] && this.$container) {
    if (Array.isArray(oldValue)) {
      for (i = 0; i < oldValue.length; i++) {
        newValue[i].render(this.$container);
      }
    } else {
      newValue.render(this.$container);
    }
  } else {
    this[setFuncName](newValue);
  }
};

/**
 * Maybe overridden to influence creation. Default is emtpy.
 */
scout.ModelAdapter.prototype.onChildAdapterCreated = function(propertyName) {

};

scout.ModelAdapter.prototype.goOffline = function() {
  var i;
  for (i = 0; i < this.children.length; i++) {
    if (!this.children[i].rendered) {
      //going offline must not modify model state -> only necessary to inform rendered objects
      continue;
    }
    this.children[i].goOffline();
  }
};

scout.ModelAdapter.prototype.goOnline = function() {
  var i;
  for (i = 0; i < this.children.length; i++) {
    if (!this.children[i].rendered) {
      //going offline must not modify model state -> only necessary to inform rendered objects
      continue;
    }
    this.children[i].goOnline();
  }
};
