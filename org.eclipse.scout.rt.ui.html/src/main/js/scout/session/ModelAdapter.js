/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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

  // Adapter structure
  this.owner;
  this.ownedAdapters = [];
  this._adapterProperties = [];

  /**
   * This array contains the name of all model-properties. It is used to distinct between ModelAdapter properties
   * from the (server-side) model and other properties (like $container, etc.) which are added on the ModelAdapter
   * instance.
   */
  this._modelProperties = [];

  this._register = true;
  this.remoteHandler = scout.NullRemoteHandler;
  this._addKeyStrokeContextSupport();
  this._addEventSupport();
};
scout.inherits(scout.ModelAdapter, scout.Widget);

// NullRemoteHandler is used as default for local objects
// in place of this.session.sendEvent
scout.NullRemoteHandler = function() {
  // NOP
};

/**
 * @param model expects parent session to be set. Other options:
 *   _register: (optional) when set to true the adapter instance is un-/registered in the modelAdapterRegistry of the session
 *   when not set, the default-value is true. When working with local objects (see LocalObject.js) the register flag is set to false.
 */
scout.ModelAdapter.prototype._init = function(model) {
  scout.ModelAdapter.parent.prototype._init.call(this, model);
  this.id = model.id;
  this.objectType = model.objectType;
  this._register = scout.nvl(model._register, true);
  if (this._register) {
    this.session.registerModelAdapter(this);
    this.remoteHandler = this.session.sendEvent.bind(this.session);
  }

  // copy all properties from model to this adapter
  this._eachProperty(model, function(propertyName, value, isAdapterProp) {
    // if property is not yet in the array of property names -> add property
    // the same property should exist only once in the array
    if (this._modelProperties.indexOf(propertyName) === -1) {
      this._modelProperties.push(propertyName);
    }
    if (scout.isOneOf(propertyName, 'id', 'session', 'objectType')) {
      return; // Ignore (already set manually above)
    }
    if (isAdapterProp && value) {
      value = this._createAdapters(propertyName, value);
    }
    this[propertyName] = value;
  }.bind(this));

  // Fill in the missing default values
  scout.defaultValues.applyTo(this);
};

/**
 * @returns Creates a scout.Event object from the current adapter instance and
 *   sends the event by using the Session#sendEvent() method. Local objects may
 *   set a different remoteHandler to call custom code instead of the Session#sendEvent()
 *   method.
 *
 * @param type of event
 * @param data of event
 * @param delay (optional) delay before event is sent. default 0
 * @param coalesceFunc (optional) coalesce function added to event-object
 */
scout.ModelAdapter.prototype._send = function(type, data, delay, coalesceFunc) {
  var adapter = this,
    adapterId = this.id;

  // If adapter is a clone, get original adapter and get its id
  while (adapter.cloneOf) {
    adapter = adapter.cloneOf;
    adapterId = adapter.id;
  }

  var event = new scout.Event(adapterId, type, data);
  if (coalesceFunc) {
    event.coalesce = coalesceFunc;
  }
  adapter.remoteHandler(event, delay);
  this.trigger('send', event);
};

/**
 * Sends the current state of the given property to the server.
 */
scout.ModelAdapter.prototype._sendProperty = function(propertyName) {
  var data = {};
  data[propertyName] = this[propertyName];
  this._send(propertyName, data);
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
  this._addProperties('_adapterProperties', properties);
};

/**
 * Adds property name(s) of model properties. They're used when a model adpater is cloned (see #cloneAdapter()).
 * You only need to call this method for UI-only properties. Properties from the server-side model are automatically
 * added in the _init method of the model adpater.
 *
 * @param properties String or String-array with property names.
 */
scout.ModelAdapter.prototype._addModelProperties = function(properties) {
  this._addProperties('_modelProperties', properties);
};

scout.ModelAdapter.prototype._addProperties = function(propertyName, properties) {
  if (Array.isArray(properties)) {
    this[propertyName] = this[propertyName].concat(properties);
  } else {
    this[propertyName].push(properties);
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

/**
 * This method creates adapter instances for a given adapter-ID or an array of adapter-IDs.
 * In some cases the adapter-ID is already resolved and replaced by a ModelAdapter instance,
 * this happens when you use the ModelAdapter#extractModel() method. In that case we simply
 * use the provided instance and don't lookup the adapter by ID.
 */
scout.ModelAdapter.prototype._createAdapters = function(propertyName, adapterOrIds) {
  return this._processAdapters(adapterOrIds, function(adapterOrId) {
    var adapter, model;
    if (adapterOrId instanceof scout.ModelAdapter) {
      adapter = adapterOrId;
    } else {
      model = this.session.getAdapterData(adapterOrId);
      if (model) {
        // Allow the creator to adapt the model of the child adapter
        this._onChildAdapterCreation(propertyName, model);
      }
      adapter = this.session.getOrCreateModelAdapter(adapterOrId, this);
    }
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
  var oldProperties = {},
    preventRendering = [];

  // step 1 synchronizing - apply properties on adapter or calls syncPropertyName if it exists
  this._syncPropertiesOnPropertyChange(oldProperties, event.properties, preventRendering);

  // step 2 rendering - call render methods to update UI, but only if it is displayed (rendered)
  if (this.rendered) {
    this._renderPropertiesOnPropertyChange(oldProperties, event.properties, preventRendering);
  }

  // step 3 notify - fire propertyChange _after_ properties have been rendered. (This is important
  // to make sure the DOM is in the right state, when the propertyChange event is consumed.)
  // Note: A new event object has to be created, because it is altered in EventSuppor.trigger().
  this._fireBulkPropertyChange(oldProperties, event.properties);
};

/**
 * The default impl. only logs a warning that the event is not supported.
 */
scout.ModelAdapter.prototype.onModelAction = function(event) {
  $.log.warn('Model action "' + event.type + '" is not supported by model-adapter ' + this.objectType);
};

scout.ModelAdapter.prototype._syncPropertiesOnPropertyChange = function(oldProperties, newProperties, preventRendering) {
  this._eachProperty(newProperties, function(propertyName, value, isAdapterProp) {
    var syncFuncName = '_sync' + scout.ModelAdapter._preparePropertyNameForFunctionCall(propertyName),
      oldValue = this[propertyName];
    oldProperties[propertyName] = oldValue;

    if (isAdapterProp) {
      if (oldValue) {
        // TODO CGU this should actually be configurable, otherwise m_disposeOnChange=false on server doesn't work
        this._destroyAdapters(propertyName, oldValue, value);
      }
      if (value) {
        value = this._createAdapters(propertyName, value);
      }
    }

    if (this[syncFuncName]) {
      if (this[syncFuncName](value, oldValue) === false) {
        // _syncPropName may return false to prevent the rendering (e.g. if the property has not changed)
        // This may be useful for some properties with an expensive render method
        // Do not prevent if undefined is returned!
        preventRendering.push(propertyName);
      }
    } else {
      this[propertyName] = value;
    }
  }.bind(this));
};

scout.ModelAdapter.prototype._renderPropertiesOnPropertyChange = function(oldProperties, newProperties, preventRendering) {
  this._eachProperty(newProperties, function(propertyName, value, isAdapterProp) {
    if (preventRendering.indexOf(propertyName) > -1) {
      // Do not render if _syncPropName returned false
      return;
    }

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
      // TODO awe, cgu: (model-adapter) value and oldValue should be switched to conform with other functions.
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
 * Maybe overridden to adapt the model. Default is empty.
 */
scout.ModelAdapter.prototype._onChildAdapterCreation = function(propertyName, adapter) {
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
  } else {
    s += scout.nvl(this.objectType, 'NO_TYPE');
    if (qualifier) {
      s += '@' + qualifier;
    }
  }
  s += '[' + this.session.partId + '-' + scout.nvl(this.id, 'NO_ID') + ']';
  return s.replace(/\s/g, '');
};

/**
 * Creates a deep clone of the current adapter instance. For each adapter a local object is created.
 * The 'cloneOf' property of the local-object points to the original adapter. When the ModelAdapter#
 * _send() method sends events to the server, it uses the ID of the original adapter for cloned instances
 * so the original adapter/model is notified on the server.
 */
scout.ModelAdapter.prototype.cloneAdapter = function(modelOverride) {
  var cloneProperty, cloneAdapter, adapterProperty,
    cloneModel = modelOverride || {};

  // #1 - clone model (excl. all adapter properties since they require a parent instance
  this._modelProperties.forEach(function(propertyName) {
    if (cloneModel.hasOwnProperty(propertyName)) {
      // NOP - when property is already set by modelOverride
    } else if ('id' === propertyName) {
      // must set ID to undefined - so scout#_createLocalObject will
      // create a new unique ID for the cloned adapter. You can still
      // pass an ID by the modelOverride argument.
      cloneModel[propertyName] = undefined;
    } else if (this._isAdapterProperty(propertyName)) {
      // NOP - we deal with adapter properties below
    } else if ('_register' === propertyName) {
      // NOP - is initialized on create of adapter
    } else if (this.hasOwnProperty(propertyName)) {
      cloneModel[propertyName] = this[propertyName];
    }
  }, this);

  cloneAdapter = scout.create(cloneModel);

  // #2 - create child adapters, use cloneAdapter as parent
  this._adapterProperties.forEach(function(propertyName) {
    if (cloneModel.hasOwnProperty(propertyName)) {
      // NOP - when property is already set by modelOverride
    } else if (this.hasOwnProperty(propertyName)) {
      adapterProperty = this[propertyName];
      if(adapterProperty === null){
        cloneProperty = null;
      } else if (Array.isArray(adapterProperty)) {
        cloneProperty = [];
        adapterProperty.forEach(function(adapterPropertyElement) {
          cloneProperty.push(adapterPropertyElement.cloneAdapter({
            parent: cloneAdapter
          }));
        }, this);
      } else {
        cloneProperty = adapterProperty.cloneAdapter({
          parent: cloneAdapter
        });
      }
      cloneAdapter[propertyName] = cloneProperty;
    }
  }, this);

  this.session.registerAdapterClone(this, cloneAdapter);
  return cloneAdapter;
};

scout.ModelAdapter.prototype._isModelProperty = function(propertyName) {
  return this._modelProperties.indexOf(propertyName) > -1;
};

scout.ModelAdapter.prototype._isAdapterProperty = function(propertyName) {
  return this._adapterProperties.indexOf(propertyName) > -1;
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
