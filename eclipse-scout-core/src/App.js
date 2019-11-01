/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import {codes, Device, EventSupport, fonts, locales, logging, models, numbers, ObjectFactory, objects, polyfills, scout, Session, texts, webstorage} from './index';
import * as $ from 'jquery';

let instance = null;
let listeners = [];

export default class App {

  static addListener(type, func) {
    var listener = {
      type: type,
      func: func
    };
    if (instance) {
      instance.events.addListener(listener);
    } else {
      listeners.push(listener);
    }
    return listener;
  }

  static get() {
    return instance;
  }

  constructor() {
    this.events = this._createEventSupport();
    this.initialized = false;
    this.sessions = [];

    /// register the listeners which were added to scout before the app is created
    listeners.forEach(function(listener) {
      this.addListener(listener);
    }, this);
    listeners = [];

    instance = this;
    this.errorHandler = this._createErrorHandler();
  }

  /**
   * Main initialization function.<p>
   *
   * Calls this._prepare, this._bootstrap and this._init.<p>
   * At the initial phase the essential objects are initialized, those which are required for the next phases like logging and the object factory.<br>
   * During the bootstrap phase additional scripts may get loaded required for a successful session startup.<br>
   * The actual initialization does not get started before these bootstrap scripts are loaded.
   */
  init(options) {
    options = options || {};
    return this._prepare(options)
      .then(this._bootstrap.bind(this, options.bootstrap))
      .then(this._init.bind(this, options))
      .then(this._initDone.bind(this, options))
      .catch(this._fail.bind(this, options));
  }

  /**
   * Initializes the logging framework, polyfills and the object factory.
   * This happens at the prepare phase because all these things should be available from the beginning.
   */
  _prepare(options) {
    return this._prepareLogging(options)
      .done(function() {
        this._prepareEssentials(options);
        this._prepareDone(options);
      }.bind(this));
  }

  _prepareEssentials(options) {
    polyfills.install(window);
    ObjectFactory.get().init();
  }

  _prepareDone(options) {
    this.trigger('prepare', {
      options: options
    });
    $.log.isDebugEnabled() && $.log.debug('App prepared');
  }

  _prepareLogging(options) {
    return logging.bootstrap();
  }

  /**
   * Executes the default bootstrap functions and returns an array of promises.<p>
   * The actual session startup begins only when every of these promises are completed.
   * This gives the possibility to dynamically load additional scripts or files which are mandatory for a successful session startup.
   * The individual bootstrap functions may return null or undefined, a single promise or multiple promises as an array.
   */
  _bootstrap(options) {
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
      .done(this._bootstrapDone.bind(this, options))
      .catch(this._bootstrapFail.bind(this, options));
  }

  _doBootstrap(options) {
    return [
      Device.get().bootstrap(),
      fonts.bootstrap(options.fonts),
      models.bootstrap(options.modelsUrl),
      locales.bootstrap(options.localesUrl),
      texts.bootstrap(options.textsUrl),
      codes.bootstrap(options.codesUrl)
    ];
  }

  _bootstrapDone(options) {
    webstorage.removeItem(sessionStorage, 'scout:timeoutPageReload');
    this.trigger('bootstrap', {
      options: options
    });
    $.log.isDebugEnabled() && $.log.debug('App bootstrapped');
  }

  _bootstrapFail(options, vararg, textStatus, errorThrown, requestOptions) {
    $.log.isInfoEnabled() && $.log.info('App bootstrap failed');

    // If one of the bootstrap ajax call fails due to a session timeout, the index.html is probably loaded from cache without asking the server for its validity.
    // Normally, loading the index.html should already return a session timeout, but if it is loaded from the (back button) cache, no request will be done and therefore no timeout can be returned.
    // The browser is allowed to display a page when navigating back without issuing a request even though cache-headers are set to must-revalidate.
    // The only way to prevent it would be the no-store header but then pressing back would always reload the page and not only on a session timeout.
    // Sometimes the JavaScript and therefore the ajax calls won't be executed in case the page is loaded from that cache, but sometimes they will nevertheless (we don't know the reasons).
    // So, if it that happens, the server will return a session timeout and the best thing we can do is to reload the page hoping a request for the index.html will be done which eventually will be forwarded to the login page.
    if ($.isJqXHR(vararg)) {
      // Ajax error
      // If a resource returns 401 (unauthorized) it is likely a session timeout.
      // This may happen if no Scout backend is used or a reverse proxy returned the response, otherwise status 200 with an error object would be returned, see below
      if (this._isSessionTimeoutStatus(vararg.status)) {
        var url = requestOptions ? requestOptions.url : '';
        this._handleBootstrapTimeoutError(vararg, url);
        return;
      }
    } else if (objects.isPlainObject(vararg) && vararg.error) {
      // Json based error
      // Json errors (normally processed by Session.js) are returned with http status 200
      if (vararg.error.code === Session.JsonResponseError.SESSION_TIMEOUT) {
        this._handleBootstrapTimeoutError(vararg.error, vararg.url);
        return;
      }
    }

    // Make sure promise will be rejected with all original arguments so that it can be eventually handled by this._fail
    var args = objects.argumentsToArray(arguments).slice(1);
    return $.rejectedPromise.apply($, args);
  }

  _isSessionTimeoutStatus(httpStatus) {
    return httpStatus === 401;
  }

  _handleBootstrapTimeoutError(error, url) {
    $.log.isInfoEnabled() && $.log.info('Timeout error for resource ' + url + '. Reloading page...');
    if (webstorage.getItem(sessionStorage, 'scout:timeoutPageReload')) {
      // Prevent loop in case a reload did not solve the problem
      $.log.isWarnEnabled() && $.log.warn('Prevented automatic reload, startup will likely fail', error, url);
      webstorage.removeItem(sessionStorage, 'scout:timeoutPageReload');
      throw new Error('Resource ' + url + ' could not be loaded due to a session timeout, even after a page reload');
    }
    webstorage.setItem(sessionStorage, 'scout:timeoutPageReload', true);

    // See comment in _bootstrapFail for the reasons why to reload here
    scout.reloadPage();
  }

  /**
   * Initializes a session for each html element with class '.scout' and stores them in scout.sessions.
   */
  _init(options) {
    options = options || {};
    if (!this._checkBrowserCompability(options)) {
      return;
    }

    this._initVersion(options);
    this._prepareDOM();
    this._installErrorHandler();
    this._installGlobalMouseDownInterceptor();
    this._installSyntheticActiveStateHandler();
    this._ajaxSetup();
    this._installExtensions();
    return this._load(options)
      .then(this._loadSessions.bind(this, options.session));
  }

  /**
   * Maybe implemented to load data from a server before the desktop is created.
   * @returns {Promise} promise which is resolved after the loading is complete
   */
  _load(options) {
    return $.resolvedPromise();
  }

  _checkBrowserCompability(options) {
    var device = Device.get();
    var app = this;
    $.log.isInfoEnabled() && $.log.info('Detected browser ' + device.browser + ' version ' + device.browserVersion);
    if (!scout.nvl(options.checkBrowserCompatibility, true) || device.isSupportedBrowser()) {
      // No check requested or browser is supported
      return true;
    }

    $('.scout').each(function() {
      var $entryPoint = $(this),
        $box = $entryPoint.appendDiv(),
        newOptions = objects.valueCopy(options);

      newOptions.checkBrowserCompatibility = false;
      $box.load('unsupported-browser.html', function() {
        $box.find('button').on('click', function() {
          $box.remove();
          app._init(newOptions);
        });
      });
    });
    return false;
  }

  _initVersion(options) {
    this.version = scout.nvl(
      this.version,
      options.version,
      $('scout-version').data('value'));
  }

  _prepareDOM() {
    scout.prepareDOM(document);
  }

  _installGlobalMouseDownInterceptor() {
    scout.installGlobalMouseDownInterceptor(document);
  }

  _installSyntheticActiveStateHandler() {
    scout.installSyntheticActiveStateHandler(document);
  }

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
  _installErrorHandler() {
    window.onerror = this.errorHandler.windowErrorHandler;
    // FIXME bsh, cgu: use ErrorHandler to handle unhandled promise rejections
    //                 --> replace jQuery.Deferred.exceptionHook(error, stack)
  }

  _createErrorHandler() {
    return scout.create('ErrorHandler');
  }

  /**
   * Uses the object returned by {@link #_ajaxDefaults} to setup ajax. The values in that object are used as default values for every ajax call.
   */
  _ajaxSetup() {
    var ajaxDefaults = this._ajaxDefaults();
    if (ajaxDefaults) {
      $.ajaxSetup(ajaxDefaults);
    }
  }

  /**
   * Returns the defaults for every ajax call. You may override it to set custom defaults.
   * By default _beforeAjaxCall is assigned to the beforeSend method.
   * <p>
   * Note: This will affect every ajax call, so use it with care! See also the advice on https://api.jquery.com/jquery.ajaxsetup/.
   */
  _ajaxDefaults() {
    return {
      beforeSend: this._beforeAjaxCall.bind(this)
    };
  }

  /**
   * Called before every ajax call. Sets the header X-Scout-Correlation-Id.
   * <p>
   * Maybe overridden to set custom headers or to execute other code which should run before an ajax call.
   */
  _beforeAjaxCall(request) {
    request.setRequestHeader('X-Scout-Correlation-Id', numbers.correlationId());
    request.setRequestHeader('X-Requested-With', 'XMLHttpRequest'); // explicitly add here because jQuery only adds it automatically if it is no crossDomain request
  }

  _loadSessions(options) {
    options = options || {};
    var promises = [];
    $('.scout').each(function(i, elem) {
      var $entryPoint = $(elem);
      options.portletPartId = options.portletPartId || $entryPoint.data('partid') || '0';
      var promise = this._loadSession($entryPoint, options);
      promises.push(promise);
    }.bind(this));
    return $.promiseAll(promises);
  }

  /**
   * @returns {Promise} promise which is resolved when the session is ready
   */
  _loadSession($entryPoint, options) {
    options.locale = options.locale || this._loadLocale();
    options.$entryPoint = $entryPoint;
    var session = this._createSession(options);
    this.sessions.push(session);

    // TODO [7.0] cgu improve this, start must not be executed because it currently does a server request
    var desktop = this._createDesktop(session.root);
    this.trigger('desktopReady', {
      desktop: desktop
    });
    session.render(function() {
      session._renderDesktop();

      // Ensure layout is valid (explicitly layout immediately and don't wait for setTimeout to run to make layouting invisible to the user)
      session.layoutValidator.validate();
      session.focusManager.validateFocus();

      session.ready = true;
      this.trigger('sessionReady', {
        session: session
      });
      $.log.isInfoEnabled() && $.log.info('Session initialized. Detected ' + Device.get());
    }.bind(this));
    return $.resolvedPromise();
  }

  _createSession(options) {
    return scout.create('Session', options, {
      ensureUniqueId: false
    });
  }

  _createDesktop(parent) {
    return scout.create('Desktop', {
      parent: parent
    });
  }

  /**
   * @returns the locale to be used when no locale is provided as app option. By default the navigators locale is used.
   */
  _loadLocale() {
    return locales.getNavigatorLocale();
  }

  _initDone(options) {
    this.initialized = true;
    this.trigger('init', {
      options: options
    });
    $.log.isInfoEnabled() && $.log.info('App initialized');
  }

  _fail(options, error) {
    $.log.error('App initialization failed', error);
    var $error = $('body').appendDiv('startup-error');
    $error.appendDiv('startup-error-title').text('The application could not be started');

    var args = objects.argumentsToArray(arguments).slice(1);
    var errorInfo = this.errorHandler.handle(args);
    if (errorInfo.message) {
      $error.appendDiv('startup-error-message').text(errorInfo.message);
    }

    // Reject with original rejection arguments
    return $.rejectedPromise.apply($, args);
  }

  /**
   * Override this method to install extensions to Scout objects. Since the extension feature replaces functions
   * on the prototype of the Scout objects you must apply 'function patches' to Scout framework or other code before
   * the extensions are installed.
   *
   * The default implementation does nothing.
   */
  _installExtensions() {
    // NOP
  }

//--- Event handling methods ---
  _createEventSupport() {
    return new EventSupport();
  }

  trigger(type, event) {
    event = event || {};
    event.source = this;
    this.events.trigger(type, event);
  }

  one(type, func) {
    this.events.one(type, func);
  }

  on(type, func) {
    return this.events.on(type, func);
  }

  off(type, func) {
    this.events.off(type, func);
  }

  addListener(listener) {
    this.events.addListener(listener);
  }

  removeListener(listener) {
    this.events.removeListener(listener);
  }

  when(type) {
    return this.events.when(type);
  }
}
/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
