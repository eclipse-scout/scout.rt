/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('ErrorHandler', function() {

  var errorHandler;

  beforeEach(function() {
    errorHandler = scout.create('ErrorHandler');
  });

  describe('analyzeError', function() {

    it('can handle errors', function() {
      var error = new TypeError('Oops, wrong type!');
      error.debugInfo = 'Dummy error';
      var errorInfo = errorHandler.analyzeError(error);
      expect(errorInfo.code).toBe('T6');
      expect(errorInfo.message).toBe('Oops, wrong type!');
      expect(errorInfo.debugInfo).toBe('Dummy error');
      expect(errorInfo.log).toContain('Unexpected error: Oops, wrong type!');
    });

    it('can handle jQuery AJAX errors', function() {
      var fakeXHR = {
        readyState: 4,
        status: 404,
        statusText: 'Not found'
      };

      var errorInfo = errorHandler.analyzeError(fakeXHR);
      expect(errorInfo.code).toBe('X404');
      expect(errorInfo.message).toBe('AJAX call failed [404]');
      expect(errorInfo.log).toContain('AJAX call failed [404]');

      errorInfo = errorHandler.analyzeError([fakeXHR]);
      expect(errorInfo.code).toBe('X404');
      expect(errorInfo.message).toBe('AJAX call failed [404]');
      expect(errorInfo.log).toContain('AJAX call failed [404]');

      fakeXHR.status = 500;
      fakeXHR.statusText = 'Internal Server Error';
      errorInfo = errorHandler.analyzeError(fakeXHR, 'error', 'Internal Server Error', {
        type: 'POST',
        url: 'http://server.example/service'
      });
      expect(errorInfo.code).toBe('X500');
      expect(errorInfo.message).toBe('AJAX call "POST http://server.example/service" failed [500 Internal Server Error]');
      expect(errorInfo.log).toContain('AJAX call "POST http://server.example/service" failed [500 Internal Server Error]');
    });

    it('can handle no arguments', function() {
      var errorInfo = errorHandler.analyzeError();
      expect(errorInfo.code).toBe('P3');
      expect(errorInfo.message).toBe('Unknown error');
      expect(errorInfo.log).toContain('Unexpected error (no reason provided)');
    });

    it('can handle arbitrary error objects', function() {
      var errorInfo = errorHandler.analyzeError({
        exception: 'it does not work'
      });
      expect(errorInfo.code).toBe('P4');
      expect(errorInfo.message).toBe('Unexpected error');
      expect(errorInfo.log).toContain('Unexpected error: {"exception":"it does not work"}');

      var cyclicObject = {};
      cyclicObject.ref = cyclicObject;
      errorInfo = errorHandler.analyzeError(cyclicObject);
      expect(errorInfo.code).toBe('P4');
      expect(errorInfo.message).toBe('Unexpected error');
      expect(errorInfo.log).toContain('Unexpected error: [object Object]');

      errorInfo = errorHandler.analyzeError('still broken');
      expect(errorInfo.code).toBe('P4');
      expect(errorInfo.message).toBe('still broken');
      expect(errorInfo.log).toContain('Unexpected error: still broken');

      errorInfo = errorHandler.analyzeError(['a', 2, new Date()]);
      expect(errorInfo.code).toBe('P4');
      expect(errorInfo.message).toBe('Unexpected error');
      expect(errorInfo.log).toContain('Unexpected error: ["a",2,');

      errorInfo = errorHandler.analyzeError(1234567890);
      expect(errorInfo.code).toBe('P4');
      expect(errorInfo.message).toBe('1234567890');
      expect(errorInfo.log).toContain('Unexpected error: 1234567890');
    });

  });

});
