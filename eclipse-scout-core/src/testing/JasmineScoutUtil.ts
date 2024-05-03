/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, DoEntity, HybridActionEvent, HybridManager, scout, Session, strings, Widget} from '../index';
import $ from 'jquery';
import 'jasmine-ajax';

let _jsonResourceCache = {};

/**
 * Utility functions for jasmine tests.
 */
export const JasmineScoutUtil = {

  /**
   * @returns the loaded JSON data structure
   */
  loadJsonResource(jsonResourceUrl: string, options: { useCache?: boolean } = {}): JQuery.Promise<any> {
    scout.assertParameter('jsonResourceUrl', jsonResourceUrl);

    if (scout.nvl(options.useCache, true)) {
      let json = _jsonResourceCache[jsonResourceUrl];
      if (json) {
        return $.resolvedPromise(json);
      }
    }

    return $.ajax({
      async: false,
      type: 'GET',
      dataType: 'json',
      contentType: 'application/json; charset=UTF-8',
      cache: false,
      url: jsonResourceUrl
    })
      .done(json => {
        if (scout.nvl(options.useCache, true)) {
          _jsonResourceCache[jsonResourceUrl] = json;
        }
        return $.resolvedPromise(json);
      })
      .fail((jqXHR, textStatus, errorThrown) => {
        throw new Error('Could not load resource from url: ' + jsonResourceUrl);
      });
  },

  loadJsonResourceAndMockRestCall(resourceUrlToMock: string, jsonResourceUrl: string, options: {
    useCache?: boolean;
    restriction?: any;
    method?: string;
  } = {}) {
    scout.assertParameter('resourceUrlToMock', resourceUrlToMock);

    JasmineScoutUtil.loadJsonResource(jsonResourceUrl, options)
      .then(json => JasmineScoutUtil.mockRestCall(resourceUrlToMock, json, options));
  },

  mockRestLookupCall(resourceUrlToMock: string, lookupRows: any[], parentRestriction?: any) {
    scout.assertParameter('resourceUrlToMock', resourceUrlToMock);

    // Normalize lookup rows
    lookupRows = arrays.ensure(lookupRows).map(lookupRow => $.extend({
      active: true,
      enabled: true,
      parentId: null
    }, lookupRow));

    // getAll()
    JasmineScoutUtil.mockRestCall(resourceUrlToMock, {
      rows: lookupRows
    }, {
      restriction: parentRestriction
    });

    // getKey()
    lookupRows.forEach(lookupRow => {
      JasmineScoutUtil.mockRestCall(resourceUrlToMock, {
        rows: [lookupRow]
      }, {
        restriction: lookupRow.id
      });
    });
  },

  mockRestCall(resourceUrlToMock: string, responseData: any, options: {
    restriction?: any;
    method?: string;
  } = {}) {
    let url = new RegExp('.*' + strings.quote(resourceUrlToMock) + '.*');
    let data = options.restriction ? new RegExp('.*' + strings.quote(options.restriction) + '.*') : undefined;
    jasmine.Ajax.stubRequest(url, data, options.method).andReturn({
      status: 200,
      responseText: JSON.stringify(responseData)
    });
  },

  /**
   * If an ajax call is not mocked, this fallback will be triggered to show information about which url is not mocked.
   */
  captureNotMockedCalls() {
    jasmine.Ajax.stubRequest(/.*/).andCallFunction((request: JasmineAjaxRequest) => {
      fail('Ajax call not mocked for url: ' + request.url + ', method: ' + request.method);
    });
  },

  /**
   * Calls the given mock as soon as a hybrid action with the given actionType is called.
   * The mock is called asynchronously using setTimeout to let the runtime code add any required event listeners first.
   *
   * The mock may return an object with [id, widget] if the action is supposed to create widgets.
   * The format of the id depends on the method used to add widgets:
   * - `AbstractHybridAction.addWidget(IWidget)` (e.g. `AbstractFormHybridAction`): `${widgetId}`
   * - `AbstractHybridAction.addWidgets(Map<String, IWidget>)`: `${actionId}${widgetId}`
   */
  mockHybridAction<TData extends DoEntity>(session: Session, actionType: string, mock: (event: HybridActionEvent<TData>) => Record<string, Widget>) {
    let hm = HybridManager.get(session);
    hm.on('hybridAction', (event: HybridActionEvent<TData>) => {
      if (event.data.actionType === actionType) {
        setTimeout(() => {
          let widgets = mock(event);
          if (widgets) {
            hm.setProperty('widgets', widgets);
          }
        });
      }
    });
  }
};
