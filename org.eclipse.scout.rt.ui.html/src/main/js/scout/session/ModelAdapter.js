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
  this._adapterProperties = []; // FIXME [awe, cgu] 6.1 - hier löschen (nur noch auf Widget.js)

  /**
   * Widget properties which should be sent to server on property change.
   */
  this._remoteProperties = [];
  this._widgetListener;

  this._propertyChangeEventFilter = new scout.PropertyChangeEventFilter();
  this._widgetEventTypeFilter = new scout.WidgetEventTypeFilter();
  this.eventFilters = [this._propertyChangeEventFilter, this._widgetEventTypeFilter];
};

// FIXME CGU [6.1] ev. renamen to RemoteAdapter
scout.ModelAdapter.prototype.init = function(model) {
  this._init(model);
  this.initialized = true;
};

/**
 * @param model expects a plain-object with properties: id, session
 */
scout.ModelAdapter.prototype._init = function(model) {
  scout.objects.mandatoryParameter('id', model.id);
  scout.objects.mandatoryParameter('session', model.session);
  $.extend(this, model);
  this.session.registerModelAdapter(this);
};

scout.ModelAdapter.prototype.destroy = function() {
  this._detachWidget();
  this.widget.destroy();
  this.widget = null;
  this.session.unregisterModelAdapter(this);
  this.destroyed = true;
};

scout.ModelAdapter.prototype.createWidget = function(adapterData, parent) {
  var model = this._prepareModel(adapterData, parent);
  this.widget = this._createWidget(model);
  this._attachWidget();
  return this.widget;
};

scout.ModelAdapter.prototype._prepareModel = function(model, parent) {
  // Make a copy to prevent a modification of the given model
  model = $.extend({}, model);

  // Fill in the missing default values
  scout.defaultValues.applyTo(model);

  model.parent = parent;
  model.remoteAdapter = this;

  if (model.owner !== undefined) {
    // Prefer the owner sent by the server
    model.owner = this.session.getModelAdapter(model.owner).widget;
    if (!model.owner) {
      throw new Error('owner not found.');
    }
  }
  return model;
};

/**
 * @returns A new widget instance. The default impl. uses calls scout.create() with property objectType from given model.
 */
scout.ModelAdapter.prototype._createWidget = function(model) {
  return scout.create(model.objectType, model);
};

scout.ModelAdapter.prototype._attachWidget = function() {
  if (this._widgetListener) {
    return;
  }
  this._widgetListener = {
    func: this._onWidgetEventInternal.bind(this)
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

// FIXME [6.1] cgu move to widget? still needed?
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

scout.ModelAdapter.prototype.goOffline = function() {
  this.widget.children.forEach(function(child) {
    if (!child.rendered) {
      // going offline must not modify model state -> only necessary to inform rendered objects
      return;
    }
    if (!child.remoteAdapter) {
      return;
    }
    child.remoteAdapter.goOffline();
  }, this);
  this._goOffline();
};

scout.ModelAdapter.prototype._goOffline = function() {
  // NOP may be implemented by subclasses
};

scout.ModelAdapter.prototype.goOnline = function() {
  this.widget.children.forEach(function(child) {
    if (!child.rendered) {
      // going online must not modify model state -> only necessary to inform rendered objects
      return;
    }
    if (!child.remoteAdapter) {
      return;
    }
    child.remoteAdapter.goOnline();
  }, this);
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

scout.ModelAdapter.prototype.addFilterForWidgetEventType = function(eventType) {
  this._widgetEventTypeFilter.addFilterForEventType(eventType);
};

scout.ModelAdapter.prototype._isPropertyChangeEventFiltered = function(propertyName, value) {
  return this._propertyChangeEventFilter.filter(propertyName, value);
};

scout.ModelAdapter.prototype._isWidgetEventFiltered = function(event) {
  return this._widgetEventTypeFilter.filter(event);
};

scout.ModelAdapter.prototype.resetEventFilters = function() {
  this.eventFilters.forEach(function(filter) {
    filter.reset();
  });
};

scout.ModelAdapter.prototype._onWidgetPropertyChange = function(event) {
  event.changedProperties.forEach(function(propertyName) {
    var value = event.newProperties[propertyName];

    if (this._isPropertyChangeEventFiltered(propertyName, value)) {
      return;
    }

    if (this._isRemoteProperty(propertyName)) {
      if (value && this._isAdapterProperty(propertyName)) {
        value = value.remoteAdapter;
      }
      this._callSendProperty(propertyName, value);
    }
  }, this);
};

scout.ModelAdapter.prototype._callSendProperty = function(propertyName, value) {
  var sendFuncName = '_send' + scout.strings.toUpperCaseFirstLetter(propertyName);
  if (this[sendFuncName]) {
    this[sendFuncName](value);
  } else {
    this._sendProperty(propertyName, value);
  }
};

scout.ModelAdapter.prototype._onWidgetDestroy = function() {
  this.destroy();
};

/**
 * Do not override this method. Widget event filtering is done here, before _onWidgetEvent is called.
 * @param event
 */
scout.ModelAdapter.prototype._onWidgetEventInternal = function(event) {
  if (!this._isWidgetEventFiltered(event)) {
    this._onWidgetEvent(event);
  }
};

scout.ModelAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'destroy') {
    this._onWidgetDestroy(event);
  } else if (event.type === 'propertyChange') {
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

scout.ModelAdapter.prototype._syncPropertiesOnPropertyChange = function(newProperties) {
  for (var propertyName in newProperties) {
    var value = newProperties[propertyName];

    // Call the setter of the widget
    this.widget.callSetter(propertyName, value);
  }
};

/**
 * Processes the JSON event from the server and calls the corresponding setter of the widget for each property.
 */
scout.ModelAdapter.prototype.onModelPropertyChange = function(event) {
  this._propertyChangeEventFilter.addFilterForProperties(event.properties);
  this._syncPropertiesOnPropertyChange(event.properties);
};

/**
 * The default impl. only logs a warning that the event is not supported.
 */
scout.ModelAdapter.prototype.onModelAction = function(event) {
  $.log.warn('Model action "' + event.type + '" is not supported by model-adapter ' + this.objectType);
};

scout.ModelAdapter.prototype.toString = function() {
  return 'ModelAdapter[objectType=' + this.objectType + ' id=' + this.id + ']';
};
