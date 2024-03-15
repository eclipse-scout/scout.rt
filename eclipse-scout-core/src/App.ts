/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {
  access, AppEventMap, aria, codes, Desktop, Device, ErrorHandler, Event, EventEmitter, EventHandler, EventListener, EventMapOf, FontDescriptor, fonts, InitModelOf, Locale, locales, logging, numbers, ObjectFactory, objects, scout, Session,
  SessionModel, texts, webstorage, Widget
} from './index';
import $ from 'jquery';

let instance: App = null;
let listeners: EventListener[] = [];
let bootstrappers: (() => JQuery.Promise<void>)[] = [];

export interface AppModel {
  /**
   * Object to configure the session, see {@link Session.init} for the available options.
   */
  session?: SessionModel;
  bootstrap?: AppBootstrapOptions;
  /**
   * True, to check whether the browser fulfills all requirements to run the application. If the check fails, a notification is shown to warn the user about his old browser. Default is true.
   */
  checkBrowserCompatibility?: boolean;
  version?: string;
}

export type JsonErrorResponse = {
  code: number;
  message: string;
};

export interface AppBootstrapOptions {
  /**
   * Fonts that should be preloaded, which means the initialization will not continue until the fonts are loaded.
   * If no fonts are specified, the list of fonts to preload is automatically calculated from the available CSS "@font-face" definitions. This is the default.<br>
   * To disable preloading entirely, set fonts to an empty array.
   */
  fonts?: FontDescriptor[];
  /**
   *  URL or multiple URLs pointing to a resource providing texts that will be available through {@link texts}.
   */
  textsUrl?: string | string[];
  /**
   * URL pointing to a resource providing locale information processed by {@link locales}.
   */
  localesUrl?: string;
  /**
   *  URL pointing to a resource providing codes that will be available through {@link codes}.
   */
  codesUrl?: string;
  /**
   * URL pointing to a resource providing permissions that will be available through {@link access}.
   *
   * @see PermissionCollectionModel
   */
  permissionsUrl?: string;
  /**
   * Custom functions that needs to be executed while bootstrapping.
   * All custom and default bootrappers need to finish successfully before the app will proceed with the initialization.
   */
  bootstrappers?: (() => JQuery.Promise<void>)[];
}

export class App extends EventEmitter {
  static addListener<K extends string & keyof EventMapOf<App>>(type: K, handler: EventHandler<(EventMapOf<App>)[K] & Event<App>>): EventListener {
    let listener = {
      type: type,
      func: handler
    };
    if (instance) {
      instance.events.addListener(listener);
    } else {
      listeners.push(listener);
    }
    return listener;
  }

  /**
   * Adds a function that needs to be executed while bootstrapping.
   * @see AppModel.bootstrappers
   */
  static addBootstrapper(bootrapper: () => JQuery.Promise<void>) {
    if (bootstrappers.indexOf(bootrapper) > -1) {
      throw new Error('Bootstrapper is already registered.');
    }
    bootstrappers.push(bootrapper);
  }

  static get(): App {
    return instance;
  }

  protected static _set(newApp: App) {
    if (instance) {
      $.log.isWarnEnabled() && $.log.warn('Overwriting already existing App "' + instance + '" with "' + newApp + '".');
    }
    instance = newApp;
  }

  declare model: AppModel;
  declare eventMap: AppEventMap;
  declare self: App;

  remote: boolean;
  initialized: boolean;
  sessions: Session[];
  errorHandler: ErrorHandler;
  version: string;
  bootstrappers: (() => JQuery.Promise<void>)[];
  protected _loadingTimeoutId: number;

  constructor() {
    super();
    this.remote = false;
    this.initialized = false;
    this.sessions = [];
    this.bootstrappers = [];
    this._loadingTimeoutId = null;

    // register the listeners which were added to scout before the app is created
    listeners.forEach(listener => {
      this.addListener(listener);
    });
    listeners = [];

    App._set(this);
    this.errorHandler = this._createErrorHandler();
  }

  /**
   * Main initialization function.
   *
   * Calls {@link _prepare}, {@link _bootstrap} and {@link _init}.<br>
   * At the initial phase the essential objects are initialized, those which are required for the next phases like logging and the object factory.<br>
   * During the bootstrap phase additional scripts may get loaded required for a successful session startup.<br>
   * The actual initialization does not get started before these bootstrap scripts are loaded.
   */
  init(options?: InitModelOf<this>): JQuery.Promise<any> {
    options = options || {} as InitModelOf<this>;
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
  protected _prepare(options: AppModel): JQuery.Promise<any> {
    return this._prepareLogging(options)
      .done(() => {
        this._prepareEssentials(options);
        this._prepareDone(options);
      });
  }

  protected _prepareEssentials(options: AppModel) {
    ObjectFactory.get().init();
  }

  protected _prepareDone(options: AppModel) {
    this.trigger('prepare', {
      options: options
    });
    $.log.isDebugEnabled() && $.log.debug('App prepared');
  }

  protected _prepareLogging(options: AppModel): JQuery.Promise<JQuery> {
    return logging.bootstrap();
  }

  /**
   * Executes the bootstrappers.
   *
   * The actual session startup begins only when all promises of the bootstrappers are completed.
   * This gives the possibility to dynamically load additional scripts or files which are mandatory for a successful application startup.
   */
  protected _bootstrap(options: AppBootstrapOptions): JQuery.Promise<any> {
    options = options || {};
    this.bootstrappers = [
      ...this._defaultBootstrappers(options),
      ...this.bootstrappers,
      ...bootstrappers
    ].filter(bootstrapper => !!bootstrapper);

    return $.promiseAll(this._doBootstrap())
      .then(this._bootstrapDone.bind(this, options))
      .catch(this._bootstrapFail.bind(this, options));
  }

  protected _defaultBootstrappers(options: AppBootstrapOptions): (() => JQuery.Promise<void>)[] {
    return [
      Device.get().bootstrap.bind(Device.get()),
      fonts.bootstrap.bind(fonts, options.fonts),
      locales.bootstrap.bind(locales, options.localesUrl),
      texts.bootstrap.bind(texts, options.textsUrl),
      codes.bootstrap.bind(codes, options.codesUrl),
      access.bootstrap.bind(access, options.permissionsUrl)
    ];
  }

  protected _doBootstrap(): JQuery.Promise<any>[] {
    return this.bootstrappers.map(bootstrapper => bootstrapper());
  }

  protected _bootstrapDone(options: AppBootstrapOptions) {
    webstorage.removeItemFromSessionStorage('scout:timeoutPageReload');
    this.trigger('bootstrap', {
      options: options
    });
    $.log.isDebugEnabled() && $.log.debug('App bootstrapped');
  }

  protected _bootstrapFail(options: AppBootstrapOptions, vararg: JQuery.jqXHR | { url?: string; error?: JsonErrorResponse }, textStatus?: JQuery.Ajax.ErrorTextStatus, errorThrown?: string, requestOptions?: JQuery.AjaxSettings)
    : JQuery.Promise<any> | void {
    $.log.isInfoEnabled() && $.log.info('App bootstrap failed');

    // If one of the bootstrap ajax call fails due to a session timeout, the index.html is probably loaded from cache without asking the server for its validity.
    // Normally, loading the index.html should already return a session timeout, but if it is loaded from the (back button) cache, no request will be done and therefore no timeout can be returned.
    // The browser is allowed to display a page when navigating back without issuing a request even though cache-headers are set to must-revalidate.
    // The only way to prevent it would be the no-store header but then pressing back would always reload the page and not only on a session timeout.
    // Sometimes the JavaScript and therefore the ajax calls won't be executed in case the page is loaded from that cache, but sometimes they will nevertheless (we don't know the reasons).
    // So, if it that happens, the server will return a session timeout and the best thing we can do is to reload the page hoping a request for the index.html will be done which eventually will be forwarded to the login page.
    if ($.isJqXHR(vararg)) {
      // AJAX error
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

  protected _isSessionTimeoutStatus(httpStatus: number): boolean {
    return httpStatus === 401;
  }

  protected _handleBootstrapTimeoutError(error: JQuery.jqXHR | JsonErrorResponse, url: string) {
    $.log.isInfoEnabled() && $.log.info('Timeout error for resource ' + url + '. Reloading page...');
    if (webstorage.getItemFromSessionStorage('scout:timeoutPageReload')) {
      // Prevent loop in case reloading did not solve the problem
      $.log.isWarnEnabled() && $.log.warn('Prevented automatic reload, startup will likely fail', error, url);
      webstorage.removeItemFromSessionStorage('scout:timeoutPageReload');
      throw new Error('Resource ' + url + ' could not be loaded due to a session timeout, even after a page reload');
    }
    webstorage.setItemToSessionStorage('scout:timeoutPageReload', true + '');

    // See comment in _bootstrapFail for the reasons why to reload here
    scout.reloadPage();
  }

  /**
   * Initializes a session for each html element with class '.scout' and stores them in scout.sessions.
   */
  protected _init(options: InitModelOf<this>): JQuery.Promise<any> {
    options = options || {} as InitModelOf<this>;
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
   * @returns promise which is resolved after the loading is complete
   */
  protected _load(options: AppModel): JQuery.Promise<any> {
    return $.resolvedPromise();
  }

  protected _checkBrowserCompatibility(options: AppModel): JQuery.Promise<InitModelOf<this>> | null {
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

  setLoading(loading: boolean) {
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

  protected _renderLoading() {
    let $body = $('body'),
      $loadingRoot = $body.children('.application-loading-root');
    if (!$loadingRoot.length) {
      $loadingRoot = $body.appendDiv('application-loading-root')
        .addClass('application-loading-root')
        .fadeIn();
    }
    aria.role($loadingRoot, 'alert');
    aria.screenReaderOnly($loadingRoot.appendDiv('text').attr('lang', 'en-US').text('Loading'));
    this._renderLoadingElement($loadingRoot, 'application-loading01');
    this._renderLoadingElement($loadingRoot, 'application-loading02');
    this._renderLoadingElement($loadingRoot, 'application-loading03');
  }

  protected _renderLoadingElement($loadingRoot: JQuery, cssClass: string) {
    if ($loadingRoot.children('.' + cssClass).length) {
      return;
    }
    $loadingRoot.appendDiv(cssClass).hide()
      .fadeIn();
  }

  protected _removeLoading() {
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

  protected _initVersion(options: AppModel) {
    this.version = scout.nvl(
      this.version,
      options.version,
      $('scout-version').data('value'));
  }

  protected _prepareDOM() {
    scout.prepareDOM(document);
  }

  protected _installGlobalMouseDownInterceptor() {
    scout.installGlobalMouseDownInterceptor(document);
  }

  protected _installSyntheticActiveStateHandler() {
    scout.installSyntheticActiveStateHandler(document);
  }

  /**
   * Installs a global error handler.
   *
   * Note: we do not install an error handler on popup-windows because everything is controlled by the main-window
   * so exceptions will also occur in that window. This also means, the fatal message-box will be displayed in the
   * main-window, even when a popup-window is opened and active.
   *
   * Caution: The error.stack doesn't look the same in different browsers. Chrome for instance puts the error message
   * on the first line of the stack. Firefox does only contain the stack lines, without the message, but in return
   * the stack trace is much longer :)
   */
  protected _installErrorHandler() {
    window.onerror = this.errorHandler.windowErrorHandler;
    // FIXME bsh, cgu: use ErrorHandler to handle unhandled promise rejections. Just replacing jQuery.Deferred.exceptionHook(error, stack) does not work
    // because it is called on every exception and not only on unhandled.
    // https://developer.mozilla.org/en-US/docs/Web/API/Window/unhandledrejection_event would be exactly what we need, but jQuery does not support it.
    // Bluebird has a polyfill -> can it be ported to jQuery?
  }

  protected _createErrorHandler(opts?: InitModelOf<ErrorHandler>): ErrorHandler {
    opts = $.extend({}, opts);
    return scout.create(ErrorHandler, opts);
  }

  /**
   * Uses the object returned by {@link _ajaxDefaults} to set up ajax. The values in that object are used as default values for every ajax call.
   */
  protected _ajaxSetup() {
    let ajaxDefaults = this._ajaxDefaults();
    if (ajaxDefaults) {
      $.ajaxSetup(ajaxDefaults);
    }
  }

  /**
   * Returns the defaults for every ajax call. You may override it to set custom defaults.
   * By default {@link _beforeAjaxCall} is assigned to the beforeSend method.
   *
   * Note: This will affect every ajax call, so use it with care! See also the advice on https://api.jquery.com/jquery.ajaxsetup/.
   */
  protected _ajaxDefaults(): JQuery.AjaxSettings {
    return {
      beforeSend: this._beforeAjaxCall.bind(this)
    };
  }

  /**
   * Called before every ajax call. Sets the header X-Scout-Correlation-Id.
   *
   * Maybe overridden to set custom headers or to execute other code which should run before an ajax call.
   */
  protected _beforeAjaxCall(request: JQuery.jqXHR, settings: JQuery.AjaxSettings) {
    request.setRequestHeader('X-Scout-Correlation-Id', numbers.correlationId());
    request.setRequestHeader('X-Requested-With', 'XMLHttpRequest'); // explicitly add here because jQuery only adds it automatically if it is no crossDomain request
    if (this.sessions[0] && this.sessions[0].ready) {
      request.setRequestHeader('Accept-Language', this.sessions[0].locale.languageTag);
    }
  }

  protected _loadSessions(options: SessionModel): JQuery.Promise<any> {
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
   * @returns promise which is resolved when the session is ready
   */
  protected _loadSession($entryPoint: JQuery, model: Omit<SessionModel, '$entryPoint'>): JQuery.Promise<any> {
    let sessionModel: InitModelOf<Session> = {$entryPoint: $entryPoint};
    let options = $.extend({}, model, sessionModel);
    options.locale = options.locale || this._loadLocale();
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

  /** @internal */
  _triggerDesktopReady(desktop: Desktop) {
    this.trigger('desktopReady', {
      desktop: desktop
    });
  }

  /** @internal */
  _triggerSessionReady(session: Session) {
    this.trigger('sessionReady', {
      session: session
    });
  }

  protected _createSession(options: InitModelOf<Session>): Session {
    return scout.create(Session, options, {
      ensureUniqueId: false
    });
  }

  protected _createDesktop(parent: Widget): Desktop {
    return scout.create(Desktop, {
      parent: parent
    });
  }

  /**
   * @returns the locale to be used when no locale is provided as session option. By default, the navigators locale is used.
   */
  protected _loadLocale(): Locale {
    return locales.getNavigatorLocale();
  }

  protected _initDone(options: AppModel) {
    this.initialized = true;
    this.setLoading(false);
    this.trigger('init', {
      options: options
    });
    $.log.isInfoEnabled() && $.log.info('App initialized');
  }

  protected _fail(options: AppModel, error: any, ...args: any[]): JQuery.Promise<any> {
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
          const errorHandler = this._createErrorHandler({session: session});
          const promise = errorHandler.analyzeError(error).then(info => {
            info.showAsFatalError = true;
            return errorHandler.handleErrorInfo(info);
          });
          promises.push(promise);
        });
    }

    // Reject with original rejection arguments
    return $.promiseAll(promises).then(errorInfo => $.rejectedPromise(error, ...args));
  }

  protected _appendStartupError($parent: JQuery, message: string) {
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
  protected _installExtensions() {
    // NOP
  }
}
