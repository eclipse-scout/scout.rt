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
 * A model adapter is the connector with the server, it takes the events sent from the server and calls the corresponding methods on the widget.
 * It also sends events to the server whenever an action happens on the widget.
 */
scout.ModelAdapter = function() {
  this.initialized = false;
  this.attached = false;
  this.destroyed = false;
  this.widget;
  this._enabledBeforeOffline = true;

  /**
   * Widget properties which should be sent to server on property change.
   */
  this._remoteProperties = [];
  this._widgetListener;

  this._propertyChangeEventFilter = new scout.PropertyChangeEventFilter();
  this._widgetEventTypeFilter = new scout.WidgetEventTypeFilter();
  this.events = new scout.EventSupport();
};

scout.ModelAdapter.prototype.init = function(model) {
  this._init(model);
  this.initialized = true;
};

/**
 * @param model expects a plain-object with properties: id, session
 */
scout.ModelAdapter.prototype._init = function(model) {
  scout.assertParameter('id', model.id);
  scout.assertParameter('session', model.session);
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
  var model = this._initModel(adapterData, parent);
  this.widget = this._createWidget(model);
  this._attachWidget();
  this._postCreateWidget();
  return this.widget;
};

/**
 * Override this method to do something right after the widget has been created and has been
 * attached to the remote adapter. The default impl. does nothing.
 */
scout.ModelAdapter.prototype._postCreateWidget = function() {
  // NOP
};

scout.ModelAdapter.prototype._initModel = function(model, parent) {
  // Make a copy to prevent a modification of the given model
  var deepCopy = this.session.adapterExportEnabled;
  model = $.extend(deepCopy, {}, model);

  // Fill in the missing default values
  scout.defaultValues.applyTo(model);

  model.parent = parent;
  model.owner = parent; // Set it explicitly because server sends owner in inspector mode -> ignore the owner sent by server.
  model.modelAdapter = this;

  if (model.global) {
    // Use the root adapter as owner if global is set to true
    model.owner = this.session.getModelAdapter('1').widget;
  }

  this._initProperties(model);

  return model;
};

/**
 * Override this method to call _sync* methods of the ModelAdapter _before_ the widget is created.
 */
scout.ModelAdapter.prototype._initProperties = function(model) {
  // NOP
};

/**
 * @returns A new widget instance. The default impl. uses calls scout.create() with property objectType from given model.
 */
scout.ModelAdapter.prototype._createWidget = function(model) {
  var widget = scout.create(model);
  widget._addCloneProperties(['modelClass', 'classId']);
  return widget;
};

scout.ModelAdapter.prototype._attachWidget = function() {
  if (this._widgetListener) {
    return;
  }
  this._widgetListener = {
    func: this._onWidgetEventInternal.bind(this)
  };
  this.widget.addListener(this._widgetListener);
  this.attached = true;
  this.events.trigger('attach');
};

scout.ModelAdapter.prototype._detachWidget = function() {
  if (!this._widgetListener) {
    return;
  }
  this.widget.removeListener(this._widgetListener);
  this._widgetListener = null;
  this.attached = false;
  this.events.trigger('detach');
};

scout.ModelAdapter.prototype.goOffline = function() {
  this.widget.visitChildren(function(child) {
    if (child.modelAdapter) {
      child.modelAdapter._goOffline();
    }
  });
};

scout.ModelAdapter.prototype._goOffline = function() {
  // NOP may be implemented by subclasses
};

scout.ModelAdapter.prototype.goOnline = function() {
  this.widget.visitChildren(function(child) {
    if (child.modelAdapter) {
      child.modelAdapter._goOnline();
    }
  });
};

scout.ModelAdapter.prototype._goOnline = function() {
  // NOP may be implemented by subclasses
};

scout.ModelAdapter.prototype.isRemoteProperty = function(propertyName) {
  return this._remoteProperties.indexOf(propertyName) > -1;
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
 * @returns Creates a scout.Event object from the current adapter instance and
 *   sends the event by using the Session#sendEvent() method. Local objects may
 *   set a different remoteHandler to call custom code instead of the Session#sendEvent()
 *   method.
 *
 * @param type of event
 * @param data of event
 * @param options (optional) options according to the following table:
 *
 * Option name         Default value   Description
 * -----------------------------------------------------------------------------------------
 * delay               0               Delay in milliseconds before the event is sent.
 *
 * coalesce            undefined       Coalesce function added to event-object.
 *
 * showBusyIndicator   undefined       Whether sending the event should block the UI
 *                     (true*)         after a certain delay.
 *                                     * The default value 'undefined' means that the
 *                                       default value ('true') is determined in Session.js.
 *                                       We don't write it explicitly to the event here
 *                                       because that would break many Jasmine tests.
 */
scout.ModelAdapter.prototype._send = function(type, data, options) {
  // Legacy fallback with all options as arguments
  var opts = {};
  if (arguments.length > 2) {
    if (options !== null && typeof options === 'object') {
      opts = options;
    } else {
      opts.delay = arguments[2];
      opts.coalesce = arguments[3];
      opts.showBusyIndicator = arguments[4];
    }
  }
  options = opts;
  // (End legacy fallback)

  var event = new scout.RemoteEvent(this.id, type, data);
  // The following properties will not be sent to the server, see Session._requestToJson().
  if (options.coalesce !== undefined) {
    event.coalesce = options.coalesce;
  }
  if (options.showBusyIndicator !== undefined) {
    event.showBusyIndicator = options.showBusyIndicator;
  }
  this.session.sendEvent(event, options.delay);
};

/**
 * Sends the given value as property event to the server.
 */
scout.ModelAdapter.prototype._sendProperty = function(propertyName, value) {
  var data = {};
  data[propertyName] = value;
  this._send('property', data);
};

scout.ModelAdapter.prototype.addFilterForWidgetEvent = function(filter) {
  this._widgetEventTypeFilter.addFilter(filter);
};

scout.ModelAdapter.prototype.addFilterForWidgetEventType = function(eventType) {
  this._widgetEventTypeFilter.addFilterForEventType(eventType);
};

scout.ModelAdapter.prototype.addFilterForProperties = function(properties) {
  this._propertyChangeEventFilter.addFilterForProperties(properties);
};

scout.ModelAdapter.prototype._isPropertyChangeEventFiltered = function(propertyName, value) {
  if (value instanceof scout.Widget) {
    // In case of a remote widget property use the id, otherwise it would always return false
    value = value.id;
  }
  return this._propertyChangeEventFilter.filter(propertyName, value);
};

scout.ModelAdapter.prototype._isWidgetEventFiltered = function(event) {
  return this._widgetEventTypeFilter.filter(event);
};

scout.ModelAdapter.prototype.resetEventFilters = function() {
  this._propertyChangeEventFilter.reset();
  this._widgetEventTypeFilter.reset();
};

scout.ModelAdapter.prototype._onWidgetPropertyChange = function(event) {
  var propertyName = event.propertyName;
  var value = event.newValue;

  // TODO [7.0] cgu This does not work if value will be converted into another object (e.g scout.DateRange.ensure(selectionRange) in Planner.js)
  // -> either do the check in this._send() or extract ensure into separate method and move the call of addFilterForProperties.
  // The advantage of the first one would be simpler filter functions (e.g. this.widget._nodesToIds(this.widget.selectedNodes) in Tree.js)
  if (this._isPropertyChangeEventFiltered(propertyName, value)) {
    return;
  }

  if (this.isRemoteProperty(propertyName)) {
    if (value && this.widget.isWidgetProperty(propertyName)) {
      value = value.modelAdapter.id;
    }
    this._callSendProperty(propertyName, value);
  }
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
  }
};

scout.ModelAdapter.prototype._syncPropertiesOnPropertyChange = function(newProperties) {
  var orderedPropertyNames = this._orderPropertyNamesOnSync(newProperties);
  orderedPropertyNames.forEach(function(propertyName) {
    var value = newProperties[propertyName];
    var syncFuncName = '_sync' + scout.strings.toUpperCaseFirstLetter(propertyName);
    if (this[syncFuncName]) {
      this[syncFuncName](value);
    } else {
      this.widget.callSetter(propertyName, value);
    }
  }, this);
};

/**
 * May be overridden to return a custom order of how the properties will be set.
 */
scout.ModelAdapter.prototype._orderPropertyNamesOnSync = function(newProperties) {
  return Object.keys(newProperties);
};

/**
 * Called by Session.js for every event from the model
 */
scout.ModelAdapter.prototype.onModelEvent = function(event) {
  if (!event) {
    return;
  }
  if (event.type === 'property') { // Special handling for 'property' type
    this.onModelPropertyChange(event);
  } else {
    this.onModelAction(event);
  }
};

/**
 * Processes the JSON event from the server and calls the corresponding setter of the widget for each property.
 */
scout.ModelAdapter.prototype.onModelPropertyChange = function(event) {
  this.addFilterForProperties(event.properties);
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

/**
 * This method is used to modify adapterData before the data is exported (as used for JSON export).
 */
scout.ModelAdapter.prototype.exportAdapterData = function(adapterData) {
  // use last part of class-name as ID (because that's better than having only a number as ID)
  var modelClass = adapterData.modelClass;
  if (modelClass) {
    var pos = Math.max(0,
      modelClass.lastIndexOf('$') + 1,
      modelClass.lastIndexOf('.') + 1);
    adapterData.id = modelClass.substring(pos);
  }
  delete adapterData.owner;
  delete adapterData.classId;
  delete adapterData.modelClass;
  return adapterData;
};

/**
 * Static method to modify the prototype of scout.Widget.
 */
scout.ModelAdapter.modifyWidgetPrototype = function() {
  if (!scout.app.remote) {
    return;
  }

  // _createChild
  scout.objects.replacePrototypeFunction(scout.Widget, '_createChild', function(model) {
    if (model instanceof scout.Widget) {
      return model;
    }

    // Remote case
    // If the widget has a model adapter use getOrCreateWidget of the session to resolve the child widget
    if (this.modelAdapter) {
      return this.session.getOrCreateWidget(model, this); // model is a String, contains (remote) object ID
    }

    // Local case (default)
    model.parent = this;
    return scout.create(model);
  });
};

scout.addAppListener('bootstrap', scout.ModelAdapter.modifyWidgetPrototype);
