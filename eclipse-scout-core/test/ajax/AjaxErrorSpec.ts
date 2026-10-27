/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AjaxError} from '../../src/index';

describe('AjaxError', () => {

  function createAjaxError(status: number, opts?: Partial<AjaxError>): AjaxError {
    return new AjaxError({
      jqXHR: {status} as JQuery.jqXHR,
      textStatus: 'error',
      errorThrown: status ? 'Internal Server Error' : '',
      requestOptions: {
        method: 'POST',
        url: 'https://server.example/api'
      },
      ...opts
    });
  }

  describe('formatStatus', () => {
    it('returns the HTTP status and the error thrown', () => {
      expect(createAjaxError(500).formatStatus()).toBe('500 Internal Server Error');
      expect(createAjaxError(404, {errorThrown: null}).formatStatus()).toBe('404');
    });

    it('returns "Connection error" if there is no HTTP status', () => {
      expect(createAjaxError(0).formatStatus()).toBe('Connection error');
      expect(createAjaxError(0, {jqXHR: null}).formatStatus()).toBe('Connection error');
    });

    it('returns the text status for timed out or aborted requests', () => {
      expect(createAjaxError(0, {textStatus: 'timeout'}).formatStatus()).toBe('timeout');
      expect(createAjaxError(0, {textStatus: 'abort'}).formatStatus()).toBe('abort');
    });
  });

  describe('formatRequest', () => {
    it('returns the method and the url', () => {
      expect(createAjaxError(500).formatRequest()).toBe('POST https://server.example/api');
      expect(createAjaxError(500, {requestOptions: {url: 'https://server.example/api'}}).formatRequest()).toBe('https://server.example/api');
    });

    it('returns an empty string if there are no request options', () => {
      expect(createAjaxError(500, {requestOptions: null}).formatRequest()).toBe('');
    });
  });

  describe('formatMessage', () => {
    it('describes the failed request', () => {
      expect(createAjaxError(500).formatMessage()).toBe('AJAX call "POST https://server.example/api" failed [500 Internal Server Error]');
      expect(createAjaxError(0, {requestOptions: null}).formatMessage()).toBe('AJAX call failed [Connection error]');
    });
  });

  describe('toString', () => {
    it('returns the type and the message', () => {
      let error = createAjaxError(500);
      expect(error.toString()).toBe('AjaxError: AJAX call "POST https://server.example/api" failed [500 Internal Server Error]');
      expect(`${error}`).toBe(error.toString());
    });

    it('contains the message of the ErrorDo', () => {
      let error = createAjaxError(500, {
        jqXHR: {
          status: 500,
          responseJSON: {error: {_type: 'scout.Error', message: 'Something went wrong'}}
        } as JQuery.jqXHR
      });
      expect(error.toString()).toBe('AjaxError: Something went wrong\nAJAX call "POST https://server.example/api" failed [500 Internal Server Error]');
    });
  });
});
