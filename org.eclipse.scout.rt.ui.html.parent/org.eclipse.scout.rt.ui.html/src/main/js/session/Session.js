// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Session = function($entryPoint, sessionPartId, userAgent) {
  this.modelAdapterRegistry = {};
  this.locale;
  this.$entryPoint = $entryPoint;
  this._asyncEvents = [];
  this._asyncRequestQueued;
  this._sessionPartId = sessionPartId;
  this._deferred;
  this._startup;
  this.userAgent = userAgent;
  if (!userAgent) {
    this.userAgent = new scout.UserAgent(scout.UserAgent.DEVICE_TYPE_DESKTOP);
  }
  this.objectFactory = new scout.ObjectFactory(this);

  // FIXME do we really want to have multiple requests pending?
  this._requestsPendingCounter = 0;

  // FIXME maybe better separate session object from event processing, create
  // ClientSession.js?
  this.modelAdapterRegistry[sessionPartId] = this;
};

scout.Session.prototype.unregisterModelAdapter = function(modelAdapter) {
  this.modelAdapterRegistry[modelAdapter.id] = null;
};

scout.Session.prototype.registerModelAdapter = function(modelAdapter) {
  this.modelAdapterRegistry[modelAdapter.id] = modelAdapter;
};

scout.Session.prototype.getModelAdapter = function(id) {
  return this.modelAdapterRegistry[id];
};

scout.Session.prototype.getOrCreateModelAdapter = function(model, parent) {
  if (!model) {
    return;
  }
  if (!parent) {
    throw "parent needs to be set";
  }

  var adapter = this.modelAdapterRegistry[model.id];
  if (adapter) {
    return adapter;
  }

  adapter = this.objectFactory.create(model);

  adapter.parent = parent;
  if (scout.ModelAdapter.prototype.isPrototypeOf(parent)) {
    parent.addChild(adapter);
  }

  return adapter;
};

scout.Session.prototype.getOrCreateModelAdapters = function(modelArray, parent) {
  if (!modelArray) {
    return [];
  }

  var adapters = [], i;
  for (i = 0; i < modelArray.length; i++) {
    adapters[i] = this.getOrCreateModelAdapter(modelArray[i], parent);
  }

  return adapters;
};

/**
 *
 * Sends the request asynchronously and processes the response later.<br>
 * Furthermore, the request is sent delayed. If send is called multiple times
 * during the same user interaction, the events are collected and sent in one
 * request at the end of the user interaction
 *
 */
scout.Session.prototype.send = function(type, id, data) {
  this._asyncEvents.push(new scout.Event(type, id, data));
  if (!this._asyncRequestQueued) {
    var that = this;
    setTimeout(function() {
      that._sendNow(that._asyncEvents);
      that._asyncRequestQueued = false;
      that._asyncEvents = [];
    }, 0);

    this._asyncRequestQueued = true;
  }
};

scout.Session.prototype._sendNow = function(events, deferred) {
  var request = {
    events: events,
    sessionPartId: this._sessionPartId
  };

  if (this._startup) {
    request.startup = true;
    this._startup = false;

    if (this.userAgent.deviceType !== scout.UserAgent.DEVICE_TYPE_DESKTOP) {
      request.userAgent = this.userAgent;
    }
  }

  var that = this;
  this._requestsPendingCounter++;
  $.ajax({
    async: true,
    type: "POST",
    dataType: "json",
    cache: false,
    url: 'json',
    data: JSON.stringify(request),
    success: function(message) {
      that._processResponse(message);
    }
  });
};

scout.Session.prototype._processResponse = function(message) {
  this._requestsPendingCounter--;

  if (message.errorMessage) {
    this.$entryPoint.html('');
    this.$entryPoint.text(message.errorMessage);
    return;
  }

  this.processingEvents = true;
  try {
    this._processEvents(message.events);
  } finally {
    this.processingEvents = false;
  }

  if (this._deferred) {
    for (var i = 0; i < message.events.length; i++) {
      this._deferredEventTypes.push(message.events[i].type_);
    }

    if (this._requestsPendingCounter === 0) {
      this._deferred.resolve(this._deferredEventTypes);
      this._deferred = null;
      this._deferredEventTypes = null;
    }
  }
};

scout.Session.prototype.listen = function() {
  if (!this._deferred) {
    this._deferred = $.Deferred();
    this._deferredEventTypes = [];
  }
  return this._deferred;
};

scout.Session.prototype.areEventsQueued = function() {
  return this._asyncEvents.length > 0;
};

scout.Session.prototype.areRequestsPending = function() {
  return this._requestsPendingCounter > 0;
};

scout.Session.prototype._processEvents = function(events) {
  // TODO AWE: convert plain JS event object in Event class
  var session = this;
  for (var i = 0; i < events.length; i++) {
    var event = events[i],
      widgetId;

    if (event.type_ == 'create') {
      widgetId = event.parentId;
    } else {
      widgetId = event.id;
    }

    var widget = session.modelAdapterRegistry[widgetId];
    if (!widget) {
      throw "No widget found for id " + widgetId;
    }

    if (event.type_ == 'create') {
      widget.onModelCreate(event);
    } else if (event.type_ == 'property') {
      widget.onModelPropertyChange(event);
    } else {
      widget.onModelAction(event);
    }
  }
};

scout.Session.prototype.init = function() {
  this._startup = true;
  this._sendNow();
};

scout.Session.prototype.onModelCreate = function(event) {
  this.locale = new scout.Locale(event.locale);
  var desktop = this.objectFactory.create(event.desktop);
  desktop.render(this.$entryPoint);
};

scout.Session.prototype.onModelAction = function(event) {
  if (event.type_ == 'localeChanged') {
    this.locale = new scout.Locale(event);
    // FIXME inform components to reformat display text?
  }
};
