/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {App, arrays, Desktop, DesktopModel, ModelAdapter, ModelAdapterModel, ObjectFactory, scout, Session, SessionModel, Widget, WidgetModel} from '../index';
import {LocaleSpecHelper, TestingApp} from './index';
import {ObjectType} from '../ObjectFactory';
import {AdapterData, RemoteRequest} from '../session/Session';
import 'jasmine-jquery';
import jasmineScoutMatchers from './scoutMatchers';
import {RefModel} from '../types';

declare global {
  function sandboxSession(options?: SandboxSessionOptions & Partial<SessionModel>): Session;

  function linkWidgetAndAdapter(widget: Widget, adapterClass: (new() => ModelAdapter) | string);

  function mapAdapterData(adapterDataArray: (AdapterData | WidgetModel) | (AdapterData | WidgetModel)[]): Record<string, AdapterData>;

  function registerAdapterData(adapterDataArray: AdapterData[], session: Session): void;

  function removePopups(session: Session, cssClass?: string): void;

  function createSimpleModel<T>(objectType: ObjectType<T>, session: Session, id?: string): {
    id: string; objectType: ObjectType<T>; parent: Widget; session: Session;
  };

  function mostRecentJsonRequest(): RemoteRequest;

  function sandboxDesktop();

  function sendQueuedAjaxCalls(response?: JasmineAjaxResponse, time?: number);

  function receiveResponseForAjaxCall(request: JasmineAjaxRequest, response?: JasmineAjaxResponse);

  function uninstallUnloadHandlers(session: Session);

  function createPropertyChangeEvent(model: { id: string }, properties: object);

  function createAdapterModel(widgetModel: ModelAdapterModel): ModelAdapterModel;
}

export interface SandboxSessionOptions {
  desktop?: RefModel<DesktopModel>;
  renderDesktop?: boolean;
}

window.sandboxSession = options => {
  options = options || {};
  let $sandbox = $('#sandbox')
    .addClass('scout');

  let model = options as SessionModel;
  model.portletPartId = options.portletPartId || '0';
  model.backgroundJobPollingEnabled = false;
  model.suppressErrors = true;
  model.$entryPoint = $sandbox;
  let session = scout.create(Session, model, {
    ensureUniqueId: false
  });

  // Install non-filtering requestToJson() function. This is required to test
  // the value of the "showBusyIndicator" using toContainEvents(). Usually, this
  // flag is filtered from the request before sending the AJAX call, however in
  // the tests we want to keep it.
  // @ts-ignore
  session._requestToJson = request => JSON.stringify(request);

  // Simulate successful session initialization
  session.uiSessionId = '1.1';
  session.modelAdapterRegistry[session.uiSessionId] = session;
  session.locale = new LocaleSpecHelper().createLocale('de-CH');

  let desktop = (options.desktop || {}) as DesktopModel;
  desktop.navigationVisible = scout.nvl(desktop.navigationVisible, false);
  desktop.headerVisible = scout.nvl(desktop.headerVisible, false);
  desktop.benchVisible = scout.nvl(desktop.benchVisible, false);
  desktop.parent = scout.nvl(desktop.parent, session.root);
  session.desktop = scout.create(Desktop, desktop);
  if (scout.nvl(options.renderDesktop, true)) {
    // @ts-ignore
    session._renderDesktop();
  }

  // Prevent exception when test window gets resized
  // @ts-ignore
  $sandbox.window().off('resize', session.desktop._resizeHandler);
  return session;
};

window.createSimpleModel = (objectType, session, id) => {
  if (id === undefined) {
    id = ObjectFactory.get().createUniqueId();
  }
  let parent = session.desktop;
  return {
    id: id,
    objectType: objectType,
    parent: parent,
    session: session
  };
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
  // @ts-ignore
  adapter._attachWidget();
  // @ts-ignore
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
 *
 * @param adapterDataArray
 */
window.registerAdapterData = (adapterDataArray, session) => {
  let adapterDataMap = window.mapAdapterData(adapterDataArray);
  // @ts-ignore
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

window.createSimpleModel = (objectType, session, id) => {
  if (id === undefined) {
    id = ObjectFactory.get().createUniqueId();
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
    return $.parseJSON(req.params);
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

/**
 * Returns a new object instance having two properties id, objectType from the given widgetModel.
 * this function is required because the model object passed to the scout.create() function is modified
 * --> model.objectType is changed to whatever string is passed as parameter objectType
 *
 * @param widgetModel
 */

export function startApp(App: new() => App) {
  // App initialization uses promises which are executed asynchronously
  // -> Use the clock to make sure all promise callbacks are executed before any test starts.
  jasmine.clock().install();

  new App().init();

  jasmine.clock().tick(1000);
  jasmine.clock().uninstall();
}

export default {
  runTestSuite: context => {
    startApp(TestingApp);

    beforeEach(() => {
      jasmine.addMatchers(jasmineScoutMatchers);
    });

    context.keys().forEach(context);
  },
  startApp
};
