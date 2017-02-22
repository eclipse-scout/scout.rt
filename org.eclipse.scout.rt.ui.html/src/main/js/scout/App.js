scout.App = function() {
  this.events = this._createEventSupport();

  /// register the listeners which were added to scout before the app is created
  scout.appListeners.forEach(function(listener) {
    this.addListener(listener);
  }, this);
  scout.appListeners = [];

  scout.app = this;
};

/**
 * Main initialization function.<p>
 *
 * Calls this._prepare, this._bootstrap and this._init.<p>
 * At the initial phase the essential objects are initialized, those which are required for the next phases like logging and the object factory.<br>
 * During the bootstrap phase additional scripts may get loaded required for a successful session startup.<br>
 * The actual initialization does not get started before these bootstrap scripts are loaded.
 */
scout.App.prototype.init = function(options) {
  options = options || {};
  return this._prepare(options)
    .then(this._bootstrap.bind(this, options.bootstrap))
    .then(this._init.bind(this, options))
    .then(this._initDone.bind(this, options));
};

/**
 * Initializes the logging framework, polyfills and the object factory.
 * This happens at the prepare phase because all these things should be available from the beginning.
 */
scout.App.prototype._prepare = function(options) {
  return this._prepareLogging().done(function() {
    this._prepareEssentials(options);
    this._prepareDone(options);
  }.bind(this));
};

scout.App.prototype._prepareEssentials = function(options) {
  scout.polyfills.install(window);
  scout.objectFactory.init();
};

scout.App.prototype._prepareDone = function(options) {
  this.trigger('prepare', {
    options: options
  });
  $.log.debug('App prepared');
};

scout.App.prototype._prepareLogging = function(options) {
  return scout.logging.bootstrap();
};

/**
 * Executes the default bootstrap functions and returns an array of promises.<p>
 * The actual session startup begins only when every of these promises are completed.
 * This gives the possibility to dynamically load additional scripts or files which are mandatory for a successful session startup.
 * The individual bootstrap functions may return null or undefined, a single promise or multiple promises as an array.
 */
scout.App.prototype._bootstrap = function(options) {
  options = options || {};

  var promises = [];
  this._doBootstrap(options).forEach(function(value) {
    if (Array.isArray(value)) {
      promises.concat(value);
    } else if (value) {
      promises.push(value);
    }
  });

  return $.promiseAll(promises)
    .done(this._bootstrapDone.bind(this), options);
};

scout.App.prototype._doBootstrap = function(options) {
  return [
    scout.device.bootstrap(),
    scout.fonts.bootstrap(options.fonts),
    scout.models.bootstrap(options.modelsUrl),
    scout.locales.bootstrap(options.localesUrl),
    scout.texts.bootstrap(options.textsUrl),
    scout.codes.bootstrap(options.codesUrl)
  ];
};

scout.App.prototype._bootstrapDone = function(options) {
  this.trigger('bootstrap', {
    options: options
  });
  $.log.debug('App bootstrapped');
};

/**
 * Initializes a session for each html element with class '.scout' and stores them in scout.sessions.
 */
scout.App.prototype._init = function(options) {
  options = options || {};
  if (!this._checkBrowserCompability(options)) {
    return;
  }

  this._initVersion(options);
  this._prepareDOM();
  this._installErrorHandler();
  this._installGlobalMouseDownInterceptor();
  this._ajaxSetup();
  this._installExtensions();
  this._loadSessions(options.session);
};

scout.App.prototype._checkBrowserCompability = function(options) {
  var device = scout.device;
  $.log.info('Detected browser ' + device.browser + ' version ' + device.browserVersion);
  if (!scout.nvl(options.checkBrowserCompatibility, true) || device.isSupportedBrowser()) {
    // No check requested or browser is supported
    return true;
  }

  $('.scout').each(function() {
    var $entryPoint = $(this),
      $box = $entryPoint.appendDiv(),
      newOptions = scout.objects.valueCopy(options);

    newOptions.checkBrowserCompatibility = false;
    $box.load('unsupported-browser.html', function() {
      $box.find('button').on('click', function() {
        $box.remove();
        scout._init(newOptions);
      });
    });
  });
  return false;
};

scout.App.prototype._initVersion = function(options) {
  this.version = scout.nvl(
    this.version,
    options.version,
    $('scout-version').data('value'));
};


scout.App.prototype._prepareDOM = function() {
  scout.prepareDOM(document);
};

scout.App.prototype._installGlobalMouseDownInterceptor = function() {
  scout.installGlobalMouseDownInterceptor(document);
};

/**
 * Installs a global error handler.
 * <p>
 * Note: we do not install an error handler on popup-windows because everything is controlled by the main-window
 * so exceptions will also occur in that window. This also means, the fatal message-box will be displayed in the
 * main-window, even when a popup-window is opened and active.
 * <p>
 * Caution: The error.stack doesn't look the same in different browsers. Chrome for instance puts the error message
 * on the first line of the stack. Firefox does only contain the stack lines, without the message, but in return
 * the stack trace is much longer :)
 */
scout.App.prototype._installErrorHandler = function() {
  var handler = this._createErrorHandler();
  window.onerror = handler.handle.bind(handler);
};

scout.App.prototype._createErrorHandler = function() {
  return scout.create('ErrorHandler');
};

/**
 * Uses the object returned by {@link #ajaxDefaults} to setup ajax. The values in that object are used as default values for every ajax call.
 */
scout.App.prototype._ajaxSetup = function() {
  var ajaxDefaults = this._ajaxDefaults();
  if (ajaxDefaults) {
    $.ajaxSetup(ajaxDefaults);
  }
};

/**
 * Returns the defaults for every ajax call. You may override it to set custom defaults.
 * By default _beforeAjaxCall is assigned to the beforeSend method.
 * <p>
 * Note: This will affect every ajax call, so use it with care! See also the advice on https://api.jquery.com/jquery.ajaxsetup/.
 */
scout.App.prototype._ajaxDefaults = function() {
  return {
    beforeSend: this._beforeAjaxCall.bind(this)
  };
};

/**
 * Called before every ajax call. Sets the header X-Scout-Correlation-Id.
 * <p>
 * Maybe overridden to set custom headers or to execute other code which should run before an ajax call.
 */
scout.App.prototype._beforeAjaxCall = function(request) {
  request.setRequestHeader('X-Scout-Correlation-Id', scout.numbers.correlationId());
};

scout.App.prototype._loadSessions = function(options) {
  options = options || {};
  $('.scout').each(function(i, elem) {
    var $entryPoint = $(elem);
    options.portletPartId = options.portletPartId || $entryPoint.data('partid') || '0';
    var session = this._loadSession($entryPoint, options);
    scout.sessions.push(session);
  }.bind(this));
};

scout.App.prototype._loadSession = function($entryPoint, options) {
  options.locale = options.locale || this._loadLocale();
  options.$entryPoint = $entryPoint;
  var session = this._createSession(options);

  // TODO [7.0] cgu improve this, start must not be executed because it currently does a server request
  var parent = new scout.NullWidget();
  parent.session = session;
  this._createDesktop(parent);
  this.trigger('desktopcreated');
  session.render(function() {
    session._renderDesktop();

    // Ensure layout is valid (explicitly layout immediately and don't wait for setTimeout to run to make layouting invisible to the user)
    session.layoutValidator.validate();
    session.focusManager.validateFocus();

    session.ready = true;
    this.trigger('sessionready', {session: session});
    $.log.info('Session initialized. Detected ' + scout.device);
  }.bind(this));
  return session;
};

scout.App.prototype._createSession = function(options) {
  return scout.create('Session', options, {
    ensureUniqueId: false
  });
};

scout.App.prototype._createDesktop = function(parent) {
  return scout.create('Desktop', {
    parent: parent
  });
};

/**
 * @returns the locale to be used when no locale is provided as app option. By default the navigators locale is used.
 */
scout.App.prototype._loadLocale = function() {
  return scout.locales.getNavigatorLocale();
};

scout.App.prototype._initDone = function(options) {
  this.trigger('init', {
    options: options
  });
  $.log.info('App initialized');
};

/**
 * Override this method to install extensions to Scout objects. Since the extension feature replaces functions
 * on the prototype of the Scout objects you must apply 'function patches' to Scout framework or other code before
 * the extensions are installed.
 *
 * The default implementation does nothing.
 */
scout.App.prototype._installExtensions = function() {
  // NOP
};

//--- Event handling methods ---
scout.App.prototype._createEventSupport = function() {
  return new scout.EventSupport();
};

scout.App.prototype.trigger = function(type, event) {
  event = event || {};
  event.source = this;
  this.events.trigger(type, event);
};

scout.App.prototype.one = function(type, func) {
  this.events.one(type, func);
};

scout.App.prototype.on = function(type, func) {
  return this.events.on(type, func);
};

scout.App.prototype.off = function(type, func) {
  this.events.off(type, func);
};

scout.App.prototype.addListener = function(listener) {
  this.events.addListener(listener);
};

scout.App.prototype.removeListener = function(listener) {
  this.events.removeListener(listener);
};
