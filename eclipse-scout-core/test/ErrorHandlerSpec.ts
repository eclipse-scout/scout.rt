/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ErrorDo, ErrorHandler, ErrorInfo, LogLevel, MessageBox, MessageBoxModel, ModelOf, scout, Session, Status} from '../src/index';

describe('ErrorHandler', () => {

  let errorHandler: ErrorHandler;
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    errorHandler = scout.create(FixtureErrorHandler);
  });

  class FixtureErrorHandler extends ErrorHandler {
    override _buildErrorMessageBoxModel(session: Session, errorInfo: ErrorInfo): ModelOf<MessageBox> {
      return super._buildErrorMessageBoxModel(session, errorInfo);
    }

    override _severityToLogLevel(severity: string): LogLevel {
      return super._severityToLogLevel(severity);
    }
  }

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

    it('can handle ErrorDo severity', () => {
      const handler = errorHandler as FixtureErrorHandler;
      expect(handler._severityToLogLevel('warning')).toBe(LogLevel.WARN);
      expect(handler._severityToLogLevel('ok')).toBe(LogLevel.DEBUG);
      expect(handler._severityToLogLevel('info')).toBe(LogLevel.INFO);
      expect(handler._severityToLogLevel('error')).toBe(LogLevel.ERROR);
      expect(handler._severityToLogLevel('anything')).toBe(LogLevel.ERROR);
    });

    it('can handle ErrorDos in AJAX response', done => {
      let errorDo: ErrorDo = {
        _type: 'scout.Error',
        httpStatus: 404,
        errorCode: 'T1234',
        message: 'test message',
        correlationId: 'Corr1234',
        title: 'test title',
        severity: 'warning'
      };
      let fakeXHR = {
        readyState: 4,
        status: 404,
        statusText: 'Not found',
        responseJSON: {
          error: errorDo
        }
      };
      errorHandler.analyzeError(fakeXHR).then(errorInfo => {
        expect(errorInfo.errorDo).toBe(errorDo);
        expect(errorInfo.message).toBe(errorDo.message);
        expect(errorInfo.level).toBe(LogLevel.WARN);
      }).always(done);
    });
  });

  describe('MessageBoxModel for error', () => {
    it('can handle missing ErrorDo', () => {
      const handler = errorHandler as FixtureErrorHandler;
      let msgBoxModel = handler._buildErrorMessageBoxModel(session, {
        httpStatus: 404,
        code: 'X1234',
        log: 'log'
      });
      expect(msgBoxModel).toEqual({
        header: undefined,
        body: 'The requested resource could not be found.',
        html: null,
        severity: Status.Severity.ERROR,
        iconId: null,
        hiddenText: 'log',
        yesButtonText: 'Ok'
      });
    });

    it('can handle thrown string', () => {
      const handler = errorHandler as FixtureErrorHandler;
      let msgBoxModel = handler._buildErrorMessageBoxModel(session, {
        log: 'log',
        error: 'custom text'
      });
      expect(msgBoxModel).toEqual({
        header: undefined,
        body: 'custom text',
        html: null,
        severity: Status.Severity.ERROR,
        iconId: null,
        hiddenText: 'log',
        yesButtonText: 'Ok'
      });
    });

    it('can handle thrown Status', () => {
      const handler = errorHandler as FixtureErrorHandler;
      let status = Status.ensure({
        message: 'status msg',
        iconId: 'testicon',
        severity: Status.Severity.INFO,
        code: 100
      });
      let msgBoxModel = handler._buildErrorMessageBoxModel(session, {
        log: 'log',
        error: status
      });
      expect(msgBoxModel).toEqual({
        header: undefined,
        body: 'status msg (Code 100).',
        html: null,
        severity: Status.Severity.INFO,
        iconId: 'testicon',
        hiddenText: 'log',
        yesButtonText: 'Ok'
      });
    });

    it('uses ErrorDo if present without _type', () => {
      const handler = errorHandler as FixtureErrorHandler;

      let msgBoxModel = handler._buildErrorMessageBoxModel(session, {
        httpStatus: 404,
        code: 'X1234',
        log: 'log',
        errorDo: {
          title: 'title',
          severity: 'warning',
          // @ts-expect-error
          severityAsInt: Status.Severity.WARNING,
          errorCode: 'Y789',
          message: 'message',
          correlationId: 'Corr567'
        }
      }) as MessageBoxModel;
      expect(msgBoxModel).toEqual({
        header: 'title',
        body: 'message (Code Y789).',
        severity: Status.Severity.WARNING,
        hiddenText: 'log',
        html: '<div class="error-popup-correlation-id">Correlation ID: Corr567</div>',
        iconId: null,
        yesButtonText: 'Ok'
      });
    });

    it('uses ErrorDo if present wit _type', () => {
      const handler = errorHandler as FixtureErrorHandler;

      let msgBoxModel = handler._buildErrorMessageBoxModel(session, {
        httpStatus: 404,
        code: 'X1234',
        log: 'log',
        errorDo: {
          _type: 'scout.Error',
          title: 'title',
          severity: 'warning',
          errorCode: 'Y789',
          message: 'message',
          correlationId: 'Corr567'
        }
      }) as MessageBoxModel;
      expect(msgBoxModel).toEqual({
        header: 'title',
        body: 'message (Code Y789).',
        severity: Status.Severity.WARNING,
        html: '<div class="error-popup-correlation-id">Correlation ID: Corr567</div>',
        hiddenText: 'log',
        iconId: null,
        yesButtonText: 'Ok'
      });
    });

    it('uses ErrorDo with default text if message is "undefined"', () => {
      const handler = errorHandler as FixtureErrorHandler;

      let msgBoxModel = handler._buildErrorMessageBoxModel(session, {
        log: 'log',
        errorDo: {
          _type: 'scout.Error',
          severity: 'error',
          message: 'undefined'
        }
      }) as MessageBoxModel;
      expect(msgBoxModel).toEqual({
        body: 'Unexpected problem', // provided message from errorDo is not used
        severity: Status.Severity.ERROR,
        hiddenText: 'log',
        header: undefined,
        iconId: null,
        html: null,
        yesButtonText: 'Ok'
      });
    });

    it('uses Error message', () => {
      const handler = errorHandler as FixtureErrorHandler;

      let msgBoxModel = handler._buildErrorMessageBoxModel(session, {
        log: 'log',
        error: new Error('test error')
      }) as MessageBoxModel;
      expect(msgBoxModel).toEqual({
        body: 'test error',
        severity: Status.Severity.ERROR,
        hiddenText: 'log',
        header: undefined,
        iconId: null,
        html: null,
        yesButtonText: 'Ok'
      });
    });
  });
});
