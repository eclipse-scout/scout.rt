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
 *   [uiUseTaskbarLogo]
 *     If true, the desktop will add a small logo to the taskbar. It is styled with
 *     the CSS class ".taskbar-logo". Defaults to false.
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
  this.partId = scout.nvl(options.portletPartId, 0);
  this.clientSessionId = clientSessionId;
  this.userAgent = options.userAgent || new scout.UserAgent(scout.device.type);
  this.suppressErrors = scout.nvl(options.suppressErrors, false);
  this.modelAdapterRegistry = {};
  this._clonedModelAdapterRegistry = {}; // key = adapter-ID, value = array of clones for that adapter
  this.locale;
  this._asyncEvents = [];
  this._asyncResponses = [];
  this._deferred;
  this.ready = false; // true after desktop has been completely rendered
  this.unloaded = false; // true after unload event has been received from the window
  this.desktop;
  this.url = 'json';
  this._adapterDataCache = {};
  this.objectFactory = new scout.ObjectFactory(this);
  this._initObjectFactory(options.objectFactories);
  this._texts = new scout.Texts();
  this._sessionStartupParams;
  this._requestsPendingCounter = 0;
  this._busyCounter = 0; // >0 = busy
  this.layoutValidator = new scout.LayoutValidator();
  this.detachHelper = new scout.DetachHelper(this);
  this._backgroundJobPollingSupport = new scout.BackgroundJobPollingSupport(scout.nvl(options.backgroundJobPollingEnabled, true));
  this._fatalMessagesOnScreen = {};
  this._loggedOut = false;
  this.uiUseTaskbarLogo = options.uiUseTaskbarLogo;

  this.modelAdapterRegistry[this.uiSessionId] = this; // FIXME CGU maybe better separate session object from event processing, create ClientSession.js?. If yes, desktop should not have rootadapter as parent, see 406
  this.rootAdapter = new scout.ModelAdapter();
  this.rootAdapter.init({
    parent: new scout.NullWidget(),
    session: this,
    id: '1',
    objectType: 'GlobalAdapter'
  });

  // Initializes session startup parameters with information from the URL.
  this._initSessionStartupParams();

  // Install focus management for this session.
  this.focusManager = new scout.FocusManager(this, options);
  this.keyStrokeManager = new scout.KeyStrokeManager(this);
};

/**
 * Extracts session startup parameters from URL: query string parameters and the URL itself with key 'url'
 */
scout.Session.prototype._initSessionStartupParams = function() {
  this._sessionStartupParams = this._sessionStartupParams || {};

  var scoutUrl = new scout.URL();
  this._sessionStartupParams.url = scoutUrl._baseUrlRaw;

  var urlParameterMap = scoutUrl.parameterMap;
  for (var prop in urlParameterMap) {
    this._sessionStartupParams[prop] = urlParameterMap[prop];
  }
};

scout.Session.prototype._throwError = function(message) {
  if (!this.suppressErrors) {
    throw new Error(message);
  }
};

scout.Session.prototype._initObjectFactory = function(objectFactories) {
  if (!objectFactories) {
    if (this.userAgent.deviceType === scout.Device.Type.MOBILE) {
      objectFactories = scout.mobileObjectFactories;
    } else {
      objectFactories = scout.defaultObjectFactories;
    }
  }
  this.objectFactory.register(objectFactories);
};

scout.Session.prototype.unregisterModelAdapter = function(modelAdapter) {
  delete this.modelAdapterRegistry[modelAdapter.id];
  if (this.hasClones(modelAdapter)) {
    this.unregisterAllAdapterClones(modelAdapter);
  }
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
      adapter.setParent(parent);
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
scout.Session.prototype.sendEvent = function(event, delay) {
  delay = delay || 0;

  this._asyncEvents = this._coalesceEvents(this._asyncEvents, event);
  this._asyncEvents.push(event);

  clearTimeout(this._sendTimeoutId);
  this._sendTimeoutId = setTimeout(function() {
    this._sendTimeoutId = null;
    if (this.areRequestsPending()) {
      // do not send if there are any requests pending because the order matters -> prevents race conditions
      return;
    }
    this._sendNow();
  }.bind(this), delay);
};

scout.Session.prototype._sendStartupRequest = function() {
  // Build startup request (see JavaDoc for JsonStartupRequest.java for details)
  var request = {
    uiSessionId: this.uiSessionId,
    startup: true
  };
  if (this.clientSessionId) {
    request.clientSessionId = this.clientSessionId;
  }
  if (this.userAgent.deviceType !== scout.Device.Type.DESKTOP) {
    request.userAgent = this.userAgent;
  }
  request.sessionStartupParams = this._sessionStartupParams;
  // Send request
  this._sendRequest(request);
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
  var busyHandling = !request.unload;
  this._performUserAjaxRequest(ajaxOptions, busyHandling, request);
};

scout.Session.prototype.defaultAjaxOptions = function(request, async) {
  request = request || {};
  async = scout.nvl(async, true);
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

scout.Session.prototype._performUserAjaxRequest = function(ajaxOptions, busyHandling, request) {
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
      // If responses are queued (by ?poll requests), handle them now
      for (var i = 0; i < this._asyncResponses.length; i++) {
        this.processJsonResponse(this._asyncResponses[i]);
      }
      this._asyncResponses = [];
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
    this._requestsPendingCounter--;
    this.layoutValidator.validate();
    if (success) {
      this._resumeBackgroundJobPolling();
      this._fireRequestFinished(data);
    }

    // If there already is a another request pending, send it now
    // But only if it should not be sent delayed
    if (!this._sendTimeoutId) {
      this._sendNow();
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
      if (this.areRequestsPending()) {
        // Add response to queue, handle later by _performUserAjaxRequest()
        this._asyncResponses.push(data);
      } else {
        // No user request pending, handle immediately
        this._processErrorJsonResponse(data.error);
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
        this._asyncResponses.push(data);
      } else {
        // No user request pending, handle immediately
        this._processSuccessResponse(data);
        this.layoutValidator.validate();
      }
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

  messageBox.remoteHandler = function(event) {
    if ('action' === event.type) {
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
      var filename = value.name;
      if (!filename) {
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
  this.offline = true;

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

scout.Session.prototype.areResponsesQueued = function() {
  return this._asyncResponses.length > 0;
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
    if (this._busyCounter === 0 && !this.areEventsQueued()) {
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
    this._busyIndicator = scout.create(scout.BusyIndicator, {
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
    this._busyIndicator.remove();
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

  // Send cancel request to the server.
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

  // Do not use _sendRequest to make sure a log request has no side effects and will be sent only once
  var ajaxOptions = this.defaultAjaxOptions(request);
  $.ajax(ajaxOptions);
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
    this.$entryPoint.children('.application-loading').remove();
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
  $.log.info('Session initializing...');

  // After a short time, display a loading animation (will be removed again in _onInitialized)
  this._setApplicationLoading(true);

  // Send startup request
  this._sendStartupRequest();

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
  } else if (event.type === 'reloadPage') {
    this._onReloadPage(event);
  } else {
    $.log.warn('Model action "' + event.type + '" is not supported by UI session');
  }
};

scout.Session.prototype._onReloadPage = function(event) {
  // Don't clear the body, because other events might be processed before the reload and
  // it could cause errors when all DOM elements are already removed.
  scout.reloadPage({
    clearBody: false
  });
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

  // Render desktop after fonts have been preloaded (this fixes initial layouting issues when font icons are not yet ready)
  var renderDesktopImpl = function() {
    this._renderDesktop();
    this.ready = true;
    $.log.info('Session initialized. Detected ' + scout.device);
  }.bind(this);
  if (scout.fonts.loadingComplete) {
    renderDesktopImpl();
  } else {
    scout.fonts.preloader().then(renderDesktopImpl());
  }
};

scout.Session.prototype._renderDesktop = function() {
  this.desktop.render(this.$entryPoint);
  this._setApplicationLoading(false);
};

scout.Session.prototype._onLogout = function(event) {
  this.logout(event.redirectUrl);
};

scout.Session.prototype.logout = function(logoutUrl) {
  this._loggedOut = true;
  // remember current url to not loose query parameters
  sessionStorage.setItem('scout:loginUrl', window.location.href);
  // Clear everything and reload the page. We wrap that in setTimeout() to allow other events to be executed normally before.
  setTimeout(function() {
    scout.reloadPage({
      redirectUrl: logoutUrl,
      suppressUnload: true
    });
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
  $.log.info('Session unloading...');
  this.unloaded = true;

  // Close popup windows
  if (this.desktop) {
    this.desktop.formController.closePopupWindows();
  }

  // Destroy UI session on server
  this._sendUnloadRequest();
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
  return scout.Texts.prototype.get.apply(this._texts, arguments);
};

scout.Session.prototype.optText = function(textKey, defaultValue) {
  return scout.Texts.prototype.optGet.apply(this._texts, arguments);
};

scout.Session.prototype.textExists = function(textKey) {
  return this._texts.exists(textKey);
};

scout.Session.prototype.registerAdapterClone = function(adapter, clone) {
  clone.cloneOf = adapter;
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

scout.Session.prototype.hasClones = function(adapter) {
  return this.getAdapterClones(adapter).length > 0;
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
