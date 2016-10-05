scout.App = function() { //
};

/**
 * Main initialization function.<p>
 *
 * Calls this._bootstrap and this._init.<p>
 * During the bootstrap phase additional scripts may get loaded required for a successful session startup.
 * The actual initialization does not get started before these bootstrap scripts are loaded.
 */
scout.App.prototype.init = function(options) {
  var deferredInit = $.Deferred();
  var deferreds = this._bootstrap(options.bootstrap);
  $.when.apply($, deferreds)
    .done(function() {
      this._init();
      deferredInit.resolve();
    }.bind(this));
  return deferredInit;
};

/**
 * Executes the default bootstrap functions and returns an array of deferred objects.<p>
 * The actual session startup begins only when every of these deferred objects are completed.
 * This gives the possibility to dynamically load additional scripts or files which are mandatory for a successful session startup.
 * The individual bootstrap functions may return null or undefined, a single deferred or multiple deferreds as an array.
 */
scout.App.prototype._bootstrap = function(options) {
  options = options || {};
  var deferredValues = this._doBootstrap(options);

  var deferreds = [];
  deferredValues.forEach(function(value) {
    if (Array.isArray(value)) {
      deferreds.concat(value);
    } else if (value) {
      deferreds.push(value);
    }
  });
  return deferreds;
};

scout.App.prototype._doBootstrap = function(options) {
  return [
    scout.logging.bootstrap(),
    scout.device.bootstrap(),
    scout.fonts.bootstrap(options.fonts),
    scout.models.bootstrap(options.modelsUrl),
    scout.locales.bootstrap(),
    scout.texts.bootstrap(),
    scout.codes.bootstrap()
  ];
};

/**
 * Initializes a session for each html element with class '.scout' and stores them in scout.sessions.
 */
scout.App.prototype._init = function(options) {
  options = options || {};
  if (!scout._checkBrowserCompability(options)) {
    return;
  }

  scout.polyfills.install(window);
  scout.prepareDOM();
  scout.objectFactory.init();
  scout._installGlobalJavascriptErrorHandler();
  scout._installGlobalMouseDownInterceptor(document);
  scout._globalAjaxSetup();

  $('.scout').each(function(i, elem) {
    var $entryPoint = $(elem);
    options.portletPartId = options.portletPartId || $entryPoint.data('partid') || '0';
    var session = this._createSession($entryPoint, options);
    scout.sessions.push(session);
  }.bind(this));
};

scout.App.prototype._createSession = function($entryPoint, options) {
  options.locale = options.locale || new scout.Locale(scout.locales.get('de-CH')); //FIXME CGU wo soll die locale definiert werden? Initial vom browser auslesen?
  options.$entryPoint = $entryPoint;
  var session = scout.create('Session', options, {
    ensureUniqueId: false
  });

  // FIXME improve this, start must not be executed because it currently does a server request
  var parent = new scout.NullWidget();
  parent.session = session;
  session.desktop = this._createDesktop(parent);
  session.render(function() {
    this.onSessionReady(session);
    session._renderDesktop();
    this.onDesktopReady(session.desktop);

    // Ensure layout is valid (explicitly layout immediately and don't wait for setTimeout to run to make layouting invisible to the user)
    session.layoutValidator.validate();
    session.focusManager.validateFocus();

    session.ready = true;
    $.log.info('Session initialized. Detected ' + scout.device);
  }.bind(this));
  return session;
};

scout.App.prototype._createDesktop = function(parent) {
  return scout.create('Desktop', {
    parent: parent
  });
};

scout.App.prototype.onSessionReady = function(session) {
  // NOP
};

scout.App.prototype.onDesktopReady = function(desktop) {
  // NOP
};


