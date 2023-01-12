/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, scout, strings} from '../index';
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
   * If a ajax call is not mocked, this fallback will be triggered to show information about which url is not mocked.
   */
  captureNotMockedCalls() {
    jasmine.Ajax.stubRequest(/.*/).andCallFunction((request: JasmineAjaxRequest) => {
      fail('Ajax call not mocked for url: ' + request.url + ', method: ' + request.method);
    });
  }
};
