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
  AdapterData, App, arrays, Desktop, FullModelOf, HtmlEnvironment, InitModelOf, JsonErrorResponse, ModelAdapter, ModelOf, ObjectUuidProvider, PermissionCollectionType, RemoteEvent, RemoteRequest, RemoteResponse, scout, Session,
  SessionStartupResponse, Widget, WidgetModel
} from '../index';
import {jasmineScoutMatchers, JasmineScoutUtil, LocaleSpecHelper, TestingApp} from './index';
import 'jasmine-jquery';
import $ from 'jquery';

declare global {

  export class SandboxSession extends Session {
    override _processEvents(events: RemoteEvent[]);

    override _requestToJson(request: RemoteRequest): string;

    override _processSuccessResponse(message: RemoteResponse);

    override _copyAdapterData(adapterData: Record<string, AdapterData>);

    override _processStartupResponse(data: SessionStartupResponse);

    override _resumeBackgroundJobPolling();

    override _processErrorJsonResponse(jsonError: JsonErrorResponse);

    override _processErrorResponse(jqXHR: JQuery.jqXHR, textStatus: JQuery.Ajax.ErrorTextStatus, errorThrown: string, request: RemoteRequest);
  }

  function sandboxSession(options?: SandboxSessionOptions & ModelOf<Session>): SandboxSession;

  function linkWidgetAndAdapter(widget: Widget, adapterClass: (new() => ModelAdapter) | string);

  function mapAdapterData(adapterDataArray: AdapterData | WidgetModel | (AdapterData | WidgetModel)[]): Record<string, AdapterData>;

  function registerAdapterData(adapterDataArray: AdapterData | WidgetModel | (AdapterData | WidgetModel)[], session: SandboxSession): void;

  function removePopups(session: Session, cssClass?: string): void;

  function createSimpleModel<O>(objectType: new(model?: any) => O, session: Session, id?: string): FullModelOf<O> & { objectType: new(model?: any) => O; id: string; session: Session; parent: Widget };
  function createSimpleModel(objectType: string, session: Session, id?: string): { objectType: string; id: string; session: Session; parent: Widget };

  function mostRecentJsonRequest(): RemoteRequest;

  function sandboxDesktop();

  function sendQueuedAjaxCalls(response?: JasmineAjaxResponse, time?: number);

  function receiveResponseForAjaxCall(request: JasmineAjaxRequest, response?: JasmineAjaxResponse);

  function uninstallUnloadHandlers(session: Session);

  function createPropertyChangeEvent(model: { id: string }, properties: object);

  function sleep(duration?: number): JQuery.Promise<void>;
}

export interface SandboxSessionOptions {
  desktop?: ModelOf<Desktop>;
  renderDesktop?: boolean;
}

window.sandboxSession = options => {
  options = options || {} as ModelOf<Session>;
  let $sandbox = $('#sandbox')
    .addClass('scout');

  let model = options as InitModelOf<Session>;
  model.portletPartId = options.portletPartId || '0';
  model.backgroundJobPollingEnabled = false;
  model.suppressErrors = true;
  model.$entryPoint = $sandbox;
  let session = scout.create(Session, model, {
    ensureUniqueId: false
  }) as SandboxSession;
  $sandbox.data('sandboxSession', session);

  // Install non-filtering requestToJson() function. This is required to test
  // the value of the "showBusyIndicator" using toContainEvents(). Usually, this
  // flag is filtered from the request before sending the AJAX call, however in
  // the tests we want to keep it.
  session._requestToJson = request => JSON.stringify(request);

  // Simulate successful session initialization
  session.uiSessionId = '1.1';
  session.modelAdapterRegistry[session.uiSessionId] = session;
  session.locale = new LocaleSpecHelper().createLocale('de-CH');

  let desktop = (options.desktop || {}) as InitModelOf<Desktop>;
  desktop.navigationVisible = scout.nvl(desktop.navigationVisible, false);
  desktop.headerVisible = scout.nvl(desktop.headerVisible, false);
  desktop.benchVisible = scout.nvl(desktop.benchVisible, false);
  desktop.parent = scout.nvl(desktop.parent, session.root);
  session.desktop = scout.create(Desktop, desktop);
  if (scout.nvl(options.renderDesktop, true)) {
    session._renderDesktop();
  }

  // Prevent exception when test window gets resized
  $sandbox.window().off('resize', session.desktop._resizeHandler);
  return session;
};

/**
 * This function links and existing widget with a new adapter instance. This is useful for tests
 * where you have an existing widget and later create a new adapter instance to that widget.
 */
window.linkWidgetAndAdapter = (widget, adapterClass) => {
  let session = widget.session;
  let adapter = scout.create(adapterClass, {
    id: widget.id,
    session: session
  });
  adapter.widget = widget;
  widget.modelAdapter = adapter;
  adapter._attachWidget();
  adapter._postCreateWidget();
};

/**
 * Converts the given adapterDataArray into a map of adapterData where the key
 * is the adapterData.id and the value is the adapterData itself.
 */
window.mapAdapterData = adapterDataArray => {
  let adapterDataMap = {};
  adapterDataArray = arrays.ensure(adapterDataArray);
  adapterDataArray.forEach(adapterData => {
    adapterDataMap[adapterData.id] = adapterData;
  });
  return adapterDataMap;
};

/**
 * Converts the given adapterDataArray into a map of adapterData and registers the adapterData in the Session.
 * Only use this function when your tests requires to have a remote adapter. In that case create widget and
 * remote adapter with Session#getOrCreateWidget().
 */
window.registerAdapterData = (adapterDataArray, session) => {
  let adapterDataMap = window.mapAdapterData(adapterDataArray);
  session._copyAdapterData(adapterDataMap);
};

/**
 * Removes all open popups for the given session.
 * May be used to make sure handlers get properly detached
 */
window.removePopups = (session, cssClass) => {
  cssClass = cssClass || '.popup';
  session.$entryPoint.children(cssClass).each(function() {
    let popup = scout.widget($(this));
    popup.animateRemoval = false;
    popup.remove();
  });
};

window.createSimpleModel = <T>(objectType, session, id) => {
  if (id === undefined) {
    id = ObjectUuidProvider.createUiId();
  }
  let parent = session.desktop;
  return {
    id: id,
    objectType: objectType,
    parent: parent,
    session: session
  };
};

window.mostRecentJsonRequest = () => {
  let req = jasmine.Ajax.requests.mostRecent();
  if (req) {
    return JSON.parse(req.params);
  }
};

window.sandboxDesktop = () => {
  let $sandbox = sandbox() as unknown as JQuery;
  $sandbox.addClass('scout desktop');
  return $sandbox;
};

/**
 * Sends the queued requests and simulates a response as well.
 * @param response if not set an empty success response will be generated
 */
window.sendQueuedAjaxCalls = (response, time) => {
  time = time || 0;
  jasmine.clock().tick(time);

  window.receiveResponseForAjaxCall(null, response);
};

window.receiveResponseForAjaxCall = (request, response) => {
  if (!response) {
    response = {
      status: 200,
      responseText: '{"events":[]}'
    };
  }
  if (!request) {
    request = jasmine.Ajax.requests.mostRecent();
  }
  if (request && request.onload) {
    request.respondWith(response);
  }
};

/**
 * Uninstalls 'beforeunload' and 'unload' events from window that were previously installed by session.start()
 */
window.uninstallUnloadHandlers = session => {
  $(window)
    .off('beforeunload.' + session.uiSessionId)
    .off('unload.' + session.uiSessionId);
};

window.createPropertyChangeEvent = (model, properties) => ({
  target: model.id,
  properties: properties,
  type: 'property'
});

window.sleep = duration => {
  let deferred = $.Deferred();
  setTimeout(() => deferred.resolve(), duration);
  return deferred.promise();
};

export const JasmineScout = {
  runTestSuite(context) {
    this.startApp(TestingApp);

    beforeAll(() => {
      spyOn(scout, 'reloadPage').and.callFake(() => {
        // NOP: disable reloading as this would restart the whole test-suite again and again
      });
    });

    beforeEach(() => {
      jasmine.addMatchers(jasmineScoutMatchers);
    });

    afterEach(() => {
      const $sandbox = $('#sandbox');
      const session = $sandbox.data('sandboxSession');
      $sandbox.removeData('sandboxSession');
      if (session?.layoutValidator) {
        (session.layoutValidator as { _postValidateFunctions: (() => void)[] })._postValidateFunctions = [];
        session.layoutValidator.desktop = null;
      }
      // Remove every handler to avoid a memory leak because widgets are not destroyed properly after tests, so they won't unregister their handlers
      HtmlEnvironment.get().off('propertyChange');
    });

    context.keys().forEach(context);
  },

  startApp(App: new() => App) {
    // App initialization uses promises which are executed asynchronously
    // -> Use the clock to make sure all promise callbacks are executed before any test starts.
    jasmine.clock().install();
    jasmine.Ajax.install();

    new App().init();

    jasmine.clock().tick(1000);

    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  }
};

JasmineScoutUtil.mockRestCall('api/permissions', {type: PermissionCollectionType.ALL});
JasmineScoutUtil.mockRestCall('api/codes', {});
JasmineScoutUtil.mockRestCall('api/parameters', []);
