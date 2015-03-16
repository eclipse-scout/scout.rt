// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

/**
 * $entryPoint and jsonSessionId are required to create a new session. The 'options'
 * argument holds all optional values that may be used during initialization (it is
 * the same object passed to the scout.init() function).
 *
 * The following properties are read by this constructor function:
 *   [clientSessionId]
 *     Identifies the 'client instance' on the UI server. If the property is not set
 *     (which is the default case), the clientSessionId is taken from the browser's
 *     session storage (per browser window, survives F5 refresh of page). If no
 *     clientSessionId can be found, a new one is generated.
 *   [userAgent]
 *     Default: DESKTOP
 *   [objectFactories]
 *     Factories to build model adapters. Default: scout.defaultObjectFactories.
 */
scout.Session = function($entryPoint, jsonSessionId, options) {
  options = options || {};

  // Prepare clientSessionId
  var clientSessionId = options.clientSessionId;
  if (!clientSessionId) {
    clientSessionId = sessionStorage.getItem('scout:clientSessionId');
  }
  if (!clientSessionId) {
    clientSessionId = scout.numbers.toBase62(scout.dates.timestamp());
    sessionStorage.setItem('scout:clientSessionId', clientSessionId);
  }

  // Set members
  this.$entryPoint = $entryPoint;
  this.jsonSessionId = jsonSessionId;
  this.parentJsonSession;
  this.clientSessionId = clientSessionId;
  this.userAgent = options.userAgent || new scout.UserAgent(scout.UserAgent.DEVICE_TYPE_DESKTOP);
  this.modelAdapterRegistry = {};
  this.locale;
  this._asyncEvents = [];
  this._asyncRequestQueued;
  this._childWindows = []; // for detached windows
  this._deferred;
  this._startup;
  this._unload;
  this.desktop;
  this.url = 'json';
  this._adapterDataCache = {};
  this.objectFactory = new scout.ObjectFactory(this);
  this._initObjectFactory(options);
  this._texts = new scout.Texts();
  this._customParams;
  this._requestsPendingCounter = 0; // TODO CGU do we really want to have multiple requests pending?
  this._busyCounter = 0; //  >0 = busy
  this.layoutValidator = new scout.LayoutValidator();
  this.detachHelper = new scout.DetachHelper();
  this.partId = jsonSessionId.substring(0, jsonSessionId.indexOf(':'));
  this._asyncChecker;

  // TODO BSH Detach | Check if there is another way
  // If this is a popup window, re-register with parent (in case the user reloaded the popup window)
  // re-name "detach window", since "detach" is also an often used JQuery operation on the DOM
  if (window.opener && window.opener.scout && window.opener.scout.sessions) {
    // Should never happen, as forms are not detachable when multiple sessions are alive (see Form.js)
    if (window.opener.scout.sessions.length > 1) {
      window.close();
      throw new Error('Too many scout sessions');
    }
    var parentJsonSession = window.opener.scout.sessions[0];
    parentJsonSession.registerChildWindow(window);
    this.parentJsonSession = parentJsonSession; // TODO BSH Detach | Get from options instead?
  }

  this.modelAdapterRegistry[jsonSessionId] = this; // FIXME CGU maybe better separate session object from event processing, create ClientSession.js?. If yes, desktop should not have rootadapter as parent, see 406
  this.rootAdapter = new scout.ModelAdapter();
  this.rootAdapter.init({
    id: '1',
    objectType: 'GlobalAdapter'
  }, this);

  this._initCustomParams();
};

/**
 * Extracts custom parameters from URL
 */
scout.Session.prototype._initCustomParams = function() {
  var customParamMap = new scout.URL().parameterMap;
  for (var prop in customParamMap) {
    this._customParams = this._customParams || {};
    this._customParams[prop] = customParamMap[prop];
  }
};

scout.Session.prototype._initObjectFactory = function(options) {
  if (!options.objectFactories) {
    if (this.userAgent.deviceType === scout.UserAgent.DEVICE_TYPE_MOBILE) {
      options.objectFactories = scout.mobileObjectFactories;
    } else {
      options.objectFactories = scout.defaultObjectFactories;
    }
  }

  this.objectFactory.register(options.objectFactories);
};

scout.Session.prototype.unregisterModelAdapter = function(modelAdapter) {
  delete this.modelAdapterRegistry[modelAdapter.id];
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
  $.log.trace('getOrCreate(' + id + (parent ? ', ' + parent : '') + ')');
  if (!id) {
    return;
  }
  if (typeof id !== 'string') {
    throw new Error('typeof id must be string');
  }

  var adapter = this.modelAdapterRegistry[id];
  if (adapter) {
    $.log.trace('model adapter already exists: ' + adapter + ' --> owner = ' + adapter.owner + ', parent = ' + adapter.parent + ', new parent = ' + parent);
    if (!adapter.rendered) {
      // Re-link
      $.log.trace('unlink ' + adapter + ' from ' + adapter.parent + ' and link to new parent ' + parent);
      if (adapter.parent) {
        adapter.parent.removeChild(adapter);
      }
      adapter.parent = parent;
      parent.addChild(adapter);
    } else {
      $.log.trace('adapter ' + adapter + ' is already rendered. keeping link to parent ' + adapter.parent);
    }
    return adapter;
  }

  var adapterData = this._getAdapterData(id);
  if (!adapterData) {
    throw new Error('no adapterData found for id=' + id);
  }

  var owner;
  if (adapterData.owner !== undefined) {
    // Prefer the owner sent by the server
    owner = this.getModelAdapter(adapterData.owner);
    parent = parent || owner; // convenience when 'parent' was not set, e.g. in tests
  } else {
    if (!parent) {
      throw new Error('parent must be defined');
    }
    owner = parent;
  }

  adapter = this.objectFactory.create(adapterData);
  $.log.trace('created new adapter ' + adapter + ': owner = ' + owner + ', parent = ' + parent + ', adapterData.owner = ' + adapterData.owner);
  adapter.owner = owner;
  adapter.parent = parent;
  owner.addOwnedAdapter(adapter);
  parent.addChild(adapter);

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
 * Sends the request asynchronously and processes the response later.<br>
 * Furthermore, the request is sent delayed. If send is called multiple times
 * during the same user interaction, the events are collected and sent in one
 * request at the end of the user interaction
 */
scout.Session.prototype.send = function(target, type, data) {
  this._asyncEvents.push(new scout.Event(target, type, data));
  if (!this._asyncRequestQueued) {
    setTimeout(function() {
      this._sendNow(this._asyncEvents);
      this._asyncRequestQueued = false;
      this._asyncEvents = [];
    }.bind(this), 0);
    this._asyncRequestQueued = true;
  }
};

scout.Session.prototype._sendNow = function(events, deferred) {
  var request = {
    jsonSessionId: this.jsonSessionId,
    events: events
  };

  if (this._startup) {
    this._startup = false;
    // Build startup request (see JavaDoc for JsonStartupRequest.java for details)
    request.startup = true;
    request.clientSessionId = this.clientSessionId;
    if (this.parentJsonSession) {
      request.parentJsonSessionId = this.parentJsonSession.jsonSessionId;
    }
    if (this.userAgent.deviceType !== scout.UserAgent.DEVICE_TYPE_DESKTOP) {
      request.userAgent = this.userAgent;
    }
    request.customParams = this._customParams;
  }

  if (this._unload) {
    request.unload = true;
  }

  this._sendRequest(request);
};

scout.Session.prototype._sendRequest = function(request) {
  if (this.offline) {
    this._queuedRequest.events = this._queuedRequest.events.concat(request.events);
    return;
  }

  if (request.unload && navigator.sendBeacon) {
    // The unload request must _not_ be sent asynchronously, because the browser would cancel
    // it when the page unload is completed. Because the support for synchronous AJAX request
    // will apparently be dropped eventually, we use the "sendBeacon" method to send the unload
    // request to the server (we don't expect an answer). Not all browsers support this method,
    // therefore we check for its existence and fall back to (legacy) synchronous AJAX call
    // when it is missing. More information:
    // - http://stackoverflow.com/questions/15479103/can-beforeunload-unload-be-used-to-send-xmlhttprequests-reliably
    // - https://groups.google.com/a/chromium.org/forum/#!topic/blink-dev/7nKMdg_ALcc
    // - https://developer.mozilla.org/en-US/docs/Web/API/Navigator/sendBeacon
    navigator.sendBeacon(this.url, JSON.stringify(request));
    return;
  }

  if (!this.areRequestsPending() && !request.unload) {
    this.setBusy(true);
  }
  this._requestsPendingCounter++;

  var ajaxOptions = {
    async: !request.unload,
    type: 'POST',
    dataType: 'json',
    contentType: 'application/json; charset=UTF-8',
    cache: false,
    url: this.url,
    data: JSON.stringify(request),
    context: request
  };

  var success = false;
  var jsError;

  function onAjaxDone(data) {
    try {
      if (data.error) {
        this._processErrorJsonResponse(data.error);
      }
      else {
        this._processSuccessResponse(data);
        success = true;
      }
    } catch (err) {
      jsError = jsError || err;
    }
  }

  function onAjaxFail(jqXHR, textStatus, errorThrown) {
    try {
      this._processErrorResponse(request, jqXHR, textStatus, errorThrown);
    } catch (err) {
      jsError = jsError || err;
    }
  }

  function onAjaxAlways(data, textStatus, errorThrown) {
    this._requestsPendingCounter--;
    if (!this.areRequestsPending() && !request.unload) {
      this.setBusy(false);
    }
    this.layoutValidator.validate();
    if (success) {
      this._fireRequestFinished(data);
    }
    // Throw previously catched error
    if (jsError) {
      throw jsError;
    }
  }

  $.ajax(ajaxOptions).done(onAjaxDone.bind(this)).fail(onAjaxFail.bind(this)).always(onAjaxAlways.bind(this));
};

scout.Session.prototype._processSuccessResponse = function(message) {
  this._queuedRequest = null;

  message.adapterData = message.adapterData || {};
  message.events = message.events || {};
  if (!$.isEmptyObject(message.adapterData)) {
    this._copyAdapterData(message.adapterData);
  }
  if (!$.isEmptyObject(message.events)) {
    this.processingEvents = true;
    try {
      this._processEvents(message.events);
    } finally {
      this.processingEvents = false;
    }
  }

  if ($.log.isDebugEnabled()) {
    var cacheSize = scout.objects.countProperties(this._adapterDataCache);
    $.log.debug('size of _adapterDataCache after response has been processed: ' + cacheSize);
    cacheSize = scout.objects.countProperties(this.modelAdapterRegistry);
    $.log.debug('size of modelAdapterRegistry after response has been processed: ' + cacheSize);
  }

  if (message.checkAsync) {
    this._asyncChecker = setTimeout(this._pullAsyncResults.bind(this), 250);
    $.log.debug('message.checkAsync = true. Pull async results in 250 [ms]...');
  }
};

/**
 * Pulls the results of running async jobs. Note: we cannot use the _sendRequest method here
 * since we don't want the busy handling in this case (async jobs should run in the _background_).
 */
scout.Session.prototype._pullAsyncResults = function() {
  $.log.info('Check for async results...');
  var request = {
      pullAsyncResults: true,
      jsonSessionId: this.jsonSessionId
    }, ajaxOptions = {
      async: true,
      type: 'POST',
      dataType: 'json',
      contentType: 'application/json; charset=UTF-8',
      cache: false,
      url: this.url,
      data: JSON.stringify(request),
      context: request
    };

    $.ajax(ajaxOptions)
      .done(function(data) {
        this._processSuccessResponse(data);
      }.bind(this))
      .fail(function(jqXHR, textStatus, errorThrown) {
        this._processErrorResponse(request, jqXHR, textStatus, errorThrown);
      }.bind(this));
};

scout.Session.prototype._copyAdapterData = function(adapterData) {
  var count = 0;
  var prop;

  for (prop in adapterData) {
    this._adapterDataCache[prop] = adapterData[prop];
    count++;
  }
  if (count > 0) {
    $.log.debug('Stored ' + count + ' properties in adapterDataCache');
  }
};

/**
 * @param textStatus 'timeout', 'abort', 'error' or 'parseerror' (see http://api.jquery.com/jquery.ajax/)
 */
scout.Session.prototype._processErrorResponse = function(request, jqXHR, textStatus, errorThrown) {
  $.log.error('errorResponse: status=' + jqXHR.status + ', textStatus=' + textStatus + ', errorThrown=' + errorThrown);

  // Status code = 0 -> no connection
  // Status code >= 12000 come from windows, see http://msdn.microsoft.com/en-us/library/aa383770%28VS.85%29.aspx. Not sure if it is necessary for IE >= 9.
  if (!jqXHR.status || jqXHR.status >= 12000) {
    this.goOffline();
    if (!this._queuedRequest) {
      this._queuedRequest = request;
    }
    return;
  }

  throw new Error('Error while processing request: ' + errorThrown);
};

scout.Session.prototype._processErrorJsonResponse = function(jsonResponse) {
  // Default values for fatal message boxes
  var boxOptions = {
    title: this.text('ServerError'),
    text: jsonResponse.errorMessage,
    yesButtonText: this.text('Reload'),
    yesButtonAction: function() {
      // Hide everything (on entire page, not only $entryPoint)
      $('body').html('');
      // Reload window (using setTimeout, to overcome drawing issues in IE)
      setTimeout(function() {
        window.location.reload();
      });
    }.bind(this)
  };

  // Customize for specific error codes
  if (jsonResponse.errorCode === 5) { // JsonResponse.ERR_STARTUP_FAILED
    // there are no texts yet if session startup failed
    boxOptions.title = '';
    boxOptions.text = jsonResponse.errorMessage;
    boxOptions.yesButtonText = 'Retry';
  } else if (jsonResponse.errorCode === 10) { // JsonResponse.ERR_SESSION_TIMEOUT
    boxOptions.title = this.optText('SessionTimeout', boxOptions.title);
    boxOptions.text = this.optText('SessionExpiredMsg', boxOptions.text);
  } else if (jsonResponse.errorCode === 20) { // JsonResponse.ERR_UI_PROCESSING
    boxOptions.title = this.optText('UiProcessingErrorTitle', boxOptions.title);
    boxOptions.text = this.optText('UiProcessingErrorText', boxOptions.text);
    boxOptions.actionText = this.optText('UiProcessingErrorAction', boxOptions.actionText);
    boxOptions.noButtonText = this.text('Ignore');
  }
  this.showFatalMessage(boxOptions);
};

scout.Session.prototype._fireRequestFinished = function(message) {
  if (!this._deferred) {
    return;
  }

  for (var i = 0; i < message.events.length; i++) {
    this._deferredEventTypes.push(message.events[i].type);
  }

  if (this._requestsPendingCounter === 0) {
    this._deferred.resolve(this._deferredEventTypes);
    this._deferred = null;
    this._deferredEventTypes = null;
  }
};

scout.Session.prototype.showFatalMessage = function(options) {
  options = options || {};
  var model = {
    title: options.title,
    iconId: options.iconId,
    severity: options.severity || 4,
    introText: options.text || options.introText,
    actionText: options.actionText,
    yesButtonText: options.yesButtonText,
    noButtonText: options.noButtonText,
    cancelButtonText: options.cancelButtonText
  };
  var ui = new scout.MessageBox(model);

  model.onButtonClicked = function($button, event) {
    var option = $button.data('option');
    // Close message box
    ui.remove();
    // Custom actions
    if (option === 'yes' && options.yesButtonAction) {
      options.yesButtonAction.apply(this);
    } else if (option === 'no' && options.noButtonAction) {
      options.noButtonAction.apply(this);
    } else if (option === 'cancel' && options.cancelButtonAction) {
      options.cancelButtonAction.apply(this);
    }
  }.bind(this);

  ui.render(this.$entryPoint);
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

scout.Session.prototype.setBusy = function(busy) {
  if (busy) {
    if (this._busyCounter === 0) {
      this._renderBusyGlasspane();
    }
    this._busyCounter++;
  } else {
    this._busyCounter--;
    if (this._busyCounter === 0) {
      this._removeBusyGlasspane();
    }
  }
};

scout.Session.prototype._renderBusyGlasspane = function() {
  // Don't show the busy glasspane immediately. Set a short timer instead (which may be
  // cancelled again if the busy state returns to false in the meantime).
  this._busyGlasspaneTimer = setTimeout(function() {
    // Create busy glasspane
    this._$busyGlasspane = scout.fields.new$Glasspane()
      .addClass('busy')
      .appendTo(this.$entryPoint);
    $('.taskbar-logo').addClass('animated');

    // Workround for Chrome: Trigger cursor change (Otherwise, the cursor is not correctly
    // updated without moving the mouse, see https://code.google.com/p/chromium/issues/detail?id=26723)
    this._$busyGlasspane.css('cursor', 'default');
    setTimeout(function() {
      this._$busyGlasspane.css('cursor', 'wait');
    }.bind(this), 0);
    // (End workaround)

    if (this.desktop) {
      this._darkBusyGlasspaneTimer = setTimeout(function() {
        this._$busyGlasspane.addClass('dark');
      }.bind(this), 2500);
    }
  }.bind(this), 500);
};

scout.Session.prototype._removeBusyGlasspane = function() {
  // Clear any pending timers
  clearTimeout(this._busyGlasspaneTimer);
  clearTimeout(this._darkBusyGlasspaneTimer);

  // If the timer action was executed and the glasspane is showing, we have to remove it
  if (this._$busyGlasspane) {
    // Workround for Chrome: Before removing the glasspane, reset the cursor. Therefore,
    // the actual remove has to be inside setTimeout()
    this._$busyGlasspane.css('cursor', 'default');
    setTimeout(function() {
      // (End workaround)
      this._$busyGlasspane.stop().fadeOut(150, $.removeThis);
      $('.taskbar-logo').removeClass('animated');
    }.bind(this), 0);
  }
};

scout.Session.prototype._processEvents = function(events) {
  var i, event, adapter;
  for (i = 0; i < events.length; i++) {
    event = events[i];

    $.log.debug("Processing event '" + event.type + "' for adapter with ID " + event.target);
    // FIXME BSH/CGU: Check if this should only be getModelAdapter()
    // See commit by CGU 2014-08-15 18:20:43 ("HtmlUi: Fixed 'No adapter' bug")
    adapter = this.getOrCreateModelAdapter(event.target, this.rootAdapter);
    if (!adapter) {
      throw new Error('No adapter registered for ID ' + event.target);
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
      return 'There are windows in DETACHED state.'; // TODO BSH Detach | Text
    }
  }.bind(this));

  // Destroy json session on server when page is closed or reloaded
  $(window).on('unload.' + this.id, this._onWindowUnload.bind(this));
};

scout.Session.prototype.onModelAction = function(event) {
  if (event.type === 'localeChanged') {
    this._onLocaleChanged(event);
  } else if (event.type === 'initialized') {
    this._onInitialized(event);
  } else if (event.type === 'logout') {
    this._onLogout(event);
  }
};

scout.Session.prototype._onLocaleChanged = function(event) {
  this.locale = new scout.Locale(event);
  // FIXME inform components to reformat display text?
};

scout.Session.prototype._onInitialized = function(event) {
  this._texts = new scout.Texts(event.textMap);
  var clientSessionData = this._getAdapterData(event.clientSession);
  this.locale = new scout.Locale(clientSessionData.locale);
  this.desktop = this.getOrCreateModelAdapter(clientSessionData.desktop, this.rootAdapter);
  this.desktop.render(this.$entryPoint);
};

scout.Session.prototype._onLogout = function(event) {
  // Make sure the unload handler does not get triggered since the server initiated the logout and already disposed the session
  $(window).off('unload.' + this.id);

  if (event.redirectUrl) {
    window.location.href = event.redirectUrl;
  } else {
    window.location.reload();
  }
};

scout.Session.prototype._onWindowUnload = function() {
  // Destroy JSON session on server
  this._unload = true;
  this._sendNow();

  // If child windows are open, they have to be closed as well
  this._childWindows.forEach(function(childWindow) {
    childWindow.close();
  });
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

scout.Session.prototype.text = function(textKey) {
  return scout.Texts.prototype.get.apply(this._texts, arguments);
};

scout.Session.prototype.optText = function(textKey, defaultValue) {
  return scout.Texts.prototype.optGet.apply(this._texts, arguments);
};

scout.Session.prototype.textExists = function(textKey) {
  return this._texts.exists(textKey);
};

scout.Session.prototype.getUniqueFieldIdPrefix = function() {
  return 'scout-' + this.partId + '-';
};
