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
 * $entryPoint is required to create a new session.
 *
 * The 'options' argument holds all optional values that may be used during
 * initialization (it is the same object passed to the scout.init() function).
 * The following 'options' properties are read by this constructor function:
 *   [portletPartId]
 *     Optional, default is 0. Necessary when multiple UI sessions are managed
 *     by the same window (portlet support). Each session's partId must be unique.
 *   [clientSessionId]
 *     Identifies the 'client instance' on the UI server. If the property is not set
 *     (which is the default case), the clientSessionId is taken from the browser's
 *     session storage (per browser window, survives F5 refresh of page). If no
 *     clientSessionId can be found, a new one is generated on the server.
 *   [userAgent]
 *     Default: DESKTOP
 *   [backgroundJobPollingEnabled]
 *     Unless websockets is used, this property turns on (default) or off background
 *     polling using an async ajax call together with setTimeout()
 *   [suppressErrors]
 *     Basically added because of Jasmine-tests. When working with async tests that
 *     use setTimeout(), sometimes the Jasmine-Maven plug-in fails and aborts the
 *     build because there were console errors. These errors always happen in this
 *     class. That's why we can skip suppress error handling with this flag.
 *   [focusManagerActive]
 *     Forces the focus manager to be active or not. If undefined, the value is
 *     auto detected by Device.js.
 */
scout.Session = function($entryPoint, options) {
  options = options || {};

  // Prepare clientSessionId
  var clientSessionId = options.clientSessionId || sessionStorage.getItem('scout:clientSessionId');

  this.scoutUrl = new scout.URL();
  if (this.scoutUrl.getParameter('forceNewClientSession') || options.forceNewClientSession) {
    clientSessionId = null;
    this._forceNewClientSession = true;
  }

  // Set members
  this.$entryPoint = $entryPoint;
  this.uiSessionId; // assigned by server on session init (OWASP recommendation, see https://www.owasp.org/index.php/Cross-Site_Request_Forgery_%28CSRF%29_Prevention_Cheat_Sheet#General_Recommendation:_Synchronizer_Token_Pattern).
  this.partId = scout.nvl(options.portletPartId, 0);
  this.clientSessionId = clientSessionId;
  this.userAgent = options.userAgent || new scout.UserAgent(scout.device.type, scout.device.supportsTouch());
  this.suppressErrors = scout.nvl(options.suppressErrors, false);
  this.modelAdapterRegistry = {};
  this.locale = options.locale;
  if (this.locale) {
    this._texts = scout.texts.get(this.locale.languageTag);
  } else {
    this._texts = new scout.TextMap();
  }
  this.ajaxRequests = [];
  this._asyncEvents = [];
  this.responseQueue = new scout.ResponseQueue(this);
  this._deferred;
  this.requestTimeoutCancel = 5000; // ms
  this.requestTimeoutPoll = 75000; // ms
  this.requestTimeoutPing = 5000; // ms
  this.ready = false; // true after desktop has been completely rendered
  this.unloaded = false; // true after unload event has been received from the window
  this.desktop;
  this.url = 'json';
  this._adapterDataCache = {};
  this._requestsPendingCounter = 0;
  this._busyCounter = 0; // >0 = busy
  this.layoutValidator = new scout.LayoutValidator();
  this.detachHelper = new scout.DetachHelper(this);
  this._backgroundJobPollingSupport = new scout.BackgroundJobPollingSupport(scout.nvl(options.backgroundJobPollingEnabled, true));
  this._fatalMessagesOnScreen = {};
  this._loggedOut = false;

  // FIXME CGU [6.1] flag necessary for modeladapter, remove it
  this.remote = options.remote;

  // FIXME [awe] 6.1 - rename in RootAdapter, should also have a widget, see FIXME in Session#_processEvents
  this.rootAdapter = new scout.ModelAdapter();
  this.rootAdapter.init({
    session: this,
    id: '1',
    objectType: 'GlobalAdapter'
  });
  this.rootAdapter.createWidget({
    session: this,
    id: '1',
    objectType: 'NullWidget'
  }, new scout.NullWidget());

  // Install focus management for this session.
  this.focusManager = new scout.FocusManager({
    session: this,
    active: options.focusManagerActive
  });
  this.keyStrokeManager = new scout.KeyStrokeManager(this);
};

scout.Session.prototype._throwError = function(message) {
  if (!this.suppressErrors) {
    throw new Error(message);
  }
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

scout.Session.prototype.getOrCreateWidget = function(adapterId, parent) {
  if (!adapterId) {
    return null;
  }
  if (typeof adapterId !== 'string') {
    throw new Error('typeof adapterId must be string');
  }
  var adapter = this.getModelAdapter(adapterId);
  if (adapter) {
    var widget = adapter.widget;
    widget.setParent(parent);
    return widget;
  }
  var adapterData = this._getAdapterData(adapterId);
  if (!adapterData) {
    throw new Error('no adapterData found for adapterId=' + adapterId);
  }
  adapter = this.createModelAdapter(adapterData);
  return adapter.createWidget(adapterData, parent);
};

scout.Session.prototype.createModelAdapter = function(adapterData) {
  var objectType = adapterData.objectType;
  var objectTypeParts = objectType.split('.');
  if (objectTypeParts.length === 2) {
    objectType = objectTypeParts[0] + 'Adapter.' + objectTypeParts[1];
    // If no adapter exists for the given variant then create an adapter without variant.
    // Mostly variant is only essential for the widget, not the adapter
    adapterData.variantLenient = true;
  } else {
    objectType = objectType + 'Adapter';
  }
  var adapterModel = {
    id: adapterData.id,
    session: this,
    variantLenient: adapterData.variantLenient
  };
  var adapter = scout.create(objectType, adapterModel);
  $.log.trace('created new adapter ' + adapter);
  return adapter;
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
  // Use the specified delay, except another event is already scheduled. In that case, use the minimal delay.
  // This ensures that an event with a long delay doesn't hold back another event with a short delay.
  this._asyncDelay = Math.min(delay, scout.nvl(this._asyncDelay, delay));

  clearTimeout(this._sendTimeoutId);
  this._sendTimeoutId = setTimeout(function() {
    this._sendTimeoutId = null;
    this._asyncDelay = null;
    if (this.areRequestsPending()) {
      // do not send if there are any requests pending because the order matters -> prevents race conditions
      return;
    }
    this._sendNow();
  }.bind(this), this._asyncDelay);
};

scout.Session.prototype._sendStartupRequest = function() {
  // Build startup request (see JavaDoc for JsonStartupRequest.java for details)
  var request = {
    startup: true
  };
  if (this.partId) {
    request.partId = this.partId;
  }
  if (this.clientSessionId) {
    request.clientSessionId = this.clientSessionId;
  }
  request.userAgent = this.userAgent;
  request.sessionStartupParams = this._createSessionStartupParams();

  // Send request
  var ajaxOptions = this.defaultAjaxOptions(request);

  $.ajax(ajaxOptions)
    .done(onAjaxDone.bind(this))
    .fail(onAjaxFail.bind(this));

  // ----- Helper methods -----

  function onAjaxDone(data) {
    this._processStartupResponse(data);
  }

  function onAjaxFail(jqXHR, textStatus, errorThrown) {
    this._setApplicationLoading(false);
    this._processErrorResponse(jqXHR, textStatus, errorThrown, request);
  }
};

/**
 * Extracts session startup parameters from URL: query string parameters and the URL itself with key 'url'
 */
scout.Session.prototype._createSessionStartupParams = function() {
  var params = {};
  params.url = this.scoutUrl.baseUrlRaw;

  var urlParameterMap = this.scoutUrl.parameterMap;
  for (var prop in urlParameterMap) {
    params[prop] = urlParameterMap[prop];
  }

  return params;
};

scout.Session.prototype._processStartupResponse = function(data) {
  // Handle errors from server
  if (data.error) {
    this._processErrorJsonResponse(data.error);
    return;
  }

  if (!data.startupData) {
    throw new Error('Missing startupData');
  }

  // Store clientSessionId in sessionStorage (to send the same ID again on page reload)
  sessionStorage.setItem('scout:clientSessionId', data.startupData.clientSessionId);

  // Assign server generated uiSessionId. It must be sent along with all further requests.
  this.uiSessionId = data.startupData.uiSessionId;

  // Destroy UI session on server when page is closed or reloaded
  $(window)
    .on('beforeunload.' + this.uiSessionId, this._onWindowBeforeUnload.bind(this))
    .on('unload.' + this.uiSessionId, this._onWindowUnload.bind(this));

  // Special case: Page must be reloaded on startup (e.g. theme changed)
  if (data.startupData.reloadPage) {
    scout.reloadPage();
    return;
  }

  // Register UI session
  this.modelAdapterRegistry[this.uiSessionId] = this; // FIXME cgu: maybe better separate session object from event processing, create ClientSession.js?. If yes, desktop should not have rootadapter as parent, see 406

  // Store adapters to adapter data cache
  if (data.adapterData) {
    this._copyAdapterData(data.adapterData);
  }

  // Create the desktop
  this._putLocaleData(data.startupData.locale, data.startupData.textMap);
  // Extract client session data without creating a model adapter for it. It is (currently) only used to transport the desktop's adapterId.
  var clientSessionData = this._getAdapterData(data.startupData.clientSession);
  this.desktop = this.getOrCreateWidget(clientSessionData.desktop, this.rootAdapter.widget);
  var renderDesktopImpl = function() {
    this._renderDesktop();

    // In case the server sent additional events, process them
    if (data.events) {
      this.processingEvents = true;
      try {
        this._processEvents(data.events);
      } finally {
        this.processingEvents = false;
      }
    }

    // Ensure layout is valid (explicitly layout immediately and don't wait for setTimeout to run to make layouting invisible to the user)
    this.layoutValidator.validate();
    this.focusManager.validateFocus();

    // Start poller
    this._resumeBackgroundJobPolling();

    this.ready = true;
    $.log.info('Session initialized. Detected ' + scout.device);
    if ($.log.isDebugEnabled()) {
      $.log.debug('size of _adapterDataCache after session has been initialized: ' + scout.objects.countOwnProperties(this._adapterDataCache));
      $.log.debug('size of modelAdapterRegistry after session has been initialized: ' + scout.objects.countOwnProperties(this.modelAdapterRegistry));
    }
  }.bind(this);

  this.render(renderDesktopImpl);
};

scout.Session.prototype.render = function(renderFunc) {
  // Render desktop after fonts have been preloaded (this fixes initial layouting issues when font icons are not yet ready)
  if (scout.fonts.loadingComplete) {
    renderFunc();
  } else {
    scout.fonts.preloader().then(renderFunc);
  }
};

scout.Session.prototype._sendUnloadRequest = function() {
  var request = {
    uiSessionId: this.uiSessionId,
    unload: true
  };
  // Send request
  this._sendRequest(request);
};

scout.Session.prototype._sendNow = function() {
  if (this._asyncEvents.length === 0) {
    // Nothing to send -> return
    return;
  }
  var request = {
    uiSessionId: this.uiSessionId,
    events: this._asyncEvents
  };
  this.responseQueue.prepareRequest(request);
  // Send request
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

  if (this.offline) {
    this._handleSendWhenOffline(request);
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
    var msg = new Blob([JSON.stringify(request)], {
      type: 'application/json; charset=UTF-8'
    });
    navigator.sendBeacon(this._decorateUrl(this.url, request), msg);
    return;
  }

  var ajaxOptions = this.defaultAjaxOptions(request);

  var busyHandling = true;
  if (request.unload) {
    ajaxOptions.async = false;
    busyHandling = false;
  }
  this._performUserAjaxRequest(ajaxOptions, busyHandling, request);
};

scout.Session.prototype._handleSendWhenOffline = function(request) {
  // Note: Firefox is offline when page is unloaded

  // No need to queue the request when request does not contain events (e.g. log request, unload request)
  if (!request.events) {
    return;
  }

  // Merge request with queued event
  if (this._queuedRequest) {
    if (this._queuedRequest.events) {
      // 1. Remove request events from queued events
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
};

scout.Session.prototype.defaultAjaxOptions = function(request) {
  request = request || {};
  var url = this._decorateUrl(this.url, request);

  var ajaxOptions = {
    async: true,
    type: 'POST',
    dataType: 'json',
    contentType: 'application/json; charset=UTF-8',
    cache: false,
    url: url,
    data: JSON.stringify(request)
  };

  // Ensure that certain request don't run forever. When a timeout occurs, the session
  // is put into offline mode. Note that normal requests should NOT be limited, because
  // the server processing might take very long (e.g. long running database query).
  if (request.cancel) {
    ajaxOptions.timeout = this.requestTimeoutCancel;
  }
  if (request.ping) {
    ajaxOptions.timeout = this.requestTimeoutPing;
  }
  if (request.pollForBackgroundJobs) {
    ajaxOptions.timeout = this.requestTimeoutPoll;
  }
  return ajaxOptions;
};

scout.Session.prototype._decorateUrl = function(url, request) {
  var urlHint = null;
  // Add dummy URL parameter as marker (for debugging purposes)
  if (request.unload) {
    urlHint = 'unload';
  } else if (request.pollForBackgroundJobs) {
    urlHint = 'poll';
  } else if (request.ping) {
    urlHint = 'ping';
  } else if (request.cancel) {
    urlHint = 'cancel';
  } else if (request.log) {
    urlHint = 'log';
  } else if (request.syncResponseQueue) {
    urlHint = 'sync';
  }
  if (urlHint) {
    url = new scout.URL(url).addParameter(urlHint).toString();
  }
  return url;
};

scout.Session.prototype._performUserAjaxRequest = function(ajaxOptions, busyHandling, request) {
  if (busyHandling) {
    this.setBusy(true);
  }
  this._requestsPendingCounter++;

  var jsError = null,
    success = false;

  var xhr = $.ajax(ajaxOptions)
    .done(onAjaxDone.bind(this))
    .fail(onAjaxFail.bind(this))
    .always(onAjaxAlways.bind(this));
  this.registerAjaxRequest(xhr);

  // ----- Helper methods -----

  function onAjaxDone(data) {
    try {
      // Note: remove busy handling _before_ processing the response, otherwise the focus cannot be set
      // correctly, because the glasspane of the busy indicator is still visible.
      if (busyHandling) {
        this.setBusy(false);
      }
      success = this.responseQueue.process(data);
    } catch (err) {
      jsError = jsError || err;
    }
  }

  function onAjaxFail(jqXHR, textStatus, errorThrown) {
    try {
      if (busyHandling) {
        this.setBusy(false);
      }
      this._processErrorResponse(jqXHR, textStatus, errorThrown, request);
    } catch (err) {
      jsError = jsError || err;
    }
  }

  function onAjaxAlways(data, textStatus, errorThrown) {
    this.unregisterAjaxRequest(xhr);
    this._requestsPendingCounter--;
    this.layoutValidator.validate();

    // "success" is false when either
    // a) an HTTP error occurred or
    // b) a JSON response with the error flag set (UI processing error) was returned
    if (success) {
      this._resumeBackgroundJobPolling();
      this._fireRequestFinished(data);

      // Send events that happened while begin offline
      var queuedRequest = this._queuedRequest;
      if (queuedRequest) {
        this._queuedRequest = null;
        this.responseQueue.prepareRequest(queuedRequest);
        this._sendRequest(queuedRequest);
      }

      // If there already is a another request pending, send it now
      // But only if it should not be sent delayed
      if (!this._sendTimeoutId) {
        this._sendNow();
      }
    }

    // Throw previously caught error
    if (jsError) {
      throw jsError;
    }
  }
};

scout.Session.prototype.registerAjaxRequest = function(xhr) {
  if (xhr) {
    this.ajaxRequests.push(xhr);
  }
};

scout.Session.prototype.unregisterAjaxRequest = function(xhr) {
  if (xhr) {
    scout.arrays.remove(this.ajaxRequests, xhr);
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
  this.responseQueue.prepareRequest(request);

  this._backgroundJobPollingSupport.setRunning();

  var ajaxOptions = this.defaultAjaxOptions(request);

  var xhr = $.ajax(ajaxOptions)
    .done(onAjaxDone.bind(this))
    .fail(onAjaxFail.bind(this))
    .always(onAjaxAlways.bind(this));
  this.registerAjaxRequest(xhr);

  // --- Helper methods ---

  function onAjaxDone(data) {
    if (data.error) {
      // Don't schedule a new polling request, when an error occurs
      // when the next user-initiated request succeeds, we re-enable polling
      // otherwise the polling would ping the server to death in case of an error
      $.log.warn('Polling request failed. Interrupt polling until the next user-initiated request succeeds');
      this._backgroundJobPollingSupport.setFailed();
      if (this.areRequestsPending()) {
        // Add response to queue, handle later by _performUserAjaxRequest()
        this.responseQueue.add(data);
      } else {
        // No user request pending, handle immediately
        this.responseQueue.process(data);
      }
    } else if (data.sessionTerminated) {
      $.log.warn('Session terminated, stopped polling for background jobs');
      // If were are not yet logged out, redirect to the logout URL (the session that initiated the
      // session invalidation will receive a dedicated logout event, redirect is handled there).
      if (!this._loggedOut && data.redirectUrl) {
        this.logout(data.redirectUrl);
      }
    } else {
      if (this.areRequestsPending()) {
        // Add response to queue, handle later by _performUserAjaxRequest()
        this.responseQueue.add(data);
      } else {
        // No user request pending, handle immediately
        this.responseQueue.process(data);
        this.layoutValidator.validate();
      }
      setTimeout(this._pollForBackgroundJobs.bind(this));
    }
  }

  function onAjaxFail(jqXHR, textStatus, errorThrown) {
    this._backgroundJobPollingSupport.setFailed();
    this._processErrorResponse(jqXHR, textStatus, errorThrown, request);
  }

  function onAjaxAlways(data, textStatus, errorThrown) {
    this.unregisterAjaxRequest(xhr);
  }
};

/**
 * Do NOT call this method directly, always use the response queue:
 *
 *   session.responseQueue.process(data);
 *
 * Otherwise, the response queue's expected sequence number will get out of sync.
 */
scout.Session.prototype.processJsonResponseInternal = function(data) {
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
  if (message.adapterData) {
    this._copyAdapterData(message.adapterData);
  }

  if (message.events) {
    this.processingEvents = true;
    try {
      this._processEvents(message.events);
    } finally {
      this.processingEvents = false;
    }
  }

  if ($.log.isDebugEnabled()) {
    var cacheSize = scout.objects.countOwnProperties(this._adapterDataCache);
    $.log.debug('size of _adapterDataCache after response has been processed: ' + cacheSize);
    cacheSize = scout.objects.countOwnProperties(this.modelAdapterRegistry);
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
  if (this.ready && (!jqXHR.status || jqXHR.status >= 12000)) {
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
    noButtonText: (this.ready ? this.optText('ui.Ignore', 'Ignore') : null)
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
  } else if (jsonError.code === 30) { // JsonResponse.ERR_UNSAFE_UPLOAD
    boxOptions.header = this.optText('ui.UnsafeUpload', boxOptions.header);
    boxOptions.body = this.optText('ui.UnsafeUploadMsg', boxOptions.body);
    boxOptions.yesButtonText = this.optText('ui.Ok', 'Ok');
    boxOptions.yesButtonAction = function() {};
  }
  this.showFatalMessage(boxOptions, jsonError.code);
};

scout.Session.prototype._fireRequestFinished = function(message) {
  if (!this._deferred) {
    return;
  }
  if (message.events) {
    for (var i = 0; i < message.events.length; i++) {
      this._deferredEventTypes.push(message.events[i].type);
    }
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
      session: this,
      parent: this.desktop || new scout.NullWidget(),
      iconId: options.iconId,
      severity: scout.nvl(options.severity, scout.MessageBox.SEVERITY.ERROR),
      header: options.header,
      body: options.body,
      hiddenText: options.hiddenText,
      yesButtonText: options.yesButtonText,
      noButtonText: options.noButtonText,
      cancelButtonText: options.cancelButtonText
    },
    messageBox = scout.create('MessageBox', model),
    $entryPoint = options.entryPoint || this.$entryPoint;

  messageBox.on('action', function(event) {
    delete this._fatalMessagesOnScreen[errorCode];
    messageBox.destroy();
    var option = event.option;
    if (option === 'yes' && options.yesButtonAction) {
      options.yesButtonAction.apply(this);
    } else if (option === 'no' && options.noButtonAction) {
      options.noButtonAction.apply(this);
    } else if (option === 'cancel' && options.cancelButtonAction) {
      options.cancelButtonAction.apply(this);
    }
  }.bind(this));
  messageBox.render($entryPoint);
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
    if (!allowedTypes || allowedTypes.length === 0 || scout.isOneOf(value.type, allowedTypes)) {
      totalSize += value.size;
      formData.append('files', value, value.name || '');
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
  if (this.offline) {
    return; // already offline
  }
  this.offline = true;

  // Abort pending ajax requests. Because the error handlers alter the "this.ajaxRequest" array,
  // the loop must operate on a copy of the original array.
  this.ajaxRequests.slice().forEach(function(xhr) {
    xhr.abort();
  });

  // In Firefox, the current async polling request is interrupted immediately when the page is unloaded. Therefore,
  // an offline message would appear at once on the desktop. When reloading the page, all elements are cleared anyway,
  // thus we wait some short period of time before displaying the message and starting the reconnector. If
  // we find that goOffline() was called because of request unloading, we skip the unnecessary part.
  setTimeout(function() {
    if (this.unloaded) {
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

  var request = {
    uiSessionId: this.uiSessionId,
    syncResponseQueue: true
  };
  this._sendRequest(request); // implies "_resumeBackgroundJobPolling", and also sends queued request

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

scout.Session.prototype.areResponsesQueued = function() {
  return this.responseQueue.size() > 0;
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
    // Do not remove busy indicators if there is a scheduled request which will run immediately to prevent busy cursor flickering
    if (this._busyCounter === 0 && (!this.areEventsQueued() || this.offline)) {
      this._removeBusy();
    }
  }
};

scout.Session.prototype._renderBusy = function() {
  if (this._busyIndicatorTimeoutId !== null && this._busyIndicatorTimeoutId !== undefined) {
    // Do not schedule it twice
    return;
  }
  // Don't show the busy indicator immediately. Set a short timer instead (which may be
  // cancelled again if the busy state returns to false in the meantime).
  this._busyIndicatorTimeoutId = setTimeout(function() {
    if (this._busyIndicator) {
      // busy indicator is already showing
      return;
    }
    if (!this.desktop || !this.desktop.rendered) {
      return; // No busy indicator without desktop (e.g. during shutdown)
    }
    this._busyIndicator = scout.create('BusyIndicator', {
      parent: this.desktop
    });
    this._busyIndicator.on('cancel', this._onCancelProcessing.bind(this));
    this._busyIndicator.render(this.$entryPoint);
  }.bind(this), 500);
};

scout.Session.prototype._removeBusy = function() {
  // Clear pending timer
  clearTimeout(this._busyIndicatorTimeoutId);
  this._busyIndicatorTimeoutId = null;

  // Remove busy indicator (if it was already created)
  if (this._busyIndicator) {
    this._busyIndicator.destroy();
    this._busyIndicator = null;
  }
};

scout.Session.prototype._onCancelProcessing = function(event) {
  var busyIndicator = this._busyIndicator;
  if (!busyIndicator) {
    return; // removed in the mean time
  }
  busyIndicator.off('cancel');

  // Set "canceling" state in busy indicator (after 100ms, would not look good otherwise)
  setTimeout(function() {
    if (busyIndicator.rendered) { // not closed yet
      busyIndicator.$label.addClass('cancelled');
      busyIndicator.$buttons.remove();
      busyIndicator.$content.addClass('no-buttons');
    }
  }.bind(this), 100);

  this._sendCancelRequest();
};

scout.Session.prototype._sendCancelRequest = function() {
  this._sendRequest({
    uiSessionId: this.uiSessionId,
    cancel: true
  });
};

/**
 * Sends a request containing the error message for logging purpose.
 * The request is sent immediately (does not await pending requests)
 */
scout.Session.prototype.sendLogRequest = function(message) {
  var request = {
    uiSessionId: this.uiSessionId,
    log: true,
    message: message
  };
  if (this.currentEvent) {
    request.event = {
      target: this.currentEvent.target,
      type: this.currentEvent.type
    };
  }

  // Do not use _sendRequest to make sure a log request has no side effects and will be sent only once
  var ajaxOptions = this.defaultAjaxOptions(request);

  var xhr = $.ajax(ajaxOptions)
    .always(onAjaxAlways.bind(this));
  this.registerAjaxRequest(xhr);

  // ----- Helper methods -----

  function onAjaxAlways(data, textStatus, errorThrown) {
    this.unregisterAjaxRequest(xhr);
  }
};

scout.Session.prototype._setApplicationLoading = function(applicationLoading) {
  if (applicationLoading) {
    this._applicationLoadingTimeoutId = setTimeout(function() {
      if (!this.desktop || !this.desktop.rendered) {
        this.$entryPoint.appendDiv('application-loading').hide().fadeIn();
      }
    }.bind(this), 500);
  } else {
    clearTimeout(this._applicationLoadingTimeoutId);
    this._applicationLoadingTimeoutId = null;
    this.$entryPoint.children('.application-loading').remove();
  }
};

scout.Session.prototype._processEvents = function(events) {
  var i, j, event, adapter, eventTargets;
  for (i = 0; i < events.length; i++) {
    event = events[i];
    this.currentEvent = event;

    $.log.debug("Processing event '" + event.type + "' for adapter with ID " + event.target);
    adapter = this.getModelAdapter(event.target);
    if (!adapter) {
      throw new Error('No adapter registered for ID ' + event.target);
    }
    if (event.type === 'property') { // Special handling for 'property' type
      adapter.onModelPropertyChange(event);
    } else {
      adapter.onModelAction(event);
    }
    adapter.resetEventFilters();
  }
  this.currentEvent = null;
};

scout.Session.prototype.init = function() {
  $.log.info('Session initializing...');

  // After a short time, display a loading animation (will be removed again in _renderDesktop)
  this._setApplicationLoading(true);

  // Send startup request
  this._sendStartupRequest();
};

// FIXME [awe] 6.1 : discuss with C.GU. Session requires same methods as ModelAdapter, but it is NOT a ModelAdapter currently
// guess we need a SessionAdapter.js - I noticed this in a jasmine test where _processEvents is called an the adapter is the Session
// (event.type=disposeAdapter), also see resetEventFilters method
scout.Session.prototype.onModelAction = function(event) {
  if (event.type === 'localeChanged') {
    this._onLocaleChanged(event);
  } else if (event.type === 'logout') {
    this._onLogout(event);
  } else if (event.type === 'disposeAdapter') {
    this._onDisposeAdapter(event);
  } else if (event.type === 'reloadPage') {
    this._onReloadPage(event);
  } else {
    $.log.warn('Model action "' + event.type + '" is not supported by UI session');
  }
};

scout.Session.prototype.resetEventFilters = function() {
  // NOP
};

scout.Session.prototype._onLocaleChanged = function(event) {
  this._putLocaleData(event.locale, event.textMap);
};

scout.Session.prototype._putLocaleData = function(locale, textMap) {
  this.locale = new scout.Locale(locale);
  this._texts = new scout.TextMap(textMap);
  // FIXME bsh: inform components to reformat display text? also check Collator in scout.comparators.TEXT
};

scout.Session.prototype._renderDesktop = function() {
  this.desktop.render(this.$entryPoint);
  this.desktop.invalidateLayoutTree(false);
  this._setApplicationLoading(false);
};

scout.Session.prototype._onLogout = function(event) {
  this.logout(event.redirectUrl);
};

scout.Session.prototype.logout = function(logoutUrl) {
  this._loggedOut = true;
  if (this._forceNewClientSession) {
    this.desktop.$container.window(true).close();
  } else {
    // remember current url to not lose query parameters
    sessionStorage.setItem('scout:loginUrl', window.location.href);
    // Clear everything and reload the page. We wrap that in setTimeout() to allow other events to be executed normally before.
    setTimeout(function() {
      scout.reloadPage({
        redirectUrl: logoutUrl
      });
    }.bind(this));
  }
};

scout.Session.prototype._onDisposeAdapter = function(event) {
  // Model adapter was disposed on server -> dispose it on the UI, too
  var adapter = this.getModelAdapter(event.adapter);
  if (adapter) { // adapter may be null if it was never sent to the UI, e.g. a form that was opened and closed in the same request
    adapter.destroy();
  }
};

scout.Session.prototype._onReloadPage = function(event) {
  // Don't clear the body, because other events might be processed before the reload and
  // it could cause errors when all DOM elements are already removed.
  scout.reloadPage({
    clearBody: false
  });
};

scout.Session.prototype._onWindowBeforeUnload = function() {
  $.log.info('Session before unloading...');
  // TODO BSH Cancel pending requests
};

scout.Session.prototype._onWindowUnload = function() {
  $.log.info('Session unloading...');
  this.unloaded = true;

  // Close popup windows
  if (this.desktop) {
    this.desktop.formController.closePopupWindows();
  }

  // Destroy UI session on server (only when the server did not not initiate the logout,
  // otherwise the UI session would already be dispoed)
  if (!this._loggedOut) {
    this._sendUnloadRequest();
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

scout.Session.prototype.getAdapterData = function(id) {
  return this._adapterDataCache[id];
};

scout.Session.prototype.text = function(textKey) {
  return scout.TextMap.prototype.get.apply(this._texts, arguments);
};

scout.Session.prototype.optText = function(textKey, defaultValue) {
  return scout.TextMap.prototype.optGet.apply(this._texts, arguments);
};

scout.Session.prototype.textExists = function(textKey) {
  return this._texts.exists(textKey);
};
