/**
 * A ModelAdapter has a these naming-based contracts. Each property the model (=JSON data) has, is automatically
 * synchronized with the property with the same name in the ModelAdapter. When a property is synchronized it
 * happens in this defined order:
 *
 * <ol>
 * <li><b>_sync[propertyName](newValue, oldValue) method</b> [optional] if this method is present, it is called with the new- and the old value.
 *   Use this method to perform required conversions on the values provided by the model (for instance, convert a date-string into a date object),
 *   or use it when you have to do something based on the old-value.</li>
 * <li><b>Set property [propertyName]</b> if a _sync method is not present, the property is simply set. If the property is an adapter, as specified
 *   by the <code>_adapterProperties</code> list, the property is automatically transformed to an adapter instance.</li>
 * <li><b>_render[propertyName] method</b> at the point the _render method is called, the property is already set, so you can access its value
 *   by using this.[propertyName]. The _render method is required to update the UI based on the new property-value.</li>
 * </ol>
 */
scout.ModelAdapter = function() {
  scout.ModelAdapter.parent.call(this);

  this._adapterProperties = [];

  // Adapter structure
  this.owner;
  this.ownedAdapters = [];

  this.initialized = false;
  this._register = true;
  this.destroyed = false;
  this._addEventSupport();
  this._addKeyStrokeContextSupport();
};
scout.inherits(scout.ModelAdapter, scout.Widget);

/**
 * @param model
 * @param session
 * @param register (optional) when set to true the adapter instance is un-/registered in the modelAdapterRegistry of the session
 *   when not set, the default-value is true. When working with local objects (see LocalObject.js) the register flag is set to false.
 */
scout.ModelAdapter.prototype.init = function(model, session, register) {
  scout.ModelAdapter.parent.prototype.init.call(this, session);

  this.id = model.id;
  this.objectType = model.objectType;
  this.remoteHandler = session.send.bind(session);
  this._register = scout.helpers.nvl(register, true);
  if (this._register) {
    this.session.registerModelAdapter(this);
  }

  // copy all properties from model to this adapter
  this._eachProperty(model, function(propertyName, value, isAdapterProp) {
    if (scout.helpers.isOneOf(propertyName, 'id', 'session', 'objectType')) {
      return; // Ignore (already set manually above)
    }
    if (isAdapterProp && value) {
      value = this._createAdapters(propertyName, value);
    }
    this[propertyName] = value;
  }.bind(this));

  // Register model keystrokes.
  if (this.keyStrokes) {
    this.keyStrokeContext.registerKeyStroke(this.keyStrokes);
  }

  // Fill in the missing default values
  scout.defaultValues.applyTo(this);
  this._init(model, session);
  this._initKeyStrokeContext(this.keyStrokeContext);
  this.initialized = true;
  this.trigger('initialized');
};

scout.ModelAdapter.prototype._init = function(model, session) {
  // NOP
};

scout.ModelAdapter.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  // NOP
};

scout.ModelAdapter.prototype.render = function($parent) {
  scout.ModelAdapter.parent.prototype.render.call(this, $parent);
  if (this.session.offline) {
    this.goOffline();
  }
};

scout.ModelAdapter.prototype._renderInternal = function($parent) {
  scout.ModelAdapter.parent.prototype._renderInternal.call(this, $parent);
  this._renderUniqueId();
};

scout.ModelAdapter.prototype._renderUniqueId = function(qualifier, $target) {
  if (typeof qualifier !== 'string' && $target === undefined) {
    $target = qualifier;
    qualifier = undefined;
  }
  $target = $target || this.$container;
  if ($target && !$target.attr('id')) { // don't overwrite
    $target.attr('id', this.uniqueId(qualifier));
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
 * Removes  property name(s) of model properties which must be converted automatically to a model adapter.
 *
 * Only used for special cases (e.g. when a model adapter wraps another adapter).
 */
scout.ModelAdapter.prototype._removeAdapterProperties = function(properties) {
  if (Array.isArray(properties)) {
    scout.arrays.removeAll(this._adapterProperties, properties);
  } else {
    scout.arrays.remove(this._adapterProperties, properties);
  }
};

scout.ModelAdapter.prototype.destroy = function() {
  // destroy owned adapters in reverse order.
  this.ownedAdapters.slice().reverse().forEach(function(ownedAdapter) {
    ownedAdapter.destroy();
  });

  this.remove();
  if (this._register) {
    this.session.unregisterModelAdapter(this);
  }

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
  // Inform listeners
  this.trigger('destroy');
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

  // step 3 notify - fire propertyChange _after_ properties have been rendered. (This is important
  // to make sure the DOM is in the right state, when the propertyChange event is consumed.)
  // Note: A new event object has to be created, because it is altered in EventSuppor.trigger().
  this._fireBulkPropertyChange(oldProperties, event.properties);
};

scout.ModelAdapter.prototype._fireBulkPropertyChange = function(oldProperties, newProperties) {
  var propertyChangeEvent = {
      newProperties: newProperties,
      oldProperties: oldProperties,
      changedProperties: []
  };
  // To allow a listener to react only to properties that have really changed their value, we
  // calculate the list of "changedProperties". This may be relevant, when the value on the model
  // changes from A to B and back to A, which emits a property change event when in fact, the
  // property has not really changed for the UI.
  for (var prop in newProperties) {
    if (newProperties[prop] !== oldProperties[prop]) {
      propertyChangeEvent.changedProperties.push(prop);
    }
  }
  this.trigger('propertyChange', propertyChangeEvent);
};

/**
 * Fires a property change for a single property.
 */
scout.ModelAdapter.prototype._firePropertyChange = function(propertyName, oldValue, newValue) {
  if (!propertyName) {
    return;
  }
  var oldProperties = {},
    newProperties = {};
  oldProperties[propertyName] = oldValue;
  newProperties[propertyName] = newValue;
  this._fireBulkPropertyChange(oldProperties, newProperties);
};


/**
 * Sets the value of the property 'propertyName' to 'newValue' and then fires a propertyChange event for that property.
 */
scout.ModelAdapter.prototype._setProperty = function(propertyName, newValue) {
  if (!propertyName) {
    return;
  }
  var oldValue = this[propertyName];
  this[propertyName] = newValue;
  this._firePropertyChange(propertyName, oldValue, newValue);
};

/**
 * The default impl. only logs a warning that the event is not supported.
 */
scout.ModelAdapter.prototype.onModelAction = function(event) {
  $.log.warn('Model action "' + event.type + '" is not supported by model-adapter ' + this.objectType);
};

scout.ModelAdapter.prototype._syncPropertiesOnPropertyChange = function(oldProperties, newProperties) {
  this._eachProperty(newProperties, function(propertyName, value, isAdapterProp) {
    var syncFuncName = '_sync' + scout.ModelAdapter._preparePropertyNameForFunctionCall(propertyName),
      oldValue = this[propertyName];
    oldProperties[propertyName] = oldValue;

    if (isAdapterProp) {
      if (oldValue) {
        this._destroyAdapters(propertyName, oldValue, value);
      }
      if (value) {
        value = this._createAdapters(propertyName, value);
      }
    }

    if (this[syncFuncName]) {
      this[syncFuncName](value, oldValue);
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
      if (!this[renderFuncName]) {
        throw new Error('Render function ' + renderFuncName + ' does not exist in ' + this.toString());
      }
      // FIXME AWE/CGU: value and oldValue should be switched to conform with other functions.
      // Or better create remove function as it is done with adapters? currently only "necessary" for AnalysisTableControl
      // Input von 08.04.15: z.Z. wird die _renderXxx Methode sehr uneinheitlich verwendet. Manche mit ohne Parameter, andere mit
      // 1 oder 2 Parameter. Dann gibt es noch Fälle (DateField.js) bei denen es nötig ist, render aufzurufen, aber mit einem
      // anderen Wert für xxx als this.xxx. Nur wenige benötigen den 2. Parameter für old-value (FormField#_renderCssClass).
      // Vorgeschlagene Lösung:
      // - renderXxx() ist grundsätzlich Parameterlos und verwendet this.xxx
      // - wenn jemand den old-value von this.xxx braucht, muss er sich diesen selber auf dem adapter merken
      // - wenn jemand die render methode mit anderen werten als this.xxx aufrufen können muss, implementiert er für
      //   diesen speziellen fall: function renderXxx(xxx) { xxx = xxx || this.xxx; ...
      this[renderFuncName](newValue, oldValue);
    }
  }.bind(this));
};

/**
 * Removes the existing adapter specified by oldValue. Renders the new adapters if this.$container is set.<br>
 * To prevent this behavior just implement the method _renderPropertyName or _removePropertyName (e.g _removeTable).
 */
scout.ModelAdapter.prototype.onChildAdapterChange = function(propertyName, oldValue, newValue) {
  var i,
    funcName = scout.ModelAdapter._preparePropertyNameForFunctionCall(propertyName),
    renderFuncName = '_render' + funcName,
    removeFuncName = '_remove' + funcName;

  // Remove old adapter, if there is one
  if (oldValue) {
    if (!this[removeFuncName]) {
      if (Array.isArray(oldValue)) {
        for (i = 0; i < oldValue.length; i++) {
          oldValue[i].remove();
        }
      } else {
        oldValue.remove();
      }
    } else {
      this[removeFuncName](oldValue);
    }
  }

  // Render new adapter, if there is one
  if (newValue) {
    var $container = this.$container;
    if (!this[renderFuncName] && $container) {
      if (Array.isArray(newValue)) {
        for (i = 0; i < newValue.length; i++) {
          newValue[i].render($container);
        }
      } else {
        newValue.render($container);
      }
    } else {
      this[renderFuncName](newValue);
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
  for (i = 0; i < this.ownedAdapters.length; i++) {
    if (!this.ownedAdapters[i].rendered) {
      //going offline must not modify model state -> only necessary to inform rendered objects
      continue;
    }
    this.ownedAdapters[i].goOffline();
  }
  this._goOffline();
};

scout.ModelAdapter.prototype._goOffline = function() {
  // NOP may be implemented by subclasses
};

scout.ModelAdapter.prototype.goOnline = function() {
  var i;
  for (i = 0; i < this.ownedAdapters.length; i++) {
    if (!this.ownedAdapters[i].rendered) {
      //going offline must not modify model state -> only necessary to inform rendered objects
      continue;
    }
    this.ownedAdapters[i].goOnline();
  }
  this._goOnline();
};

scout.ModelAdapter.prototype._goOnline = function() {
  // NOP may be implemented by subclasses
};

/**
 * Returns a unique identifier for the modelAdapter, consisting of the object type,
 * the session's partId and the adapter ID. An optional qualifier argument allows
 * generation of multiple unique IDs per adapter.
 *
 * The return value is suitable for use in the HTML 'id' attribute.
 *
 * @see http://www.w3.org/TR/html5/dom.html#the-id-attribute
 */
scout.ModelAdapter.prototype.uniqueId = function(qualifier) {
  var s = 'scout.';
  if (!this.objectType && qualifier) {
    s += qualifier;
  }
  else {
    s += scout.helpers.nvl(this.objectType, 'NO_TYPE');
    if (qualifier) {
      s += '@' + qualifier;
    }
  }
  s +=  '[' + this.session.partId + '-' + scout.helpers.nvl(this.id, 'NO_ID') + ']';
  return s.replace(/\s/g, '');
};

scout.ModelAdapter.prototype.toString = function() {
  return 'ModelAdapter[objectType=' + this.objectType + ' id=' + this.id +
    ' super=' + scout.ModelAdapter.parent.prototype.toString.call(this) + ']';
};

/* --- STATIC HELPERS ------------------------------------------------------------- */

/**
 * @memberOf scout.ModelAdapter
 */
scout.ModelAdapter._preparePropertyNameForFunctionCall = function(propertyName) {
  return propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
};
