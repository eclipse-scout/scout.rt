/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import {codes, Device, EventSupport, fonts, locales, logging, numbers, ObjectFactory, objects, scout, Session, texts, webstorage} from './index';
import $ from 'jquery';

let instance = null;
let listeners = [];

export default class App {

  static addListener(type, func) {
    let listener = {
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
    this._loadingTimeoutId = null;

    // register the listeners which were added to scout before the app is created
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
   *
   * @param {object} [options]
   * @param {string|string[]} [options.bootstrap.textsUrl] URL or multiple URLs pointing to a json resource containing texts that will be available through texts.js.
   * @param {string} [options.bootstrap.localesUrl] URL pointing to a json resource containing locale information processed by locales.js
   * @param {string} [options.bootstrap.codesUrl] URL pointing to a json resources containing codes that will be available through codes.js
   * @param {object} [options.session] Object to configure the session, see {@link Session.init} for the available options.
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
   * Initializes the logging framework and the object factory.
   * This happens at the prepare phase because all these things should be available from the beginning.
   */
  _prepare(options) {
    return this._prepareLogging(options)
      .done(() => {
        this._prepareEssentials(options);
        this._prepareDone(options);
      });
  }

  _prepareEssentials(options) {
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

    let promises = [];
    this._doBootstrap(options).forEach(value => {
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
      locales.bootstrap(options.localesUrl),
      texts.bootstrap(options.textsUrl),
      codes.bootstrap(options.codesUrl)
    ];
  }

  _bootstrapDone(options) {
    webstorage.removeItemFromSessionStorage('scout:timeoutPageReload');
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
        let url = requestOptions ? requestOptions.url : '';
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
    // eslint-disable-next-line prefer-rest-params
    let args = objects.argumentsToArray(arguments).slice(1);
    return $.rejectedPromise(...args);
  }

  _isSessionTimeoutStatus(httpStatus) {
    return httpStatus === 401;
  }

  _handleBootstrapTimeoutError(error, url) {
    $.log.isInfoEnabled() && $.log.info('Timeout error for resource ' + url + '. Reloading page...');
    if (webstorage.getItemFromSessionStorage('scout:timeoutPageReload')) {
      // Prevent loop in case a reload did not solve the problem
      $.log.isWarnEnabled() && $.log.warn('Prevented automatic reload, startup will likely fail', error, url);
      webstorage.removeItemFromSessionStorage('scout:timeoutPageReload');
      throw new Error('Resource ' + url + ' could not be loaded due to a session timeout, even after a page reload');
    }
    webstorage.setItemToSessionStorage('scout:timeoutPageReload', true);

    // See comment in _bootstrapFail for the reasons why to reload here
    scout.reloadPage();
  }

  /**
   * Initializes a session for each html element with class '.scout' and stores them in scout.sessions.
   */
  _init(options) {
    options = options || {};
    this.setLoading(true);
    let compatibilityPromise = this._checkBrowserCompatibility(options);
    if (compatibilityPromise) {
      this.setLoading(false);
      return compatibilityPromise.then(newOptions => this._init(newOptions));
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

  _checkBrowserCompatibility(options) {
    let device = Device.get();
    $.log.isInfoEnabled() && $.log.info('Detected browser ' + device.browser + ' version ' + device.browserVersion);
    if (!scout.nvl(options.checkBrowserCompatibility, true) || device.isSupportedBrowser()) {
      // No check requested or browser is supported
      return;
    }

    let deferred = $.Deferred();
    let newOptions = objects.valueCopy(options);
    newOptions.checkBrowserCompatibility = false;
    $('.scout').each(function() {
      let $entryPoint = $(this);
      let $box = $entryPoint.appendDiv();

      $box.load('unsupported-browser.html', () => {
        $box.find('button').on('click', () => {
          $box.remove();
          deferred.resolve(newOptions);
        });
      });
    });
    return deferred.promise();
  }

  setLoading(loading) {
    if (loading) {
      this._loadingTimeoutId = setTimeout(() => {
        // Don't start loading if a desktop is already rendered to prevent flickering when the loading will be set to false after app initialization finishes
        if (!this.sessions.some(session => session.desktop && session.desktop.rendered)) {
          this._renderLoading();
        }
      }, 200);
    } else {
      clearTimeout(this._loadingTimeoutId);
      this._loadingTimeoutId = null;
      this._removeLoading();
    }
  }

  _renderLoading() {
    let $body = $('body'),
      $loadingRoot = $body.children('.application-loading-root');
    if (!$loadingRoot.length) {
      $loadingRoot = $body.appendDiv('application-loading-root')
        .addClass('application-loading-root')
        .fadeIn();
    }
    this._renderLoadingElement($loadingRoot, 'application-loading01');
    this._renderLoadingElement($loadingRoot, 'application-loading02');
    this._renderLoadingElement($loadingRoot, 'application-loading03');
  }

  _renderLoadingElement($loadingRoot, cssClass) {
    if ($loadingRoot.children('.' + cssClass).length) {
      return;
    }
    // noinspection JSValidateTypes
    $loadingRoot.appendDiv(cssClass).hide()
      .fadeIn();
  }

  _removeLoading() {
    let $loadingRoot = $('body').children('.application-loading-root');
    // the fadeout animation only contains a to-value and no from-value
    // therefore set the current value to the elements style
    $loadingRoot.css('opacity', $loadingRoot.css('opacity'));
    // Add animation listener before adding the classes to ensure the listener will always be triggered even while debugging
    $loadingRoot.oneAnimationEnd(() => $loadingRoot.remove());
    if ($loadingRoot.css('opacity') === '1') {
      $loadingRoot.addClass('fadeout and-more');
    } else {
      $loadingRoot.addClass('fadeout');
    }
    if (!Device.get().supportsCssAnimation()) {
      // fallback for old browsers that do not support the animation-end event
      $loadingRoot.remove();
    }
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
    // FIXME bsh, cgu: use ErrorHandler to handle unhandled promise rejections. Just replacing jQuery.Deferred.exceptionHook(error, stack) does not work
    // because it is called on every exception and not only on unhandled.
    // https://developer.mozilla.org/en-US/docs/Web/API/Window/unhandledrejection_event would be exactly what we need, but jQuery does not support it.
    // Bluebird has a polyfill -> can it be ported to jQuery?
  }

  _createErrorHandler(opts) {
    opts = $.extend({}, opts);
    return scout.create('ErrorHandler', opts);
  }

  /**
   * Uses the object returned by {@link #_ajaxDefaults} to setup ajax. The values in that object are used as default values for every ajax call.
   */
  _ajaxSetup() {
    let ajaxDefaults = this._ajaxDefaults();
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
    let promises = [];
    $('.scout').each((i, elem) => {
      let $entryPoint = $(elem);
      options.portletPartId = options.portletPartId || $entryPoint.data('partid') || '0';
      let promise = this._loadSession($entryPoint, options);
      promises.push(promise);
    });
    return $.promiseAll(promises);
  }

  /**
   * @returns {Promise} promise which is resolved when the session is ready
   */
  _loadSession($entryPoint, options) {
    options.locale = options.locale || this._loadLocale();
    options.$entryPoint = $entryPoint;
    let session = this._createSession(options);
    this.sessions.push(session);

    // TODO [7.0] cgu improve this, start must not be executed because it currently does a server request
    let desktop = this._createDesktop(session.root);
    this._triggerDesktopReady(desktop);
    session.render(() => {
      session._renderDesktop();

      // Ensure layout is valid (explicitly layout immediately and don't wait for setTimeout to run to make layouting invisible to the user)
      session.layoutValidator.validate();
      session.focusManager.validateFocus();

      session.ready = true;
      this._triggerSessionReady(session);
      $.log.isInfoEnabled() && $.log.info('Session initialized. Detected ' + Device.get());
    });
    return $.resolvedPromise();
  }

  _triggerDesktopReady(desktop) {
    this.trigger('desktopReady', {
      desktop: desktop
    });
  }

  _triggerSessionReady(session) {
    this.trigger('sessionReady', {
      session: session
    });
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
   * @returns {Locale} the locale to be used when no locale is provided as app option. By default the navigators locale is used.
   */
  _loadLocale() {
    return locales.getNavigatorLocale();
  }

  _initDone(options) {
    this.initialized = true;
    this.setLoading(false);
    this.trigger('init', {
      options: options
    });
    $.log.isInfoEnabled() && $.log.info('App initialized');
  }

  _fail(options, error, ...args) {
    $.log.error('App initialization failed.');
    this.setLoading(false);

    let promises = [];
    if (this.sessions.length === 0) {
      promises.push(this.errorHandler.handle(error, ...args)
        .then(errorInfo => {
          this._appendStartupError($('body'), errorInfo.message);
        }));
    } else {
      // Session.js may already display a fatal message box
      // -> don't handle the error again and display multiple error messages
      this.sessions
        .filter(session => !session.ready && !session.isFatalMessageShown())
        .forEach(session => {
          session.$entryPoint.empty();
          promises.push(this._createErrorHandler({session: session}).handle(error));
        });
    }

    // Reject with original rejection arguments
    return $.promiseAll(promises).then(errorInfo => $.rejectedPromise(error, ...args));
  }

  _appendStartupError($parent, message) {
    let $error = $parent.appendDiv('startup-error');
    $error.appendDiv('startup-error-title').text('The application could not be started');
    if (message) {
      $error.appendDiv('startup-error-message').text(message);
    }
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

  // --- Event handling methods ---
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
