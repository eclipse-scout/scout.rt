/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ErrorHandler, scout} from '../src/index';

describe('ErrorHandler', () => {

  let errorHandler: ErrorHandler;

  beforeEach(() => {
    errorHandler = scout.create(ErrorHandler);
  });

  describe('handle', () => {
    it('accepts individually passed arguments', done => {
      errorHandler = scout.create(ErrorHandler);
      spyOn(errorHandler, 'analyzeError').and.callThrough();

      let deferred = $.Deferred();
      deferred.promise().then(() => {
        return $.rejectedPromise('err', 'a', 'b');
      }).catch((...args) => {
        // eslint-disable-next-line prefer-rest-params
        return errorHandler.handle(...args);
      }).then(() => {
        expect(errorHandler.analyzeError).toHaveBeenCalledWith('err', 'a', 'b');
        done();
      });
      deferred.resolve();
    });

    it('accepts argument like object as first param', done => {
      errorHandler = scout.create(ErrorHandler);
      spyOn(errorHandler, 'analyzeError').and.callThrough();

      let deferred = $.Deferred();
      deferred.promise().then(() => {
        return $.rejectedPromise('err', 'a', 'b');
      }).catch(function() {
        // eslint-disable-next-line prefer-rest-params
        return errorHandler.handle(arguments);
      }).then(() => {
        expect(errorHandler.analyzeError).toHaveBeenCalledWith('err', 'a', 'b');
        done();
      });
      deferred.resolve();
    });

    it('accepts array as first param', done => {
      errorHandler = scout.create(ErrorHandler);
      spyOn(errorHandler, 'analyzeError').and.callThrough();

      let deferred = $.Deferred();
      deferred.promise().then(() => {
        return $.rejectedPromise('err', 'a', 'b');
      }).catch((...args) => {
        // eslint-disable-next-line prefer-rest-params
        return errorHandler.handle(args);
      }).then(() => {
        expect(errorHandler.analyzeError).toHaveBeenCalledWith('err', 'a', 'b');
        done();
      });
      deferred.resolve();
    });
  });

  describe('analyzeError', () => {

    it('can handle errors', done => {
      let error = new TypeError('Oops, wrong type!');
      error['debugInfo'] = 'Dummy error';
      errorHandler.analyzeError(error).then(errorInfo => {
        expect(errorInfo.code).toBe('T6');
        expect(errorInfo.message).toBe('Oops, wrong type!');
        expect(errorInfo.debugInfo).toBe('Dummy error');
        expect(errorInfo.log).toContain('Oops, wrong type!');
      }).always(done);
    });

    it('can handle jQuery AJAX errors', done => {
      let fakeXHR = {
        readyState: 4,
        status: 404,
        statusText: 'Not found'
      };

      let promises = [];
      promises.push(errorHandler.analyzeError(fakeXHR).then(errorInfo => {
        expect(errorInfo.code).toBe('X404');
        expect(errorInfo.message).toBe('AJAX call failed [404]');
        expect(errorInfo.log).toContain('AJAX call failed [404]');
      }));

      promises.push(errorHandler.analyzeError([fakeXHR]).then(errorInfo => {
        expect(errorInfo.code).toBe('X404');
        expect(errorInfo.message).toBe('AJAX call failed [404]');
        expect(errorInfo.log).toContain('AJAX call failed [404]');
      }));

      fakeXHR.status = 500;
      fakeXHR.statusText = 'Internal Server Error';
      promises.push(errorHandler.analyzeError(fakeXHR, 'error', 'Internal Server Error', {
        type: 'POST',
        url: 'http://server.example/service'
      }).then(errorInfo => {
        expect(errorInfo.code).toBe('X500');
        expect(errorInfo.message).toBe('AJAX call "POST http://server.example/service" failed [500 Internal Server Error]');
        expect(errorInfo.log).toContain('AJAX call "POST http://server.example/service" failed [500 Internal Server Error]');
      }));

      $.promiseAll(promises).always(done);
    });

    it('can handle no arguments', done => {
      errorHandler.analyzeError().then(errorInfo => {
        expect(errorInfo.code).toBe('P3');
        expect(errorInfo.message).toBe('Unknown error');
        expect(errorInfo.log).toContain('Unexpected error (no reason provided)');
      }).always(done);
    });

    it('can handle arbitrary error objects', done => {
      let promises = [];
      promises.push(errorHandler.analyzeError({
        exception: 'it does not work'
      }).then(errorInfo => {
        expect(errorInfo.code).toBe('P4');
        expect(errorInfo.message).toBe('Unexpected error');
        expect(errorInfo.log).toContain('Unexpected error: {"exception":"it does not work"}');
      }));

      let cyclicObject: any = {};
      cyclicObject.ref = cyclicObject;
      promises.push(errorHandler.analyzeError(cyclicObject).then(errorInfo => {
        expect(errorInfo.code).toBe('P4');
        expect(errorInfo.message).toBe('Unexpected error');
        expect(errorInfo.log).toContain('Unexpected error: [object Object]');
      }));

      promises.push(errorHandler.analyzeError('still broken').then(errorInfo => {
        expect(errorInfo.code).toBe('P4');
        expect(errorInfo.message).toBe('still broken');
        expect(errorInfo.log).toContain('Unexpected error: still broken');
      }));

      promises.push(errorHandler.analyzeError(['a', 2, new Date()]).then(errorInfo => {
        expect(errorInfo.code).toBe('P4');
        expect(errorInfo.message).toBe('Unexpected error');
        expect(errorInfo.log).toContain('Unexpected error: ["a",2,');
      }));

      promises.push(errorHandler.analyzeError(1234567890).then(errorInfo => {
        expect(errorInfo.code).toBe('P4');
        expect(errorInfo.message).toBe('1234567890');
        expect(errorInfo.log).toContain('Unexpected error: 1234567890');
      }));

      $.promiseAll(promises).always(done);
    });

  });

});
