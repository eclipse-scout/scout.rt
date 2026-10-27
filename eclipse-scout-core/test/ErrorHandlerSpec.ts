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

    override _onUnhandledRejection(event: PromiseRejectionEvent): Promise<void> {
      return super._onUnhandledRejection(event);
    }

    override _logErrorInfo(errorInfo: ErrorInfo, prefix?: string) {
      super._logErrorInfo(errorInfo, prefix);
    }
  }

  function createRejectionEvent(reason: any): PromiseRejectionEvent {
    return {
      reason,
      preventDefault: jasmine.createSpy('preventDefault')
    } as unknown as PromiseRejectionEvent;
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

  describe('_onUnhandledRejection', () => {
    let handler: FixtureErrorHandler;

    beforeEach(() => {
      handler = scout.create(FixtureErrorHandler, {displayError: false, logError: false, sendError: false});
    });

    it('prevents the default and handles the reason', async () => {
      let handlePromise: Promise<ErrorInfo>;
      let origHandle = handler.handle.bind(handler);
      spyOn(handler, 'handle').and.callFake(e => {
        handlePromise = origHandle(e);
        return handlePromise;
      });
      let error = new Error('unhandled');
      let event = createRejectionEvent(error);

      await handler._onUnhandledRejection(event);
      await handlePromise;

      expect(event.preventDefault).toHaveBeenCalled();
      expect(handler.handle).toHaveBeenCalledOnceWith(error);
      expect(ErrorHandler.isConsumed(error)).toBeTrue();
    });

    it('prevents the default but does not handle ignorable rejections', async () => {
      spyOn(handler, 'handle').and.callThrough();
      let event = createRejectionEvent(new AbortError());

      await handler._onUnhandledRejection(event);
      await sleep();

      expect(event.preventDefault).toHaveBeenCalled();
      expect(handler.handle).not.toHaveBeenCalled();
    });

    it('handles a new error with the failure as cause if handling fails', async () => {
      let handleError = new Error('handle failed');
      let handleSpy = spyOn(handler, 'handle').and.returnValues(Promise.reject(handleError), Promise.resolve({}));

      await handler._onUnhandledRejection(createRejectionEvent('reason'));
      await sleep();

      expect(handleSpy).toHaveBeenCalledTimes(2);
      expect(handleSpy.calls.argsFor(0)).toEqual(['reason']);
      let secondError = handleSpy.calls.argsFor(1)[0];
      expect(secondError).toBeInstanceOf(Error);
      expect(secondError.message).toBe('An error occurred while handling a unhandled rejection');
      expect(secondError.cause).toBe(handleError);
    });

    it('logs the error if handling fails twice', async () => {
      let secondHandleError = new Error('second handle failed');
      let handleSpy = spyOn(handler, 'handle').and.returnValues(Promise.reject(new Error('first handle failed')), Promise.reject(secondHandleError));
      spyOn($.log, 'error');

      await handler._onUnhandledRejection(createRejectionEvent('reason'));
      await sleep();

      expect(handleSpy).toHaveBeenCalledTimes(2);
      expect($.log.error).toHaveBeenCalledOnceWith('An error occurred during the second handle() in unhandled rejection', secondHandleError);
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

    it('handles an error only once', async () => {
      let handler = scout.create(FixtureErrorHandler, {displayError: false, logError: true, sendError: false});
      spyOn(handler, '_logErrorInfo');
      let error = new Error('handle once');

      await handler.handle(error);
      expect(handler._logErrorInfo).toHaveBeenCalledTimes(1);

      let errorInfo = await handler.handle(error);
      expect(handler._logErrorInfo).toHaveBeenCalledTimes(1);
      expect(errorInfo.error).toBe(error);
    });
  });

  describe('handleErrorInfo', () => {
    let handler: FixtureErrorHandler;

    beforeEach(() => {
      handler = scout.create(FixtureErrorHandler, {displayError: false, logError: true, sendError: false});
      spyOn(handler, '_logErrorInfo');
    });

    it('marks the error as consumed', async () => {
      let error = new Error('to be consumed');
      let errorInfo = await handler.analyzeError(error);

      await handler.handleErrorInfo(errorInfo);

      expect(handler._logErrorInfo).toHaveBeenCalledOnceWith(errorInfo);
      expect(ErrorHandler.isConsumed(error)).toBeTrue();
    });

    it('ignores consumed errors', async () => {
      spyOn($.log, 'info');
      let error = new Error('already consumed');
      ErrorHandler.consume(error);
      let errorInfo = await handler.analyzeError(error);

      let result = await handler.handleErrorInfo(errorInfo);

      expect(result).toBe(errorInfo);
      expect(handler._logErrorInfo).not.toHaveBeenCalled();
      expect($.log.info).toHaveBeenCalledOnceWith(`Error has already been consumed, ignoring it. Code ${errorInfo.code}.`);
    });

    it('does not display consumed errors', async () => {
      handler.displayError = true;
      handler.session = session;
      let error = new Error('already consumed');
      ErrorHandler.consume(error);

      await handler.handle(error);

      expect(session.desktop.messageBoxes.length).toBe(0);
    });
  });

  describe('_logErrorInfo', () => {
    let handler: FixtureErrorHandler;

    beforeEach(() => {
      handler = scout.create(FixtureErrorHandler);
      spyOn(window.console, 'error');
      spyOn(window.console, 'warn');
      spyOn(window.console, 'log');
    });

    it('does nothing if there is no log message', () => {
      spyOn($.log, 'error');
      handler._logErrorInfo({message: 'msg', level: LogLevel.ERROR});
      handler._logErrorInfo(null);
      expect($.log.error).not.toHaveBeenCalled();
      expect(window.console.error).not.toHaveBeenCalled();
    });

    it('logs using the level of the error info', () => {
      spyOn($.log, 'error');
      spyOn($.log, 'warn');
      spyOn($.log, 'info');

      handler._logErrorInfo({log: 'warn log', level: LogLevel.WARN});
      expect($.log.warn).toHaveBeenCalledOnceWith('warn log');

      handler._logErrorInfo({log: 'info log', level: LogLevel.INFO});
      expect($.log.info).toHaveBeenCalledOnceWith('info log');

      handler._logErrorInfo({log: 'error log', level: LogLevel.ERROR});
      expect($.log.error).toHaveBeenCalledOnceWith('error log');
    });

    it('appends the logs of the causes', () => {
      spyOn($.log, 'error');
      handler._logErrorInfo({
        log: 'outer',
        level: LogLevel.ERROR,
        causeInfo: {
          log: 'middle',
          causeInfo: {
            log: 'inner'
          }
        }
      });
      expect($.log.error).toHaveBeenCalledOnceWith('outer\nCaused by: middle\nCaused by: inner');
    });
  });

  describe('consume', () => {
    it('marks objects as consumed', () => {
      let error = new Error('err');
      expect(ErrorHandler.isConsumed(error)).toBeFalsy();
      ErrorHandler.consume(error);
      expect(ErrorHandler.isConsumed(error)).toBeTrue();

      let obj = {code: 1};
      ErrorHandler.consume(obj);
      expect(ErrorHandler.isConsumed(obj)).toBeTrue();
    });

    it('ignores primitives', () => {
      ErrorHandler.consume('err');
      ErrorHandler.consume(42);
      ErrorHandler.consume(null);
      ErrorHandler.consume(undefined);
      expect(ErrorHandler.isConsumed('err')).toBeFalsy();
      expect(ErrorHandler.isConsumed(42)).toBeFalsy();
      expect(ErrorHandler.isConsumed(null)).toBeFalsy();
      expect(ErrorHandler.isConsumed(undefined)).toBeFalsy();
    });

    it('logs a warning if the object cannot be marked', () => {
      spyOn($.log, 'warn');
      let error = Object.freeze(new Error('frozen'));
      expect(() => ErrorHandler.consume(error)).not.toThrow();
      expect(ErrorHandler.isConsumed(error)).toBeFalsy();
      expect($.log.warn).toHaveBeenCalledOnceWith('Cannot set __consumed property on error object', error);
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

    it('analyzes the causes of errors', async () => {
      let inner = new RangeError('inner');
      let middle = new TypeError('middle', {cause: inner});
      let outer = new Error('outer', {cause: middle});

      let errorInfo = await errorHandler.analyzeError(outer);
      expect(errorInfo.message).toBe('outer');
      expect(errorInfo.causeInfo.error).toBe(middle);
      expect(errorInfo.causeInfo.code).toBe('T6');
      expect(errorInfo.causeInfo.message).toBe('middle');
      expect(errorInfo.causeInfo.log).toContain('middle');
      expect(errorInfo.causeInfo.causeInfo.error).toBe(inner);
      expect(errorInfo.causeInfo.causeInfo.code).toBe('A3');
      expect(errorInfo.causeInfo.causeInfo.causeInfo).toBeUndefined();
    });

    it('ignores causes that are not errors', async () => {
      let errorInfo = await errorHandler.analyzeError(new Error('outer', {cause: 'no error'}));
      expect(errorInfo.message).toBe('outer');
      expect(errorInfo.causeInfo).toBeUndefined();
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
