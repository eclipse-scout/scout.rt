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
import {scout} from '../src/index';

describe('ErrorHandler', function() {

  var errorHandler;

  beforeEach(function() {
    errorHandler = scout.create('ErrorHandler');
  });

  describe('analyzeError', function() {

    it('can handle errors', function(done) {
      var error = new TypeError('Oops, wrong type!');
      error.debugInfo = 'Dummy error';
      errorHandler.analyzeError(error).then(function(errorInfo) {
        expect(errorInfo.code).toBe('T6');
        expect(errorInfo.message).toBe('Oops, wrong type!');
        expect(errorInfo.debugInfo).toBe('Dummy error');
        expect(errorInfo.log).toContain('Oops, wrong type!');
      }).always(done);
    });

    it('can handle jQuery AJAX errors', function(done) {
      var fakeXHR = {
        readyState: 4,
        status: 404,
        statusText: 'Not found'
      };

      var promises = [];
      promises.push(errorHandler.analyzeError(fakeXHR).then(function(errorInfo) {
        expect(errorInfo.code).toBe('X404');
        expect(errorInfo.message).toBe('AJAX call failed [404]');
        expect(errorInfo.log).toContain('AJAX call failed [404]');
      }));

      promises.push(errorHandler.analyzeError([fakeXHR]).then(function(errorInfo) {
        expect(errorInfo.code).toBe('X404');
        expect(errorInfo.message).toBe('AJAX call failed [404]');
        expect(errorInfo.log).toContain('AJAX call failed [404]');
      }));

      fakeXHR.status = 500;
      fakeXHR.statusText = 'Internal Server Error';
      promises.push(errorHandler.analyzeError(fakeXHR, 'error', 'Internal Server Error', {
        type: 'POST',
        url: 'http://server.example/service'
      }).then(function(errorInfo) {
        expect(errorInfo.code).toBe('X500');
        expect(errorInfo.message).toBe('AJAX call "POST http://server.example/service" failed [500 Internal Server Error]');
        expect(errorInfo.log).toContain('AJAX call "POST http://server.example/service" failed [500 Internal Server Error]');
      }));

      $.promiseAll(promises).always(done);
    });

    it('can handle no arguments', function(done) {
      errorHandler.analyzeError().then(function(errorInfo) {
        expect(errorInfo.code).toBe('P3');
        expect(errorInfo.message).toBe('Unknown error');
        expect(errorInfo.log).toContain('Unexpected error (no reason provided)');
      }).always(done);
    });

    it('can handle arbitrary error objects', function(done) {
      var promises = [];
      promises.push(errorHandler.analyzeError({
        exception: 'it does not work'
      }).then(function(errorInfo) {
        expect(errorInfo.code).toBe('P4');
        expect(errorInfo.message).toBe('Unexpected error');
        expect(errorInfo.log).toContain('Unexpected error: {"exception":"it does not work"}');
      }));

      var cyclicObject = {};
      cyclicObject.ref = cyclicObject;
      promises.push(errorHandler.analyzeError(cyclicObject).then(function(errorInfo) {
        expect(errorInfo.code).toBe('P4');
        expect(errorInfo.message).toBe('Unexpected error');
        expect(errorInfo.log).toContain('Unexpected error: [object Object]');
      }));

      promises.push(errorHandler.analyzeError('still broken').then(function(errorInfo) {
        expect(errorInfo.code).toBe('P4');
        expect(errorInfo.message).toBe('still broken');
        expect(errorInfo.log).toContain('Unexpected error: still broken');
      }));

      promises.push(errorHandler.analyzeError(['a', 2, new Date()]).then(function(errorInfo) {
        expect(errorInfo.code).toBe('P4');
        expect(errorInfo.message).toBe('Unexpected error');
        expect(errorInfo.log).toContain('Unexpected error: ["a",2,');
      }));

      promises.push(errorHandler.analyzeError(1234567890).then(function(errorInfo) {
        expect(errorInfo.code).toBe('P4');
        expect(errorInfo.message).toBe('1234567890');
        expect(errorInfo.log).toContain('Unexpected error: 1234567890');
      }));

      $.promiseAll(promises).always(done);
    });

  });

});
