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

  // Adapter structure
  this.owner;
  this.ownedAdapters = [];
  this._adapterProperties = []; // FIXME [awe, cgu] 6.1 - hier löschen (nur noch auf Widget.js)

  /**
   * Widget properties which should be sent to server on property change.
   */
  this._remoteProperties = [];
  this._widgetListener;
};

// FIXME CGU [6.1] ev. renamen to RemoteAdapter
scout.ModelAdapter.prototype.init = function(model) {
  this._init(model);
  this.initialized = true;
};

/**
 * @param model expects parent session to be set. Other options:
 *   when not set, the default-value is true. When working with local objects (see LocalObject.js) the register flag is set to false.
 */
scout.ModelAdapter.prototype._init = function(model) {
  this.id = model.id;
  this.objectType = model.objectType;

  if (!model.owner) {
    throw new Error('Owner expected: ' + this);
  }
  this.owner = model.owner;
  this.session = model.session || this.owner.session;
  if (!this.session) {
    throw new Error('Session expected: ' + this);
  }

  this.session.registerModelAdapter(this);

  // Make a copy to prevent a modification of the given object
  this.model = $.extend({}, model);

  // Fill in the missing default values
  scout.defaultValues.applyTo(this.model);
};

scout.ModelAdapter.prototype.getOrCreateWidget = function(parent) {
  if (this.widget) {
//    this.widget.setParent() // FIXME CGU anstatt re-link in session.js?
    return this.widget;
  }
  this.model.parent = parent;
  this.model.remoteAdapter = this;
  this.widget = this._createWidget(this.model); // FIXME [awe] 6.1 - this.model delete nachdem widget existiert
  if (this.widget) { // FIXME CGU null check wegnehmen, davon ausgehen dass alle ein widget haben
    this._attachWidget();
  }
  return this.widget;
};

scout.ModelAdapter.prototype._createWidget = function(model) {
  return scout.create(this.objectType.replace('Adapter', ''), model);
};

scout.ModelAdapter.prototype._attachWidget = function() {
  if (this._widgetListener) {
    return;
  }
  this._widgetListener = {
    func: this._onWidgetEvent.bind(this)
  };
  this.widget.events.addListener(this._widgetListener);
};

scout.ModelAdapter.prototype._detachWidget = function() {
  if (!this._widgetListener) {
    return;
  }
  this.widget.events.removeListener(this._widgetListener);
  this._widgetListener = null;
};

/**
 * @returns Creates a scout.Event object from the current adapter instance and
 *   sends the event by using the Session#sendEvent() method.
 *
 * @param type of event
 * @param data of event
 * @param delay (optional) delay before event is sent. default 0
 * @param coalesceFunc (optional) coalesce function added to event-object
 */
scout.ModelAdapter.prototype._send = function(type, data, delay, coalesceFunc) {
  var event = new scout.Event(this.id, type, data);
  if (coalesceFunc) {
    event.coalesce = coalesceFunc;
  }
  this.session.sendEvent(event, delay);
};

/**
 * This method is used to directly send an event triggered by a Widget to the server.
 * Use this method in your _onWidgetEvent code when it makes no sense to implement an
 * own _sendXxx method.
 *
 * @param widgetEvent
 */
scout.ModelAdapter.prototype._sendWidgetEvent = function(widgetEvent) {
  this._send(widgetEvent.type, widgetEvent);
};

/**
 * Sends the current state of the given property to the server.
 */
scout.ModelAdapter.prototype._sendProperty = function(propertyName) {
  var data = {};
  data[propertyName] = this.widget[propertyName];
  this._send('property', data);
};

// FIXME CGU move to widget? maybe better to attach rendered listener and call setEnabled in goOffline case?
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

scout.ModelAdapter.prototype._addRemoteProperties = function(properties) {
  this._addProperties('_remoteProperties', properties);
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

  if (this.widget) {
    this._detachWidget();
    this.widget.destroy();
    this.widget = null;
  }
  this.session.unregisterModelAdapter(this);

  // Disconnect from owner
  if (this.owner) {
    this.owner.removeOwnedAdapter(this);
    this.owner = null;
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
    if (this._isAdapterProperty(propertyName)) {
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
//FIXME [6.1] CGU adjust java doc
scout.ModelAdapter.prototype.onModelPropertyChange = function(event) {
  this._syncPropertiesOnPropertyChange(event.properties);
};

/**
 * The default impl. only logs a warning that the event is not supported.
 */
scout.ModelAdapter.prototype.onModelAction = function(event) {
  $.log.warn('Model action "' + event.type + '" is not supported by model-adapter ' + this.objectType);
};

scout.ModelAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'propertyChange') {
    this._onWidgetPropertyChange(event);
  } else {
    // FIXME CGU [6.1] temporary, until model adapter separation - anmerkung von AWE: eigentlich ist das kein schlechter
    // default. Häufig gibt es events vom Widget, die man 1:1 an den server leiten will, ohne eine eigene _sendXxx Methode
    // zu implementieren. Siehe: _sendWidgetEvent
    if (event.sendToServer) {
      event = $.extend({}, event); // copy
      delete event.source;
      delete event.sendToServer;
      this._send(event.type, event);
    }
  }
};

scout.ModelAdapter.prototype._onWidgetPropertyChange = function(event) {
  event.changedProperties.forEach(function(propertyName) {
    if (this._isRemoteProperty(propertyName)) {
      var value = event.newProperties[propertyName];
      if (value && this._isAdapterProperty(propertyName)) {
//        value = value.modelAdapter; // FIXME CGU [6.1] get adapter for widget
      }
      this._sendProperty(propertyName, value);
    }
  }, this);
};

scout.ModelAdapter.prototype._syncPropertiesOnPropertyChange = function(newProperties) {
  this._eachProperty(newProperties, function(propertyName, value, isAdapterProp) {
    var ensureTypeFuncName = '_ensure' + scout.strings.toUpperCaseFirstLetter(propertyName),
      oldValue = this.widget[propertyName];

    // FIXME CGU [6.1] dieser Teil sollte irgendiwe in der Sync Funktion sein, anstatt callSetter syncProperty aufrufen, würde aber viele Funktionen brechen
    if (isAdapterProp && oldValue) {
      // TODO CGU this should actually be configurable, otherwise m_disposeOnChange=false on server doesn't work
      this._destroyAdapters(propertyName, oldValue, value);
    }

    if (this[ensureTypeFuncName]) {
      value = this[ensureTypeFuncName](value);
    }

    this.widget.callSetter(propertyName, value);

  }.bind(this));
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

scout.ModelAdapter.prototype._isAdapterProperty = function(propertyName) {
  return this._adapterProperties.indexOf(propertyName) > -1;
};

scout.ModelAdapter.prototype._isRemoteProperty = function(propertyName) {
  return this._remoteProperties.indexOf(propertyName) > -1;
};

scout.ModelAdapter.prototype.toString = function() {
  return 'ModelAdapter[objectType=' + this.objectType + ' id=' + this.id + ']';
};
