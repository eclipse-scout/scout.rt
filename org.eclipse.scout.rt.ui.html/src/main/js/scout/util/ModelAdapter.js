scout.ModelAdapter = function() {
  scout.ModelAdapter.parent.call(this);
  this.session;
  this._adapterProperties = [];
  this.ui;

  // Adapter structure
  this.owner;
  this.ownedAdapters = [];
};
scout.inherits(scout.ModelAdapter, scout.Widget);

scout.ModelAdapter.prototype.init = function(model, session) {
  this.session = session;
  this.id = model.id;
  this.session.registerModelAdapter(this);

  // copy all properties from model to this adapter instance
  this._eachProperty(model, function(propertyName, value, isAdapterProp) {
    if (isAdapterProp && value) {
      value = this._createAdapters(propertyName, value);
    }
    this[propertyName] = value;
  }.bind(this));

  this.keyStrokeAdapter = this._createKeyStrokeAdapter();
  // Fill in the missing default values
  scout.defaultValues.applyTo(this);
};

scout.ModelAdapter.prototype.render = function($parent) {
  if (this.ui) {
    throw new Error('Already rendered: ' + this);
  }
  scout.ModelAdapter.parent.prototype.render.call(this, $parent);
  if (this.session.offline) {
    this.goOffline();
  }
};

scout.ModelAdapter.prototype._renderInternal = function($parent) {
  this.ui = this._createUi();
  if (this.ui) {
    this.ui.render($parent);
  } else {
    scout.ModelAdapter.parent.prototype._renderInternal.call(this, $parent);
  }
};

scout.ModelAdapter.prototype._remove = function() {
  scout.ModelAdapter.parent.prototype._remove.call(this);
  if (this.ui) {
    this.ui.remove();
    this.ui = undefined;
  }
};

/**
 * @returns the UI widget to use when rendering the model adapter
 */
scout.ModelAdapter.prototype._createUi = function() {
  return null;
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

scout.ModelAdapter.prototype.destroy = function() {
  var ownedAdapters = this.ownedAdapters.slice();
  for (var i = 0; i < ownedAdapters.length; i++) {
    var ownedAdapter = ownedAdapters[i];
    ownedAdapter.destroy();
  }

  this.remove();
  this.session.unregisterModelAdapter(this);

  // Disconnect from owner
  if (this.owner) {
    this.owner.removeOwnedAdapter(this);
    this.owner = null;
  }
  // Disconnect from parent (adapter is being destroyed, it will never be rendered again)
  if (this.parent) {
    this.parent.removeChild(this);
    this.parent = null;
  }
  this.destroyed = true;
};

scout.ModelAdapter.prototype.addOwnedAdapter = function(ownedAdapter) {
  $.log.trace('addOwnedAdapter(' + ownedAdapter + ') to ' + this);
  this.ownedAdapters.push(ownedAdapter);
};

scout.ModelAdapter.prototype.removeOwnedAdapter = function(ownedAdapter) {
  $.log.trace('removeOwnedAdapter(' + ownedAdapter + ') from ' + this);
  scout.arrays.remove(this.ownedAdapters, ownedAdapter);
};

/**
 * Loops through all properties of the given model. Creates an ModelAdapter instance
 * for the given property when the propertyName is in the _adapterProperties array.
 */
scout.ModelAdapter.prototype._eachProperty = function(model, func) {
  var propertyName, value, i;

  // Loop through primitive properties
  for (propertyName in model) {
    if (this._adapterProperties.indexOf(propertyName) > -1) {
      continue; // will be handled below
    }
    value = model[propertyName];
    func(propertyName, value);
  }

  //Loop through adapter properties (any order will do).
  for (i = 0; i < this._adapterProperties.length; i++) {
    propertyName = this._adapterProperties[i];
    value = model[propertyName];
    if (value === undefined) {
      continue;
    }

    func(propertyName, value, true);
  }
};

scout.ModelAdapter.prototype._createAdapters = function(propertyName, adapterIds) {
  return this._processAdapters(adapterIds, function(adapterId) {
    var adapter = this.session.getOrCreateModelAdapter(adapterId, this);
    this.onChildAdapterCreated(propertyName, adapter);
    return adapter;
  }.bind(this));
};

scout.ModelAdapter.prototype._destroyAdapters = function(propertyName, oldAdapters, newAdapterIds) {
  return this._processAdapters(oldAdapters, function(oldAdapter) {
    // Only destroy it if its linked to this adapter (-> don't destroy global adapters)
    if (oldAdapter.owner !== this) {
      return;
    }

    if (Array.isArray(newAdapterIds)) {
      // If the old adapter is not in the array anymore -> destroy it
      if (newAdapterIds.indexOf(oldAdapter.id) < 0) {
        oldAdapter.destroy();
      }
    } else {
      // If the value is not an array, always destroy the oldAdapter
      oldAdapter.destroy();
    }
    return oldAdapter;
  }.bind(this));
};

/**
 * If the value is an array: Loops through the array and calls func.
 * If the value is not an array: Calls the func.
 * @returns the processed adapters (either a list or a single adapter) returned by func.
 */
scout.ModelAdapter.prototype._processAdapters = function(value, func) {
  var adapters, adapter, i;
  if (Array.isArray(value)) {
    adapters = [];
    for (i = 0; i < value.length; i++) {
      adapter = func(value[i]);
      adapters.push(adapter);
    }
    return adapters;
  } else {
    return func(value);
  }
};

/**
 * Processes the JSON event from the server and sets dynamically properties on the adapter (-model)
 * and calls the right function to update the UI. For each property a corresponding function-name
 * must exist (property-name 'myValue', function-name 'setMyValue').
 *
 * This happens in two steps:
 * 1.) Synchronizing: when a sync[propertyName] method exists, call that method - otherwise simply set the property [propertyName]
 * 2.) Rendering: Call render[propertyName] function to update UI
 *
 * You can always rely that these two steps are processed in that order, but you cannot rely that
 * individual properties are processed in a certain order.
 */
scout.ModelAdapter.prototype.onModelPropertyChange = function(event) {
  var oldProperties = {};

  // step 1 synchronizing - apply properties on adapter or calls syncPropertyName if it exists
  this._syncPropertiesOnPropertyChange(oldProperties, event.properties);

  // step 2 rendering - call render methods to update UI, but only if it is displayed (rendered)
  if (this.rendered) {
    this._renderPropertiesOnPropertyChange(oldProperties, event.properties);
  }
};

/**
 * The default impl. only logs a warning that the event is not supported.
 */
scout.ModelAdapter.prototype.onModelAction = function(event) {
  $.log.warn('Model action "' + event.type + '" is not supported by model-adapter ' + this.objectType);
};

scout.ModelAdapter.prototype._syncPropertiesOnPropertyChange = function(oldProperties, newProperties) {
  this._eachProperty(newProperties, function(propertyName, value, isAdapterProp) {
    var onFuncName = '_sync' + scout.ModelAdapter._preparePropertyNameForFunctionCall(propertyName);
    var oldValue = this[propertyName];
    oldProperties[propertyName] = oldValue;

    if (isAdapterProp) {
      if (oldValue) {
        this._destroyAdapters(propertyName, oldValue, value);
      }
      if (value) {
        value = this._createAdapters(propertyName, value);
      }
    }

    if (this[onFuncName]) {
      this[onFuncName](value);
    } else {
      this[propertyName] = value;
    }
  }.bind(this));
};

scout.ModelAdapter.prototype._renderPropertiesOnPropertyChange = function(oldProperties, newProperties) {
  this._eachProperty(newProperties, function(propertyName, value, isAdapterProp) {
    var renderFuncName = '_render' + scout.ModelAdapter._preparePropertyNameForFunctionCall(propertyName);
    var oldValue = oldProperties[propertyName];
    var newValue = this[propertyName];
    $.log.debug('call ' + renderFuncName + '(' + value + ')');
    // Call the render function for regular properties, for adapters see onChildAdapterChange
    if (isAdapterProp) {
      this.onChildAdapterChange(propertyName, oldValue, newValue);
    } else {
      var funcTarget = this.ui || this;
      if (!funcTarget[renderFuncName]) {
        throw new Error('Render function ' + renderFuncName + ' does not exist in ' + (funcTarget === this ? 'model adapter' : 'UI'));
      } // FIXME AWE/CGU: value and oldValue should be switched to conform with other functions.
      // Or better create remove function as it is done with adapters? currently only "necessary" for AnalysisTableControl
      // Input von 08.04.15: z.Z. wird die _renderXxx Methode sehr uneinheitlich verwendet. Manche mit ohne Parameter, andere mit
      // 1 oder 2 Parameter. Dann gibt es noch Fälle (DateField.js) bei denen es nötig ist, render aufzurufen, aber mit einem
      // anderen Wert für xxx als this.xxx. Nur wenige benötigen den 2. Parameter für old-value (FormField#_renderCssClass).
      // Vorgeschlagene Lösung:
      // - renderXxx() ist grundsätzlich Parameterlos und verwendet this.xxx
      // - wenn jemand den old-value von this.xxx braucht, muss er sich diesen selber auf dem adapter merken
      // - wenn jemand die render methode mit anderen werten als this.xxx aufrufen können muss, implementiert er für
      //   diesen speziellen fall: function renderXxx(xxx) { xxx = xxx || this.xxx; ...
      funcTarget[renderFuncName](newValue, oldValue);
    }
  }.bind(this));
};

/**
 * Removes the existing adapter specified by oldValue. Renders the new adapters if this.$container is set.<br>
 * To prevent this behavior just implement the method _renderPropertyName or _removePropertyName (e.g _removeTable).
 */
scout.ModelAdapter.prototype.onChildAdapterChange = function(propertyName, oldValue, newValue) {
  var funcName = scout.ModelAdapter._preparePropertyNameForFunctionCall(propertyName);
  var renderFuncName = '_render' + funcName;
  var removeFuncName = '_remove' + funcName;
  var i;
  var funcTarget = this.ui || this;

  // Remove old adapter, if there is one
  if (oldValue) {
    if (!funcTarget[removeFuncName]) {
      if (Array.isArray(oldValue)) {
        for (i = 0; i < oldValue.length; i++) {
          oldValue[i].remove();
        }
      } else {
        oldValue.remove();
      }
    } else {
      funcTarget[removeFuncName](oldValue);
    }
  }

  // Render new adapter, if there is one
  if (newValue) {
    var $container = this.$container || (this.ui ? this.ui.$container : undefined);
    if (!funcTarget[renderFuncName] && $container) {
      if (Array.isArray(newValue)) {
        for (i = 0; i < newValue.length; i++) {
          newValue[i].render($container);
        }
      } else {
        newValue.render($container);
      }
    } else {
      funcTarget[renderFuncName](newValue);
    }
  }
};

/**
 * Maybe overridden to influence creation. Default is emtpy.
 */
scout.ModelAdapter.prototype.onChildAdapterCreated = function(propertyName) {
  // NOP may be implemented by subclasses
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
  this._goOffline();
};

scout.ModelAdapter.prototype._goOffline = function() {
  // NOP may be implemented by subclasses
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
  this._goOnline();
};

scout.ModelAdapter.prototype._goOnline = function() {
  // NOP may be implemented by subclasses
};

/**
 * Creates an id based on a class name. E.g.: smart-field -> SmartField-13
 */
scout.ModelAdapter.prototype._generateId = function(cssClass) {
  var i,
    idParts = cssClass.split('-');

  for (i = 0; i < idParts.length; i++) {
    idParts[i] = idParts[i].charAt(0).toUpperCase() + idParts[i].substring(1);
  }
  return idParts.join('') + '-' + this.id;
};

scout.ModelAdapter.prototype.toString = function() {
  return this.objectType + '[' + this.id + ']';
};

/* --- STATIC HELPERS ------------------------------------------------------------- */

/**
 * @memberOf scout.ModelAdapter
 */
scout.ModelAdapter._preparePropertyNameForFunctionCall = function(propertyName) {
  return propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
};
