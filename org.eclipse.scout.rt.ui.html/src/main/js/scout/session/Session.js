// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.BackgroundJobPollingStatus = {
  STOPPED: 0,
  RUNNING: 1,
  FAILURE: 2
};

/**
 * $entryPoint and uiSessionId are required to create a new session.
 *
 * The argument partId is optional (default is '0'). It is only necessary when
 * multiple sessions are managed in the same VM (portlet support). Each session's
 * partId must be unique!
 *
 * The 'options' argument holds all optional values that may be used during
 * initialization (it is the same object passed to the scout.init() function).
 * The following 'options' properties are read by this constructor function:
 *   [uiSessionId]
 *     Never null!
 *   [portletPartId]
 *     Default 0
 *   [clientSessionId]
 *     Identifies the 'client instance' on the UI server. If the property is not set
 *     (which is the default case), the clientSessionId is taken from the browser's
 *     session storage (per browser window, survives F5 refresh of page). If no
 *     clientSessionId can be found, a new one is generated.
 *   [userAgent]
 *     Default: DESKTOP
 *   [objectFactories]
 *     Factories to build model adapters. Default: scout.defaultObjectFactories.
 *   [backgroundJobPollingEnabled]
 *     Unless websockets is used, this property turns on (default) or off background
 *     polling using an async ajax call together with setTimeout()
 */
scout.Session = function($entryPoint, options) {
  options = options || {};
  options.portletPartId = options.portletPartId || '0';

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
  this.uiSessionId = options.uiSessionId;
  this.partId = options.portletPartId;
  this.parentUiSession;
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
  this.detachHelper = new scout.DetachHelper(this);
  this._backgroundJobPollingEnabled = (options.backgroundJobPollingEnabled!== false);
  this._backgroundJobPollingStatus = scout.BackgroundJobPollingStatus.STOPPED;

  // TODO BSH Detach | Check if there is another way
  // If this is a popup window, re-register with parent (in case the user reloaded the popup window)
  // re-name "detach window", since "detach" is also an often used JQuery operation on the DOM
  if (window.opener && window.opener.scout && window.opener.scout.sessions) {
    // Should never happen, as forms are not detachable when multiple sessions are alive (see Form.js/_onFormClosed)
    if (window.opener.scout.sessions.length > 1) {
      window.close();
      throw new Error('Too many scout sessions');
    }
    var parentUiSession = window.opener.scout.sessions[0];
    parentUiSession.registerChildWindow(window);
    this.parentUiSession = parentUiSession; // TODO BSH Detach | Get from options instead?
  }

  this.modelAdapterRegistry[options.uiSessionId] = this; // FIXME CGU maybe better separate session object from event processing, create ClientSession.js?. If yes, desktop should not have rootadapter as parent, see 406
  this.rootAdapter = new scout.ModelAdapter();
  this.rootAdapter.init({
    id: '1',
    objectType: 'GlobalAdapter'
  }, this);

  this._initCustomParams();
  scout.focusManager.installManagerForSession(this, options);
};

/**
 * Extracts custom parameters from URL: query string parameters and the url itself with key 'url'
 */
scout.Session.prototype._initCustomParams = function() {
  this._customParams = this._customParams || {};

  var scoutUrl = new scout.URL();
  this._customParams.url = scoutUrl._baseUrlRaw;

  var customParamMap = scoutUrl.parameterMap;
  for (var prop in customParamMap) {
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

/**
 * Creates a new adapter for the given ID or returns an existing instance.
 * When a new adapter is created it will be automatically registered in the
 * model-adpater registry.
 */
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

  // override previously set owner/parent for adapter-data so
  // we can access them in ModelAdapter#init()
  adapterData.owner = owner;
  adapterData.parent = parent;
  adapter =  this.objectFactory.create(adapterData);
  $.log.trace('created new adapter ' + adapter + '. owner=' + owner + ' parent=' + parent);

  owner.addOwnedAdapter(adapter);
  parent.addChild(adapter);
  return adapter;
};

/**
 * Creates a new object instance based on the given model by using the object-factory.
 * This method should be used when you create Widgets or Adapters in the UI without a
 * model from the server-side client.
 *
 * The minimum required properties are:
 * - id
 * - objectType
 */
scout.Session.prototype.createUiObject = function(model) {
  // in case _registered is not set, set it to false
  // but when it is already set (true or false) we don't change it.
  if (model._registered === undefined) {
    model._registered = false;
  }
  if (model.id === undefined) {
    model.id = scout.createUniqueId();
  }
  return this.objectFactory.create(model);
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

scout.Session.prototype.send = function(target, type, data, delay) {
  this.sendEvent(new scout.Event(target, type, data), delay);
};

/**
 * Sends the request asynchronously and processes the response later.<br>
 * Furthermore, the request is sent delayed. If send is called multiple times
 * during the same user interaction, the events are collected and sent in one
 * request at the end of the user interaction
 */
scout.Session.prototype.sendEvent = function(event, delay) {
  delay = delay || 0;

  this._asyncEvents = this._coalesceEvents(this._asyncEvents, event);
  this._asyncEvents.push(event);

  clearTimeout(this._sendTimeoutId);
  this._sendTimeoutId = setTimeout(function() {
    this._sendNow();
  }.bind(this), delay);
};

scout.Session.prototype._sendNow = function() {
  var request = {
    uiSessionId: this.uiSessionId
  };

  if (this._startup) {
    this._startup = false;
    // Build startup request (see JavaDoc for JsonStartupRequest.java for details)
    request.startup = true;
    request.clientSessionId = this.clientSessionId;
    if (this.parentUiSession) {
      request.parentUiSessionId = this.parentUiSession.uiSessionId;
    }
    if (this.userAgent.deviceType !== scout.UserAgent.DEVICE_TYPE_DESKTOP) {
      request.userAgent = this.userAgent;
    }
    request.customParams = this._customParams;
  }

  if (this._unload) {
    request.unload = true;
  }

  if (this._asyncEvents.length > 0) {
    request.events = this._asyncEvents;
  }
  this._sendRequest(request);
  this._asyncEvents = [];
};

scout.Session.prototype._coalesceEvents = function(previousEvents, event) {
  if (!event.coalesce) {
    return previousEvents;
  }
  var filter = $.negate(event.coalesce).bind(event);
  return previousEvents.filter(filter);
};

scout.Session.prototype._sendRequest = function(request) {
  var unload = !! request.unload;

  if (this.offline && !unload) {
    // No need to queue the request when document is unloading
    // Note: Firefox is offline when page is unloaded
    request.events.forEach(function(event) {
      this._queuedRequest.events = this._coalesceEvents(this._queuedRequest.events, event);
    }.bind(this));
    if (this._queuedRequest.events) {
      this._queuedRequest.events = this._queuedRequest.events.concat(request.events);
    }
    else {
      this._queuedRequest.events = request.events;
    }
    return;
  }

  if (unload && navigator.sendBeacon) {
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

  var busyHandling = (!unload && !this.areRequestsPending());
  if (busyHandling) {
    this.setBusy(true);
  }
  this._requestsPendingCounter++;

  $.ajax(this.defaultAjaxOptions(request, !unload))
    .done(onAjaxDone.bind(this))
    .fail(onAjaxFail.bind(this))
    .always(onAjaxAlways.bind(this));

  // --- Helper methods ---

  var jsError,
    success = false;

  function onAjaxDone(data) {
    try {
      if (data.error) {
        this._processErrorJsonResponse(data.error);
      } else {
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
    if (busyHandling) {
      this.setBusy(false);
    }
    this.layoutValidator.validate();
    if (success) {
      this._resumeBackgroundJobPolling();
      this._fireRequestFinished(data);
    }
    // Throw previously catched error
    if (jsError) {
      throw jsError;
    }
  }
};

scout.Session.prototype.defaultAjaxOptions = function(request, async) {
  request = request || {};
  async = scout.objects.whenUndefined(async, true);
  return {
    async: async,
    type: 'POST',
    dataType: 'json',
    contentType: 'application/json; charset=UTF-8',
    cache: false,
    url: this.url,
    data: JSON.stringify(request),
    context: request
  };
};

/**
 * Enable / disable background job polling.
 */
scout.Session.prototype.enableBackgroundJobPolling = function(enabled) {
  this._backgroundJobPollingEnabled = enabled;
};

/**
 * (Re-)starts background job polling when not started yet or when an error occured while polling.
 * In the latter case, polling is resumed when a user-initiated request has been successful.
 */
scout.Session.prototype._resumeBackgroundJobPolling = function() {
  if (this._backgroundJobPollingEnabled && this._backgroundJobPollingStatus !== scout.BackgroundJobPollingStatus.RUNNING) {
    $.log.info('Resume background jobs polling request, status was=' + this._backgroundJobPollingStatus);
    this._pollForBackgroundJobs();
  }
};

/**
 * Polls the results of jobs running in the background. Note: we cannot use the _sendRequest method here
 * since we don't want any busy handling in case of background jobs. The request may take a while, since
 * the server doesn't return until either a time-out occurs or there's something in the response when
 * a model job is done and no request initiated by a user is running.
 */
scout.Session.prototype._pollForBackgroundJobs = function() {
  var request = {
    uiSessionId: this.uiSessionId,
    pollForBackgroundJobs: true
  };

  this._backgroundJobPollingStatus = scout.BackgroundJobPollingStatus.RUNNING;

  var ajaxOptions = this.defaultAjaxOptions(request);
  // Add dummy parameter as marker (for debugging purposes)
  ajaxOptions.url = new scout.URL(ajaxOptions.url).addParameter('poll').toString();

  $.ajax(ajaxOptions)
    .done(onAjaxDone.bind(this))
    .fail(onAjaxFail.bind(this));

  // --- Helper methods ---

  function onAjaxDone(data) {
    if (data.error) {
      // Don't schedule a new polling request, when an error occurs
      // when the next user-initiated request succeeds, we re-enable polling
      // otherwise the polling would ping the server to death in case of an error
      $.log.warn('Polling request failed. Interrupt polling until the next user-initiated request succeeds');
      this._backgroundJobPollingStatus = scout.BackgroundJobPollingStatus.FAILURE;
      this._processErrorJsonResponse(data.error);
    } else if (data.sessionTerminated) {
      $.log.warn('Session terminated, stopped polling for background jobs');
    } else {
      this._processSuccessResponse(data);
      this.layoutValidator.validate();
      setTimeout(this._pollForBackgroundJobs.bind(this));
    }
  }

  function onAjaxFail(jqXHR, textStatus, errorThrown) {
    this._backgroundJobPollingStatus = scout.BackgroundJobPollingStatus.FAILURE;
    this._processErrorResponse(request, jqXHR, textStatus, errorThrown);
  }
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
    if (!this._queuedRequest && !request.pollForBackgroundJob) {
      this._queuedRequest = request;
    }
    return;
  }

  throw new Error('Error while processing request: ' + errorThrown);
};

scout.Session.prototype._processErrorJsonResponse = function(jsonError) {
  // Default values for fatal message boxes
  var boxOptions = {
    title: this.text('ServerError'),
    text: jsonError.message,
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
  if (jsonError.code === 5) { // JsonResponse.ERR_STARTUP_FAILED
    // there are no texts yet if session startup failed
    boxOptions.title = '';
    boxOptions.text = jsonError.message;
    boxOptions.yesButtonText = 'Retry';
  } else if (jsonError.code === 10) { // JsonResponse.ERR_SESSION_TIMEOUT
    boxOptions.title = this.optText('SessionTimeout', boxOptions.title);
    boxOptions.text = this.optText('SessionExpiredMsg', boxOptions.text);
  } else if (jsonError.code === 20) { // JsonResponse.ERR_UI_PROCESSING
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
  var ui = new scout.MessageBox(model, this);
  ui.on('buttonClick', function(event) {
    // Close message box
    ui.remove();
    // Custom actions
    var option = event.option;
    if (option === 'yes' && options.yesButtonAction) {
      options.yesButtonAction.apply(this);
    } else if (option === 'no' && options.noButtonAction) {
      options.noButtonAction.apply(this);
    } else if (option === 'cancel' && options.cancelButtonAction) {
      options.cancelButtonAction.apply(this);
    }
  }.bind(this));

  this._setApplicationLoading(false);
  ui.render(this.$entryPoint);
};

scout.Session.prototype.goOffline = function() {
  $.log.error('goOffline');
  this.offline = true;

  // In Firefox, the current async polling request is interrupted immediately when the page is unloaded. Therefore,
  // an offline message would appear at once on the desktop. When reloading the page, all elements are cleared anyway,
  // thus we wait some short period of time before displaying the message and starting the reconnector. If
  // we find that goOffline() was called because of request unloading, we skip the unnecessary part.
  setTimeout(function() {
    if (this._unload) {
      return;
    }
    if (this.desktop) {
      this.desktop.goOffline();
    }
    if (!this.reconnector) {
      this.reconnector = new scout.Reconnector(this);
    }
    this.reconnector.start();
  }.bind(this), 100);
};

scout.Session.prototype.goOnline = function() {
  this.offline = false;
  this._sendRequest(this._queuedRequest);
  if (this.desktop) {
    this.desktop.goOnline();
  }
};

scout.Session.prototype.onReconnecting = function() {
  if (this.desktop) {
    this.desktop.onReconnecting();
  }
};

scout.Session.prototype.onReconnectingSucceeded = function() {
  if (this.desktop) {
    this.desktop.onReconnectingSucceeded();
  }
  this.goOnline();
};

scout.Session.prototype.onReconnectingFailed = function() {
  if (this.desktop) {
    this.desktop.onReconnectingFailed();
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
  this._busyGlasspaneTimeoutId = setTimeout(function() {
    // Create busy glasspane
    this._$busyGlasspane = scout.fields.new$Glasspane(this.uiSessionId)
      .addClass('busy')
      .appendTo(this.$entryPoint);

    // Workround for Chrome: Trigger cursor change (Otherwise, the cursor is not correctly
    // updated without moving the mouse, see https://code.google.com/p/chromium/issues/detail?id=26723)
    this._$busyGlasspane.css('cursor', 'default');
    setTimeout(function() {
      this._$busyGlasspane.css('cursor', 'wait');
    }.bind(this), 0);
    // (End workaround)

    if (this.desktop) {
      this._$busyGlasspane.installFocusContext('auto', this.uiSessionId);
      this._busyIndicatorTimeoutId = setTimeout(function() {
        var busyIndicator = new scout.BusyIndicator(this);
        busyIndicator.on('buttonClick', function(event) {
          // Set "cancelling" state
          busyIndicator.$label.addClass('cancelled');
          busyIndicator.$buttons.remove();
          busyIndicator.$content.addClass('no-buttons');
          // Request cancel on server
          var request = {
            uiSessionId: this.uiSessionId,
            cancel: true
          };
          this._sendRequest(request);
        }.bind(this));
        busyIndicator.render(this._$busyGlasspane);
      }.bind(this), 2500);
    }
  }.bind(this), 500);
};

scout.Session.prototype._removeBusyGlasspane = function() {
  // Clear any pending timers
  clearTimeout(this._busyGlasspaneTimeoutId);
  clearTimeout(this._busyIndicatorTimeoutId);

  // If the timer action was executed and the glasspane is showing, we have to remove it
  if (this._$busyGlasspane) {
    // Workround for Chrome: Before removing the glasspane, reset the cursor. Therefore,
    // the actual remove has to be inside setTimeout()
    this._$busyGlasspane.css('cursor', 'default');
    setTimeout(function() {
      // (End workaround)
      this._$busyGlasspane.stop().fadeOut(150, $.removeThis);
    }.bind(this), 0);
  }
};

scout.Session.prototype._setApplicationLoading = function(applicationLoading) {
  if (applicationLoading) {
    this._applicationLoadingTimeoutId = setTimeout(function() {
      if (!this.desktop) {
        this.$entryPoint.appendDiv('application-loading').fadeIn();
      }
    }.bind(this), 500);
  } else {
    clearTimeout(this._applicationLoadingTimeoutId);
    this.$entryPoint.children('.application-loading').remove();
  }
};

scout.Session.prototype._processEvents = function(events) {
  var i, event, adapter;
  for (i = 0; i < events.length; i++) {
    event = events[i];

    $.log.debug("Processing event '" + event.type + "' for adapter with ID " + event.target);
    adapter = this.getModelAdapter(event.target);
    if (!adapter) {
      // FIXME BSH/CGU: Check if this should only be getModelAdapter()
      // See commit by CGU 2014-08-15 18:20:43 ("HtmlUi: Fixed 'No adapter' bug")
      // --> This re-links the parent adapter to the root adapter!!!
      adapter = this.getOrCreateModelAdapter(event.target, this.rootAdapter);
    }
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
  // After a short time, display a loading animation (will be removed again in _onInitialized)
  this._setApplicationLoading(true);

  // Send startup request
  this._startup = true;
  this._sendNow();

  // Ask if child windows should be closed as well
  $(window).on('beforeunload', function() {
    if (this._childWindows.length > 0) {
      return 'There are windows in DETACHED state.'; // TODO BSH Detach | Text
    }
  }.bind(this));

  // Destroy UI session on server when page is closed or reloaded
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
  this.locale = new scout.Locale(event.locale);
  this._texts = new scout.Texts(event.textMap);
  // FIXME BSH(?): inform components to reformat display text?
};

scout.Session.prototype._onInitialized = function(event) {
  this.locale = new scout.Locale(event.locale);
  this._texts = new scout.Texts(event.textMap);
  var clientSessionData = this._getAdapterData(event.clientSession);
  this.desktop = this.getOrCreateModelAdapter(clientSessionData.desktop, this.rootAdapter);
  this.desktop.render(this.$entryPoint);
  this._setApplicationLoading(false);

  var d = scout.device;
  $.log.info('Session initialized. Detected user-agent: system=' + d.system + ' device=' + d.device + ' browser=' + d.browser);
};

scout.Session.prototype._onLogout = function(event) {
  // Clear everything and reload the page. Scheduling this ensures, that any
  // other events may still be executed normally.
  setTimeout(function() {
    // Hide everything (on entire page, not only $entryPoint)
    $('body').html('');

    // Make sure the unload handler does not get triggered since the server initiated the logout and already disposed the session
    $(window).off('unload.' + this.id);

    // Reload window (using setTimeout, to overcome drawing issues in IE)
    setTimeout(function() {
      if (event.redirectUrl) {
        window.location.href = event.redirectUrl;
      } else {
        window.location.reload();
      }
    });
  });
};

scout.Session.prototype._onWindowUnload = function() {
  // Destroy UI session on server
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
