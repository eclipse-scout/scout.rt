// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Session = function($entryPoint, jsonSessionId, userAgent) {
  this.modelAdapterRegistry = {};
  this.locale;
  this.$entryPoint = $entryPoint;
  this._asyncEvents = [];
  this._asyncRequestQueued;
  this.jsonSessionId = jsonSessionId;
  this.parentJsonSession; // FIXME BSH Improve this
  this._childWindows = []; // for detached windows
  this._deferred;
  this._startup;
  this._unload;
  this.desktop;
  this.userAgent = userAgent;
  this.url = 'json';
  this._adapterDataCache = {};
  if (!userAgent) {
    this.userAgent = new scout.UserAgent(scout.UserAgent.DEVICE_TYPE_DESKTOP);
  }
  this.objectFactory = new scout.ObjectFactory(this);

  // Determine clientSessionId
  var clientSessionId = sessionStorage.getItem('scout:clientSessionId');
  if (!clientSessionId) {
    clientSessionId = scout.numberToBase62(scout.getTimestamp());
    sessionStorage.setItem('scout:clientSessionId', clientSessionId);
  }
  this._clientSessionId = clientSessionId;

  // FIXME BSH Improve this
  // If this is a popup window, re-register with parent (in case the user reloaded the popup window)
  if (window.opener && window.opener.scout && window.opener.scout.sessions) {
    // Should never happen, as forms are not detachable when multiple sessions are alive (see Form.js)
    if (window.opener.scout.sessions.length > 1) {
      window.close();
      throw new Error('Too many scout sessions');
    }
    var parentJsonSession = window.opener.scout.sessions[0];
    parentJsonSession.registerChildWindow(window);
    this.parentJsonSession = parentJsonSession;
  }

  // FIXME do we really want to have multiple requests pending?
  this._requestsPendingCounter = 0;

  // FIXME maybe better separate session object from event processing, create
  // ClientSession.js?
  this.modelAdapterRegistry[jsonSessionId] = this;
};

scout.Session.prototype.unregisterModelAdapter = function(modelAdapter) {
  this.modelAdapterRegistry[modelAdapter.id] = null;
};

scout.Session.prototype.registerModelAdapter = function(modelAdapter) {
  if (modelAdapter.id === undefined) {
    throw new Error('modelAdapter.id must be defined');
  }
  this.modelAdapterRegistry[modelAdapter.id] = modelAdapter;
};

scout.Session.prototype.getModelAdapter = function(id) {
  return this.modelAdapterRegistry[id];
};

scout.Session.prototype.getOrCreateModelAdapter = function(id, parent) {
  if (!id) {
    return;
  }
  if (typeof id != 'string') {
    throw 'typeof id must be string';
  }
  if (!parent) {
    throw 'parent needs to be set';
  }

  var adapter = this.modelAdapterRegistry[id];
  if (adapter) {
    return adapter;
  }

  var adapterData = this._getAdapterData(id);
  if (!adapterData) {
    throw 'no adapterData found for id=' + id;
  }
  adapter = this.objectFactory.create(adapterData);

  adapter.parent = parent;
  if (scout.ModelAdapter.prototype.isPrototypeOf(parent)) {
    parent.addChild(adapter);
  }

  return adapter;
};


scout.Session.prototype.getOrCreateModelAdapters = function(ids, parent) {
  if (!ids) {
    return [];
  }
  var adapters = [];
  for (var i = 0; i < ids.length; i++) {
    adapters[i] = this.getOrCreateModelAdapter(ids[i], parent);
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
    jsonSessionId: this.jsonSessionId,
    events: events
  };

  if (this._startup) {
    request.clientSessionId = this._clientSessionId;
    request.startup = true;
    this._startup = false;

    if (this.userAgent.deviceType !== scout.UserAgent.DEVICE_TYPE_DESKTOP) {
      request.userAgent = this.userAgent;
    }
  }
  if (this._unload) {
    request.unload = true;
  }
  if (this.parentJsonSession) {
    request.parentJsonSessionId = this.parentJsonSession.jsonSessionId;
  }

  this._sendRequest(request);
};

scout.Session.prototype._sendRequest = function(request) {
  if (this.offline) {
    this._queuedRequest.events = this._queuedRequest.events.concat(request.events);
    return;
  }

  var that = this;
  this._requestsPendingCounter++;
  $.ajax({
    async: true,
    type: 'POST',
    dataType: 'json',
    contentType: 'application/json',
    cache: false,
    url: this.url,
    data: JSON.stringify(request),
    context: request
  })
    .done(function(data) {
      that._processSuccessResponse(data);
    })
    .fail(function(jqXHR, textStatus, errorThrown) {
      var request = this;
      that._processErrorResponse(request, jqXHR, textStatus, errorThrown);
    });
};

scout.Session.prototype._processSuccessResponse = function(message) {
  this._queuedRequest = null;
  this._requestsPendingCounter--;

  this._copyAdapterData(message.adapterData);
  this.processingEvents = true;
  try {
    this._processEvents(message.events);
  } finally {
    this.processingEvents = false;
    var cacheSize = scout.countProperties(this._adapterDataCache);
    if (cacheSize > 0) {
      $.log('size of _adapterDataCache after response has been processed: ' + cacheSize);
    }
  }

  if (this._deferred) {
    for (var i = 0; i < message.events.length; i++) {
      this._deferredEventTypes.push(message.events[i].type);
    }

    if (this._requestsPendingCounter === 0) {
      this._deferred.resolve(this._deferredEventTypes);
      this._deferred = null;
      this._deferredEventTypes = null;
    }
  }
};

scout.Session.prototype._copyAdapterData = function(adapterData) {
  var count = 0;
  for (var prop in adapterData) {
    this._adapterDataCache[prop] = adapterData[prop];
    count++;
  }
  if (count > 0) {
    $.log('Stored ' + count +  ' properties in adapterDataCache');
  }
};

/**
 *
 * @param textStatus timeout, abort, error or parseerror
 */
scout.Session.prototype._processErrorResponse = function(request, jqXHR, textStatus, errorThrown) {
  this._requestsPendingCounter--;

  // FIXME AWE Remove when not needed anymore
  $.log('ERROR: status=' + jqXHR.status + ', textStatus=' + textStatus + ', errorThrown=' + errorThrown);

  //Status code = 0 -> no connection
  //Status code >= 12000 come from windows, see http://msdn.microsoft.com/en-us/library/aa383770%28VS.85%29.aspx. Not sure if it is necessary for IE >= 9.
  if (!jqXHR.status || jqXHR.status >= 12000) {
    this.goOffline();
    if (!this._queuedRequest) {
      this._queuedRequest = request;
    }
    return;
  }

  var jsonResponse = jqXHR.responseJSON;
  if (jsonResponse && jsonResponse.errorMessage) {
    this.$entryPoint.html('');
    if (this.desktop) {
      this.desktop.showMessage(jsonResponse.errorMessage, 'timeout');
    } else {
      this.$entryPoint.text(jsonResponse.errorMessage);
    }
    return;
  }

  if (errorThrown) {
    throw errorThrown;
  }
  throw new Error('Error while processing request. ' + textStatus);
};

scout.Session.prototype.goOffline = function() {
  this.offline = true;
  this.desktop.goOffline();

  if (!this.reconnector) {
    this.reconnector = new scout.Reconnector(this);
  }
  this.reconnector.start();
};

scout.Session.prototype.goOnline = function() {
  this.offline = false;
  this._sendRequest(this._queuedRequest);
  this.desktop.goOnline();
};

scout.Session.prototype.onReconnecting = function() {
  this.desktop.onReconnecting();
};

scout.Session.prototype.onReconnectingSucceeded = function() {
  this.desktop.onReconnectingSucceeded();
  this.goOnline();
};

scout.Session.prototype.onReconnectingFailed = function() {
  this.desktop.onReconnectingFailed();
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
  var session = this;
  // TODO AWE: convert plain JS event object in Event class
  for (var i = 0; i < events.length; i++) {
    var event = events[i];
    $.log("Processing event '" + event.type + "' for adapter with ID " + event.id);
    var adapter = session.getOrCreateModelAdapter(event.id, this);
    if (!adapter) {
      throw 'No adapter registered for ID ' + event.id;
    }

    if (event.type === 'property') { // Special handling for 'property' type
      adapter.onModelPropertyChange(event);
    } else {
      adapter.onModelAction(event);
    }
  }
};

scout.Session.prototype.init = function() {
  this._startup = true;
  this._sendNow();
  // Ask if child windows should be closed as well
  $(window).on('beforeunload', function() {
    if (this._childWindows.length > 0) {
      return 'There are windows in DETACHED state.'; // FIXME BSH Text
    }
  }.bind(this));
  // Destroy json session on server when page is closed or reloaded
  $(window).on('unload', function() {
    // Destroy JSON session on server
    this._unload = true;
    this._sendNow();
    // If child windows are open, they have to be closed as well
    this._childWindows.forEach(function(childWindow) {
      childWindow.close();
    });
  }.bind(this));
};

scout.Session.prototype.onModelAction = function(event) {
  if (event.type === 'localeChanged') {
    this.locale = new scout.Locale(event);
    // FIXME inform components to reformat display text?
  } else if (event.type === 'initialized') {
    // cannot use getOrCreateModelAdapter here since Session doesn't have a parent
    var sessionData = this._getAdapterData(event.clientSession);
    this.locale = new scout.Locale(sessionData.locale);
    var desktopData = this._getAdapterData(sessionData.desktop);
    this.desktop = this.objectFactory.create(desktopData);
    this.desktop.render(this.$entryPoint);
  }
};

/**
 * Returns the adapter-data sent with the JSON response from the adapter-data cache. Note that this operation
 * removes the requested element from the cache, thus you cannot request the same ID twice. Typically once
 * you've requested an element from this cache an adapter for that ID is created and stored in the adapter
 * registry which too exists on this session object.
 */
scout.Session.prototype._getAdapterData = function(id) {
  var adapterData = this._adapterDataCache[id];
  delete this._adapterDataCache[id];
  return adapterData;
};

scout.Session.prototype.registerChildWindow = function(childWindow) {
  if (!childWindow) {
    throw new Error("Missing argument 'childWindow'");
  }

  // Add to list of open child windows
  this._childWindows.push(childWindow);

  // When the child window is closed, remove it again from the list
  $(childWindow).on('unload', function() {
    var i = this._childWindows.indexOf(childWindow);
    if (i > -1) {
      this._childWindows.splice(i, 1);
    }
  }.bind(this));
};
