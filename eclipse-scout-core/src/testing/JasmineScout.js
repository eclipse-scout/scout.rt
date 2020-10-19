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
import {arrays, ObjectFactory, scout} from '../index';
import {LocaleSpecHelper, TestingApp} from './index';

window.sandboxSession = options => {
  let $sandbox = $('#sandbox')
    .addClass('scout');

  options = options || {};
  options.portletPartId = options.portletPartId || '0';
  options.backgroundJobPollingEnabled = false;
  options.suppressErrors = true;
  options.renderDesktop = scout.nvl(options.renderDesktop, true);
  options.remote = true; // required so adapters will be registered in the adapter registry
  options.$entryPoint = $sandbox;

  let session = scout.create('Session', options, {
    ensureUniqueId: false
  });

  // Install non-filtering requestToJson() function. This is required to test
  // the value of the "showBusyIndicator" using toContainEvents(). Usually, this
  // flag is filtered from the request before sending the AJAX call, however in
  // the tests we want to keep it.
  session._requestToJson = request => JSON.stringify(request);

  // Simulate successful session initialization
  session.uiSessionId = '1.1';
  session.modelAdapterRegistry[session.uiSessionId] = session;
  session.locale = new LocaleSpecHelper().createLocale('de-CH');

  let desktop = options.desktop || {};
  desktop.navigationVisible = scout.nvl(desktop.navigationVisible, false);
  desktop.headerVisible = scout.nvl(desktop.headerVisible, false);
  desktop.benchVisible = scout.nvl(desktop.benchVisible, false);
  desktop.parent = scout.nvl(desktop.parent, session.root);
  session.desktop = scout.create('Desktop', desktop);
  if (options.renderDesktop) {
    session._renderDesktop();
  }

  // Prevent exception when test window gets resized
  $sandbox.window()
    .off('resize', session.desktop._resizeHandler);
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
  adapter._attachWidget();
  adapter._postCreateWidget();
};

/**
 * Converts the given adapaterDataArray into a map of adapterData where the key
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
  let adapterDataMap = mapAdapterData(adapterDataArray);
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

export function startApp(App) {
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
    context.keys().forEach(context);
  },
  startApp
};
