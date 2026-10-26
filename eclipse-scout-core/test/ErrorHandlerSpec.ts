/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbortError, AjaxError, ErrorDo, ErrorHandler, ErrorInfo, LogLevel, MessageBox, MessageBoxModel, ModelOf, scout, Session, Status} from '../src/index';

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

  describe('isIgnorableRejection', () => {
    it('returns true for expected cancellations', () => {
      expect(errorHandler.isIgnorableRejection(new AbortError())).toBeTrue();
      expect(errorHandler.isIgnorableRejection(new AjaxError({jqXHR: null, textStatus: 'abort', errorThrown: null, requestOptions: null}))).toBeTrue();
      expect(errorHandler.isIgnorableRejection({abort: true})).toBeTrue();
    });

    it('returns false for other errors', () => {
      expect(errorHandler.isIgnorableRejection(new AjaxError({jqXHR: null, textStatus: 'error', errorThrown: null, requestOptions: null}))).toBeFalse();
      expect(errorHandler.isIgnorableRejection(new Error('abort'))).toBeFalse();
      expect(errorHandler.isIgnorableRejection({abort: false})).toBeFalse();
      expect(errorHandler.isIgnorableRejection(undefined)).toBeFalsy();
    });
  });

  describe('handle', () => {
    it('analyzes and handles the error', async () => {
      errorHandler = scout.create(ErrorHandler, {displayError: false, logError: false, sendError: false});
      spyOn(errorHandler, 'analyzeError').and.callThrough();
      spyOn(errorHandler, 'handleErrorInfo').and.callThrough();

      let errorInfo = await errorHandler.handle('err');

      expect(errorHandler.analyzeError).toHaveBeenCalledWith('err');
      expect(errorHandler.handleErrorInfo).toHaveBeenCalledWith(errorInfo);
      expect(errorInfo.error).toBe('err');
      expect(errorInfo.message).toBe('err');
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
      }).finally(done);
    });

    it('can handle jQuery AJAX errors', async () => {
      let fakeXHR = {
        readyState: 4,
        status: 404,
        statusText: 'Not found'
      };

      let errorInfo = await errorHandler.analyzeError(fakeXHR);
      expect(errorInfo.code).toBe('X404');
      expect(errorInfo.message).toBe('AJAX call failed [404]');
      expect(errorInfo.log).toContain('AJAX call failed [404]');
    });

    it('can handle AjaxErrors', async () => {
      let fakeXHR = {
        readyState: 4,
        status: 500,
        statusText: 'Internal Server Error'
      } as JQuery.jqXHR;
      let error = new AjaxError({
        jqXHR: fakeXHR,
        textStatus: 'error',
        errorThrown: 'Internal Server Error',
        requestOptions: {
          method: 'POST',
          url: 'http://server.example/service'
        }
      });

      let errorInfo = await errorHandler.analyzeError(error);
      expect(errorInfo.code).toBe('X500');
      expect(errorInfo.message).toBe('AJAX call "POST http://server.example/service" failed [500 Internal Server Error]');
      expect(errorInfo.log).toContain('AJAX call "POST http://server.example/service" failed [500 Internal Server Error]');
    });

    it('can handle no arguments', done => {
      errorHandler.analyzeError().then(errorInfo => {
        expect(errorInfo.code).toBe('P3');
        expect(errorInfo.message).toBe('Unknown error');
        expect(errorInfo.log).toContain('Unexpected error (no reason provided)');
      }).finally(done);
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

      $.promiseAll(promises).finally(done);
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
      }).finally(done);
    });

    it('can handle ErrorDo logLevel', done => {
      let errorDo: ErrorDo = {
        _type: 'scout.Error',
        httpStatus: 404,
        errorCode: 'T1234',
        message: 'test message',
        correlationId: 'Corr1234',
        title: 'test title',
        severity: 'warning',
        logLevel: LogLevel.INFO
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
        expect(errorInfo.level).toBe(LogLevel.INFO);
      }).finally(done);
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
        body: 'status msg',
        html: '<div class="error-popup-code">Code: 100</div>',
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
        body: 'message',
        severity: Status.Severity.WARNING,
        hiddenText: 'log',
        html: '<div class="error-popup-code">Code: Y789</div><div class="error-popup-correlation-id">Correlation ID: Corr567</div>',
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
        body: 'message',
        severity: Status.Severity.WARNING,
        html: '<div class="error-popup-code">Code: Y789</div><div class="error-popup-correlation-id">Correlation ID: Corr567</div>',
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
