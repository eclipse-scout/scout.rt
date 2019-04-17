import * as $ from 'jquery';
import * as scout from './scout';
import ErrorHandler from './ErrorHandler';
import EventSupport from './EventSupport';
import ObjectFactory from './ObjectFactory';
import Locale from './session/Locale';
import Session from './session/Session';
import NullWidget from './widget/NullWidget';
import Desktop from './desktop/Desktop';
import * as models from './utils/models';
import JQueryUtils from './utils/JQueryUtils';
import { JsonResponseError } from './constants';

let scout_appListeners = [];
let scout_errorHandler = null;

// FIXME [awe] ES6: talk about this pattern, see: https://k94n.com/es6-modules-single-instance-pattern
// Let used in global scope makes sure 'instance' is not reused.
export let instance = null;

export default class App {

  constructor() {
    this.events = this._createEventSupport();
    this.sessions = [];

    /// register the listeners which were added to scout before the app is created
    scout_appListeners.forEach(function(listener) {
      this.addListener(listener);
    }, this);
    scout_appListeners = [];
    instance = this;
    scout_errorHandler = this._createErrorHandler();
  }

  getSessions() { // FIXME [awe] ES6: discuss where to put the sessions
    return this.sessions;
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
  };

  /**
   * Initializes the logging framework, polyfills and the object factory.
   * This happens at the prepare phase because all these things should be available from the beginning.
   */
  _prepare(options) {
    return this._prepareLogging().done(function() {
      this._prepareEssentials(options);
      this._prepareDone(options);
    }.bind(this));
  };

  _prepareEssentials(options) {
    //scout.polyfills.install(window);
    ObjectFactory.getInstance().init();
  };

  _prepareDone(options) {
    this.trigger('prepare', {
      options: options
    });
  };

  _prepareLogging(options) {
    return JQueryUtils.resolvedPromise();
    //return scout.logging.bootstrap();
  };

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

    return JQueryUtils.promiseAll(promises)
      .done(this._bootstrapDone.bind(this, options))
      .catch(this._bootstrapFail.bind(this, options));
  };

  _doBootstrap(options) {
    return [
      JQueryUtils.bootstrap(),
      //scout.device.bootstrap(),
      //scout.fonts.bootstrap(options.fonts),
      models.bootstrap(options.modelsUrl)
      //scout.locales.bootstrap(options.localesUrl),
      //scout.texts.bootstrap(options.textsUrl),
      //scout.codes.bootstrap(options.codesUrl)
    ];
  };

  _bootstrapDone(options) {
    //scout.webstorage.removeItem(sessionStorage, 'scout:timeoutPageReload');
    this.trigger('bootstrap', {
      options: options
    });
  };

  _bootstrapFail(options, vararg, textStatus, errorThrown, requestOptions) {

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
    } else if (scout.isPlainObject(vararg) && vararg.error) {
      // Json based error
      // Json errors (normally processed by Session.js) are returned with http status 200
      if (vararg.error.code === JsonResponseError.SESSION_TIMEOUT) {
        this._handleBootstrapTimeoutError(vararg.error, vararg.url);
        return;
      }
    }

    // Make sure promise will be rejected with all original arguments so that it can be eventually handled by this._fail
    var args = scout.argumentsToArray(arguments).slice(1);
    return $.rejectedPromise.apply($, args);
  };

  _isSessionTimeoutStatus(httpStatus) {
    return httpStatus === 401;
  };

  _handleBootstrapTimeoutError(error, url) {
    /*if (scout.webstorage.getItem(sessionStorage, 'scout:timeoutPageReload')) {
            // Prevent loop in case a reload did not solve the problem
            scout.webstorage.removeItem(sessionStorage, 'scout:timeoutPageReload');
            throw new Error('Resource ' + url + ' could not be loaded due to a session timeout, even after a page reload');
        }
        scout.webstorage.setItem(sessionStorage, 'scout:timeoutPageReload', true);
*/
    // See comment in _bootstrapFail for the reasons why to reload here
    scout.reloadPage();
  };

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
    this._ajaxSetup();
    this._installExtensions();
    this._loadSessions(options.session);
  };

  _checkBrowserCompability(options) {
    /*var device = scout.device;
    var app = this;
    return true;
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
                app._init(newOptions);
            });
        });
    });*/
    return true;
  };

  _initVersion(options) {
    this.version = scout.nvl(
      this.version,
      options.version,
      $('scout-version').data('value'));
  };

  _prepareDOM() {
    scout.prepareDOM(document);
  };

  _installGlobalMouseDownInterceptor() {
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
  _installErrorHandler() {
    window.onerror = scout_errorHandler.windowErrorHandler;
  };

  _createErrorHandler() {
    return scout.create(ErrorHandler);
  };

  _ajaxSetup() {
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
  _ajaxDefaults() {
    return {
      beforeSend: this._beforeAjaxCall.bind(this)
    };
  };

  /**
   * Called before every ajax call. Sets the header X-Scout-Correlation-Id.
   * <p>
   * Maybe overridden to set custom headers or to execute other code which should run before an ajax call.
   */
  _beforeAjaxCall(request) {
    //request.setRequestHeader('X-Scout-Correlation-Id', scout.numbers.correlationId());
  };

  _loadSessions(options) {
    options = options || {};
    $('.scout').each(function(i, elem) {
      var $entryPoint = $(elem);
      options.portletPartId = options.portletPartId || $entryPoint.data('partid') || '0';
      var session = this._loadSession($entryPoint, options);
      this.getSessions().push(session);
    }.bind(this));
  };

  _loadSession($entryPoint, options) {
    options.locale = options.locale || this._loadLocale();
    options.$entryPoint = $entryPoint;
    var session = this._createSession(options);

    var parent = new NullWidget();
    parent.session = session;
    this._createDesktop(parent);
    this.trigger('desktopReady');
    session.render(function() {
      session._renderDesktop();

      // Ensure layout is valid (explicitly layout immediately and don't wait for setTimeout to run to make layouting invisible to the user)
      session.layoutValidator.validate();
      //session.focusManager.validateFocus();

      session.ready = true;
      this.trigger('sessionReady', {
        session: session
      });
    }.bind(this));
    return session;
  };

  _createSession(options) {
    return scout.create(Session, options, {
      ensureUniqueId: false
    });
  };

  _createDesktop(parent) {
    return scout.create(Desktop, {
      parent: parent
    });
  };

  /**
   * @returns the locale to be used when no locale is provided as app option. By default the navigators locale is used.
   */
  _loadLocale() {
    return Locale.getDefault();
    //return scout.locales.getNavigatorLocale();
  };

  _initDone(options) {
    this.trigger('init', {
      options: options
    });
  };

  _fail(options, error) {
    var $error = $('body').appendDiv('startup-error', null);
    $error.appendDiv('startup-error-title', 'The application could not be started');

    var args = scout.argumentsToArray(arguments).slice(1);
    var errorInfo = scout_errorHandler.handle(args);
    if (errorInfo.message) {
      $error.appendDiv('startup-error-message', errorInfo.message);
    }

    // Reject with original rejection arguments
    return $.rejectedPromise.apply($, args);
  };

  /**
   * Override this method to install extensions to Scout objects. Since the extension feature replaces functions
   * on the prototype of the Scout objects you must apply 'function patches' to Scout framework or other code before
   * the extensions are installed.
   *
   * The default implementation does nothing.
   */
  _installExtensions() {
    // NOP
  };

  //--- Event handling methods ---
  _createEventSupport() {
    return new EventSupport();
  };

  trigger(type, event) {
    event = event || {};
    event.source = this;
    this.events.trigger(type, event);
  };

  one(type, func) {
    this.events.one(type, func);
  };

  on(type, func) {
    return this.events.on(type, func);
  };

  off(type, func) {
    this.events.off(type, func);
  };

  addListener(listener) {
    this.events.addListener(listener);
  };

  removeListener(listener) {
    this.events.removeListener(listener);
  };

}

