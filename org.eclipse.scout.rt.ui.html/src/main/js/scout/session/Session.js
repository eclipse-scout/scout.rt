// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

/**
 * $entryPoint and uiSessionId (in the options object) are required to create a new session.
 *
 * The 'options' argument holds all optional values that may be used during
 * initialization (it is the same object passed to the scout.init() function).
 * The following 'options' properties are read by this constructor function:
 *   [uiSessionId]
 *     Mandatory UI session id, must be unique.
 *   [portletPartId]
 *     Optional, default is 0. Necessary when multiple UI sessions are managed
 *     by the same window (portlet support). Each session's partId must be unique.
 *     (Usually, the partId is also part of the uiSessionId.)
 *   [clientSessionId]
 *     Identifies the 'client instance' on the UI server. If the property is not set
 *     (which is the default case), the clientSessionId is taken from the browser's
 *     session storage (per browser window, survives F5 refresh of page). If no
 *     clientSessionId can be found, a new one is generated on the server.
 *   [userAgent]
 *     Default: DESKTOP
 *   [objectFactories]
 *     Factories to build model adapters. Default: scout.defaultObjectFactories.
 *   [backgroundJobPollingEnabled]
 *     Unless websockets is used, this property turns on (default) or off background
 *     polling using an async ajax call together with setTimeout()
 *   [suppressErrors]
 *     Basically added because of Jasmine-tests. When working with async tests that
 *     use setTimeout(), sometimes the Jasmine-Maven plug-in fails and aborts the
 *     build because there were console errors. These errors always happen in this
 *     class. That's why we can skip suppress error handling with this flag.
 *   [uiOptions]
 *     Optional objects object that influences various parts of the core Scout Html UI.
 *     Currently supported UI options:
 *     o "useTaskbarLogo": Default false. If true, the desktop will add a small logo
 *          to the taskbar. It is styled with the CSS class ".taskbar-logo".
 */
scout.Session = function($entryPoint, options) {
  options = options || {};

  // Prepare clientSessionId
  var clientSessionId = options.clientSessionId;
  if (!clientSessionId) {
    clientSessionId = sessionStorage.getItem('scout:clientSessionId');
  }

  // Set members
  this.$entryPoint = $entryPoint;
  this.uiSessionId = options.uiSessionId;
  this.partId = scout.helpers.nvl(options.portletPartId, 0);
  this.parentUiSession;
  this.clientSessionId = clientSessionId;
  this.userAgent = options.userAgent || new scout.UserAgent(scout.UserAgent.DEVICE_TYPE_DESKTOP);
  this.suppressErrors = scout.helpers.nvl(options.suppressErrors, false);
  this.modelAdapterRegistry = {};
  this._clonedModelAdapterRegistry = {}; // key = adapter-ID, value = array of clones for that adapter
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
  this._initObjectFactory(options.objectFactories);
  this._texts = new scout.Texts();
  this._customParams;
  this._requestsPendingCounter = 0; // TODO CGU do we really want to have multiple requests pending?
  this._busyCounter = 0; // >0 = busy
  this.layoutValidator = new scout.LayoutValidator();
  this.detachHelper = new scout.DetachHelper(this);
  this._backgroundJobPollingSupport = new scout.BackgroundJobPollingSupport(scout.helpers.nvl(options.backgroundJobPollingEnabled, true));
  this._fatalMessagesOnScreen = {};
  this._loggedOut = false;
  this.uiOptions = options.uiOptions || {};

  this.modelAdapterRegistry[this.uiSessionId] = this; // FIXME CGU maybe better separate session object from event processing, create ClientSession.js?. If yes, desktop should not have rootadapter as parent, see 406
  this.rootAdapter = new scout.ModelAdapter();
  this.rootAdapter.init({
    id: '1',
    objectType: 'GlobalAdapter'
  }, this);

  this._initCustomParams();
  this._registerWithParentUiSession();

  // Install focus management for this session.
  this.focusManager = new scout.FocusManager(this, options);
  this.keyStrokeManager = new scout.KeyStrokeManager(this);
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

/**
 * If this is a popup window, re-registers the session with the parent session. This
 * can be the case if the user reloaded the popup window.
 */
// TODO BSH Detach | Check if there is another way
scout.Session.prototype._registerWithParentUiSession = function() {
  var openerScout;
  try {
    openerScout = window.opener && window.opener.scout;
  } catch (err) {
    // Catch security exceptions of the following type:
    //   "DOMException: Blocked a frame with origin <url> from accessing a cross-origin frame."
    //
    // This should never happen, but apparently it does sometimes. To prevent the UI session
    // from not being started, we catch catch the exception and silently ignore it.
    return;
  }

  if (openerScout && openerScout.sessions) {
    // Should never happen, as forms are not detachable when multiple sessions are alive (see Form.js/_onFormClosed)
    if (openerScout.sessions.length > 1) {
      window.close();
      throw new Error('Too many scout sessions');
    }
    var parentUiSession = openerScout.sessions[0];
    parentUiSession.registerChildWindow(window);
    this.parentUiSession = parentUiSession; // TODO BSH Detach | Get from options instead?
  }
};

scout.Session.prototype._throwError = function(message) {
  if (!this.suppressErrors) {
    throw new Error(message);
  }
};

scout.Session.prototype._initObjectFactory = function(objectFactories) {
  if (!objectFactories) {
    if (this.userAgent.deviceType === scout.UserAgent.DEVICE_TYPE_MOBILE) {
      objectFactories = scout.mobileObjectFactories;
    } else {
      objectFactories = scout.defaultObjectFactories;
    }
  }
  this.objectFactory.register(objectFactories);
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
  adapter = this.objectFactory.create(adapterData);
  $.log.trace('created new adapter ' + adapter + '. owner=' + owner + ' parent=' + parent);

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
    if (this.clientSessionId) {
      request.clientSessionId = this.clientSessionId;
    }
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
  if (!request) {
    return; // nothing to send
  }

  if (this.offline && !request.unload) {
    // No need to queue the request when document is unloading
    // Note: Firefox is offline when page is unloaded

    // Merge request with queued event
    if (this._queuedRequest) {
      if (this._queuedRequest.events) {
        // 1 .Remove request events from queued events
        request.events.forEach(function(event) {
          this._queuedRequest.events = this._coalesceEvents(this._queuedRequest.events, event);
        }.bind(this));
        // 2. Add request events to end of queued events
        this._queuedRequest.events = this._queuedRequest.events.concat(request.events);
      } else {
        this._queuedRequest.events = request.events;
      }
    } else {
      this._queuedRequest = request;
    }
    this.layoutValidator.validate();
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

  var ajaxOptions = this.defaultAjaxOptions(request, !request.unload);
  var busyHandling = (!request.unload && !this.areRequestsPending());
  this._performUserAjaxRequest(ajaxOptions, busyHandling, request);
};

scout.Session.prototype.defaultAjaxOptions = function(request, async) {
  request = request || {};
  async = scout.helpers.nvl(async, true);
  return {
    async: async,
    type: 'POST',
    dataType: 'json',
    contentType: 'application/json; charset=UTF-8',
    cache: false,
    url: this.url,
    data: JSON.stringify(request)
  };
};

scout.Session.prototype._performUserAjaxRequest = function(ajaxOptions, busyHandling) {
  if (busyHandling) {
    this.setBusy(true);
  }
  this._requestsPendingCounter++;

  var jsError = null,
    success = false;

  $.ajax(ajaxOptions)
    .done(onAjaxDone.bind(this))
    .fail(onAjaxFail.bind(this))
    .always(onAjaxAlways.bind(this));

  // ----- Helper methods -----

  function onAjaxDone(data) {
    try {
      // Note: remove busy handling _before_ processing the response, otherwise the focus cannot be set
      // correctly, because the glasspane of the busy indicator is still visible.
      if (busyHandling) {
        this.setBusy(false);
      }
      success = this.processJsonResponse(data);
    } catch (err) {
      jsError = jsError || err;
    }
  }

  function onAjaxFail(jqXHR, textStatus, errorThrown) {
    try {
      if (busyHandling) {
        this.setBusy(false);
      }
      this._processErrorResponse(jqXHR, textStatus, errorThrown);
    } catch (err) {
      jsError = jsError || err;
    }
  }

  function onAjaxAlways(data, textStatus, errorThrown) {
    this._requestsPendingCounter--;
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

/**
 * Enable / disable background job polling.
 */
scout.Session.prototype.enableBackgroundJobPolling = function(enabled) {
  this._backgroundJobPollingSupport.enabled(enabled);
};

/**
 * (Re-)starts background job polling when not started yet or when an error occurred while polling.
 * In the latter case, polling is resumed when a user-initiated request has been successful.
 */
scout.Session.prototype._resumeBackgroundJobPolling = function() {
  if (this._backgroundJobPollingSupport.enabled() && this._backgroundJobPollingSupport.status() !== scout.BackgroundJobPollingStatus.RUNNING) {
    $.log.info('Resume background jobs polling request, status was=' + this._backgroundJobPollingSupport.status());
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

  this._backgroundJobPollingSupport.setRunning();

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
      this._backgroundJobPollingSupport.setFailed();
      this._processErrorJsonResponse(data.error);
    } else if (data.sessionTerminated) {
      $.log.warn('Session terminated, stopped polling for background jobs');
      // If were are not yet logged out, redirect to the logout URL (the session that initiated the
      // session invalidation will receive a dedicated logout event, redirect is handled there).
      if (!this._loggedOut && data.redirectUrl) {
        scout.reloadPage(data.redirectUrl, true);
      }
    } else {
      this._processSuccessResponse(data);
      this.layoutValidator.validate();
      setTimeout(this._pollForBackgroundJobs.bind(this));
    }
  }

  function onAjaxFail(jqXHR, textStatus, errorThrown) {
    this._backgroundJobPollingSupport.setFailed();
    this._processErrorResponse(jqXHR, textStatus, errorThrown, request);
  }
};

scout.Session.prototype.processJsonResponse = function(data) {
  var success = false;
  if (data.error) {
    this._processErrorJsonResponse(data.error);
  } else {
    this._processSuccessResponse(data);
    success = true;
  }
  return success;
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
scout.Session.prototype._processErrorResponse = function(jqXHR, textStatus, errorThrown, request) {
  $.log.error('errorResponse: status=' + jqXHR.status + ', textStatus=' + textStatus + ', errorThrown=' + errorThrown);

  // Status code = 0 -> no connection
  // Status code >= 12000 come from windows, see http://msdn.microsoft.com/en-us/library/aa383770%28VS.85%29.aspx. Not sure if it is necessary for IE >= 9.
  if (!jqXHR.status || jqXHR.status >= 12000) {
    this.goOffline();
    if (!this._queuedRequest && request && !request.pollForBackgroundJobs) {
      this._queuedRequest = request;
    }
    return;
  }

  // Show error message
  var boxOptions = {
    header: this.optText('ui.NetworkError', 'Network error'),
    body: jqXHR.status + ' ' + errorThrown,
    yesButtonText: this.optText('ui.Reload', 'Reload'),
    yesButtonAction: function() {
      scout.reloadPage();
    },
    noButtonText: this.optText('ui.Ignore', 'Ignore')
  };
  this.showFatalMessage(boxOptions, jqXHR.status + '.net');
};

scout.Session.prototype._processErrorJsonResponse = function(jsonError) {
  // Default values for fatal message boxes
  var boxOptions = {
    header: this.optText('ui.ServerError', 'Server error') + ' (' + this.optText('ui.ErrorCodeX', 'Code ' + jsonError.code, jsonError.code) + ')',
    body: jsonError.message,
    yesButtonText: this.optText('ui.Reload', 'Reload'),
    yesButtonAction: function() {
      scout.reloadPage();
    }
  };

  // Customize for specific error codes
  if (jsonError.code === 5) { // JsonResponse.ERR_STARTUP_FAILED
    // there are no texts yet if session startup failed
    boxOptions.header = jsonError.message;
    boxOptions.body = null;
    boxOptions.yesButtonText = 'Retry';
  } else if (jsonError.code === 10) { // JsonResponse.ERR_SESSION_TIMEOUT
    boxOptions.header = this.optText('ui.SessionTimeout', boxOptions.header);
    boxOptions.body = this.optText('ui.SessionExpiredMsg', boxOptions.body);
  } else if (jsonError.code === 20) { // JsonResponse.ERR_UI_PROCESSING
    boxOptions.header = this.optText('ui.UnexpectedProblem', boxOptions.header);
    boxOptions.body = scout.strings.join('\n\n',
      this.optText('ui.InternalProcessingErrorMsg', boxOptions.body, ' (' + this.optText('ui.ErrorCodeX', 'Code 20', '20') + ')'),
      this.optText('ui.UiInconsistentMsg', ''));
    boxOptions.noButtonText = this.optText('ui.Ignore', 'Ignore');
  }
  this.showFatalMessage(boxOptions, jsonError.code);
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

/**
 * Shows a UI-only message box.
 *
 * @param options
 *          Options for the message box, see scout.MessageBox
 * @param errorCode
 *          If defined, a second call to this method with the same errorCode will
 *          do nothing. Can be used to prevent double messages for the same error.
 */
scout.Session.prototype.showFatalMessage = function(options, errorCode) {
  if (errorCode) {
    if (this._fatalMessagesOnScreen[errorCode]) {
      return;
    }
    this._fatalMessagesOnScreen[errorCode] = true;
  }
  this._setApplicationLoading(false);

  options = options || {};
  var model = {
    objectType: 'MessageBox',
    iconId: options.iconId,
    severity: scout.helpers.nvl(options.severity, scout.MessageBox.SEVERITY.ERROR),
    header: options.header,
    body: options.body,
    hiddenText: options.hiddenText,
    yesButtonText: options.yesButtonText,
    noButtonText: options.noButtonText,
    cancelButtonText: options.cancelButtonText
  };

  var messageBox = scout.localObjects.createObject(this, model);
  messageBox.remoteHandler = function(target, type, event) {
    if ('action' === type) {
      delete this._fatalMessagesOnScreen[errorCode];
      messageBox.remove();
      var option = event.option;
      if (option === 'yes' && options.yesButtonAction) {
        options.yesButtonAction.apply(this);
      } else if (option === 'no' && options.noButtonAction) {
        options.noButtonAction.apply(this);
      } else if (option === 'cancel' && options.cancelButtonAction) {
        options.cancelButtonAction.apply(this);
      }
    }
  }.bind(this);
  messageBox.render(this.$entryPoint);
};

scout.Session.prototype.uploadFiles = function(target, files, uploadProperties, maxTotalSize, allowedTypes) {
  var formData = new FormData(),
    totalSize = 0;

  if (uploadProperties) {
    $.each(uploadProperties, function(key, value) {
      formData.append(key, value);
    });
  }

  $.each(files, function(index, value) {
    if (!allowedTypes || allowedTypes.length === 0 || scout.helpers.isOneOf(value.type, allowedTypes)) {
      totalSize += value.size;
      var filename = value.name;
      if (filename === undefined) {
        filename = scout.mimeTypes.getDefaultFilename(value.type, '-' + scout.dates.timestamp());
      }
      formData.append('files', value, filename);
    }
  }.bind(this));

  if (!maxTotalSize) {
    maxTotalSize = 5 * 1024 * 1024; // 5 MB as default maximum size
  }

  // very large files must not be sent to server otherwise the whole system might crash (for all users).
  if (totalSize > maxTotalSize) {
    var boxOptions = {
      header: this._texts.get('ui.FileSizeLimitTitle'),
      body: this._texts.get('ui.FileSizeLimit', (maxTotalSize / 1024 / 1024)),
      yesButtonText: this.optText('Ok', 'Ok')
    };

    this.showFatalMessage(boxOptions);
    return;
  }

  var uploadAjaxOptions = {
    async: true,
    type: 'POST',
    url: 'upload/' + this.uiSessionId + '/' + target.id,
    cache: false,
    // Don't touch the data (do not convert it to string)
    processData: false,
    // Do not automatically add content type (otherwise, multipart boundary would be missing)
    contentType: false,
    data: formData
  };
  // Special handling for FormData polyfill
  if (formData.polyfill) {
    formData.applyToAjaxOptions(uploadAjaxOptions);
  }

  var busyHandling = !this.areRequestsPending();
  this._performUserAjaxRequest(uploadAjaxOptions, busyHandling);
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
    this.rootAdapter.goOffline();
    if (!this.reconnector) {
      this.reconnector = new scout.Reconnector(this);
    }
    this.reconnector.start();
  }.bind(this), 100);
};

scout.Session.prototype.goOnline = function() {
  this.offline = false;
  if (this._queuedRequest) {
    this._sendRequest(this._queuedRequest); // implies "_resumeBackgroundJobPolling"
  } else {
    this._resumeBackgroundJobPolling();
  }
  this.rootAdapter.goOnline();
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
      this._renderBusy();
    }
    this._busyCounter++;
  } else {
    this._busyCounter--;
    if (this._busyCounter === 0) {
      this._removeBusy();
    }
  }
};

scout.Session.prototype._renderBusy = function() {
  // Don't show the busy indicator immediately. Set a short timer instead (which may be
  // cancelled again if the busy state returns to false in the meantime).
  this._busyIndicatorTimeoutId = setTimeout(function() {
    if (!this.desktop || !this.desktop.rendered) {
      return; // No busy indicator without desktop (e.g. during shutdown)
    }
    this._busyIndicator = new scout.BusyIndicator(this);
    this._busyIndicator.on('clickCancel', this._onCancelProcessing.bind(this));
    this._busyIndicator.render(this.$entryPoint);
  }.bind(this), 500);
};

scout.Session.prototype._removeBusy = function() {
  // Clear pending timer
  clearTimeout(this._busyIndicatorTimeoutId);

  // Remove busy indicator (if it was already created)
  if (this._busyIndicator) {
    this._busyIndicator.remove();
    this._busyIndicator = null;
  }
};

scout.Session.prototype._onCancelProcessing = function(event) {
  var busyIndicator = this._busyIndicator;
  if (!busyIndicator) {
    return; // removed in the mean time
  }
  busyIndicator.off('clickCancel');

  // Set "canceling" state in busy indicator (after 100ms, would not look good otherwise)
  setTimeout(function() {
    if (busyIndicator.rendered) { // not closed yet
      busyIndicator.$label.addClass('cancelled');
      busyIndicator.$buttons.remove();
      busyIndicator.$content.addClass('no-buttons');
    }
  }.bind(this), 100);

  // Send cancel request to the server.
  this._sendRequest({
    uiSessionId: this.uiSessionId,
    cancel: true
  });
};

scout.Session.prototype._setApplicationLoading = function(applicationLoading) {
  if (applicationLoading) {
    this._applicationLoadingTimeoutId = setTimeout(function() {
      if (!this.desktop || !this.desktop.rendered) {
        this.$entryPoint.appendDiv('application-loading').setMouseCursorWait(true).hide().fadeIn();
      }
    }.bind(this), 500);
  } else {
    clearTimeout(this._applicationLoadingTimeoutId);
    this.$entryPoint.children('.application-loading').setMouseCursorWait(false).remove();
  }
};

scout.Session.prototype._processEvents = function(events) {
  var i, j, event, adapter, adapterClones, eventTargets;
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
    eventTargets = [adapter];
    scout.arrays.pushAll(eventTargets, this.getAdapterClones(adapter));
    for (j = 0; j < eventTargets.length; j++) {
      if (event.type === 'property') { // Special handling for 'property' type
        eventTargets[j].onModelPropertyChange(event);
      } else {
        eventTargets[j].onModelAction(event);
      }
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
  } else if (event.type === 'disposeAdapter') {
    this._onDisposeAdapter(event);
  } else {
    $.log.warn('Model action "' + event.type + '" is not supported by UI session');
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

  // Store clientSessionId in sessionStorage (to send the same ID again on page reload)
  this.clientSessionId = event.clientSessionId;
  sessionStorage.setItem('scout:clientSessionId', this.clientSessionId);

  var clientSessionData = this._getAdapterData(event.clientSession);
  this.desktop = this.getOrCreateModelAdapter(clientSessionData.desktop, this.rootAdapter);

  var d = scout.device;
  $.log.info('Session initialized. Detected user-agent: system=' + d.system + ' device=' + d.device + ' browser=' + d.browser);

  // Render desktop after fonts have been preloaded (this fixes initial layouting issues when font icons are not yet ready)
  if (scout.fonts.loadingComplete) {
    this._renderDesktop();
  } else {
    scout.fonts.preloader().then(function() {
      this._renderDesktop();
    }.bind(this));
  }
};

scout.Session.prototype._renderDesktop = function() {
  this.desktop.render(this.$entryPoint);
  this._setApplicationLoading(false);
};

scout.Session.prototype._onLogout = function(event) {
  this._loggedOut = true;
  // Clear everything and reload the page. We wrap that in setTimeout() to allow other events to be executed normally before.
  setTimeout(function() {
    scout.reloadPage(event.redirectUrl, true);
  });
};

scout.Session.prototype._onDisposeAdapter = function(event) {
  // Model adapter was disposed on server -> dispose it on the UI, too
  var adapter = this.getModelAdapter(event.adapter);
  if (adapter) { // adapter may be null if it was never sent to the UI, e.g. a form that was opened and closed in the same request
    adapter.destroy();
  }
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

scout.Session.prototype.registerAdapterClone = function(adapter, clone) {
  clone.cloneOf = adapter.id;
  var entry = this._clonedModelAdapterRegistry[adapter.id];
  if (entry) {
    entry.push(clone);
  } else {
    this._clonedModelAdapterRegistry[adapter.id] = [clone];
  }
};

scout.Session.prototype.getAdapterClones = function(adapter) {
  var entry = this._clonedModelAdapterRegistry[adapter.id];
  return scout.arrays.ensure(entry);
};

scout.Session.prototype.unregisterAllAdapterClones = function(adapter) {
  var entry = this._clonedModelAdapterRegistry[adapter.id];
  if (entry === undefined) {
    throw new Error('No clones registered for the given adapter');
  }
  delete this._clonedModelAdapterRegistry[adapter.id];
};

scout.Session.prototype.unregisterAdapterClone = function(clone) {
  if (clone.cloneOf === undefined) {
    throw new Error('Tried to unregister a clone but the property cloneOf is not set');
  }
  var entry = this._clonedModelAdapterRegistry[clone.cloneOf];
  if (!entry) {
    throw new Error('No clones registered for adapter');
  }
  var i = entry.indexOf(clone);
  if (i === -1) {
    throw new Error('Adapter found, but clone is not registered');
  }
  entry.splice(i, 1);
};
