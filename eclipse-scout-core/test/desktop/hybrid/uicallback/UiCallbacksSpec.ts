/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Desktop, DoEntity, ObjectFactory, scout, UiCallbackErrorDo, UiCallbackHandler, UiCallbackResponse, UiCallbacks} from '../../../../src/index';

describe('UiCallbacks', () => {

  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  it('returns result of handler', done => {
    class SimpleUiCallbackHandler implements UiCallbackHandler {
      handle(callbackId: string, owner: Desktop, request: DoEntity): JQuery.Promise<DoEntity> {
        return $.resolvedPromise({_type: 'test', foo: 'bar'});
      }
    }

    const callbackId = 'simpleUiCallback';
    expectUiCallbackResponse(SimpleUiCallbackHandler, callbackId, {
      id: callbackId,
      data: {_type: 'test', foo: 'bar'},
      error: null
    }).then(() => done());
  });

  it('uses UiCallbackErrorDo returned by handler', done => {
    const err: UiCallbackErrorDo = {
      _type: 'scout.UiCallbackError',
      message: 'Test Error',
      code: 'Test Code'
    };

    class UiCallbackHandlerReturningError implements UiCallbackHandler {
      handle(callbackId: string, owner: Desktop, request: DoEntity): JQuery.Promise<DoEntity> {
        return $.rejectedPromise(err);
      }
    }

    const callbackId = 'ErrorUiCallback';
    expectUiCallbackResponse(UiCallbackHandlerReturningError, callbackId, {
      id: callbackId,
      data: null,
      error: err
    }).then(() => done());
  });

  it('returns UiCallbackErrorDo if handler rejects', done => {
    class RejectedUiCallbackHandler implements UiCallbackHandler {
      handle(callbackId: string, owner: Desktop, request: DoEntity): JQuery.Promise<DoEntity> {
        return $.rejectedPromise('Test Error');
      }
    }

    const callbackId = 'RejectedUiCallback';
    expectUiCallbackResponse(RejectedUiCallbackHandler, callbackId, {
      id: callbackId,
      data: null,
      error: {
        _type: 'scout.UiCallbackError',
        message: 'Test Error',
        code: 'P4'
      }
    }).then(() => done());
  });

  it('returns UiCallbackErrorDo if handler throws error', done => {
    class ThrowingUiCallbackHandler implements UiCallbackHandler {
      handle(callbackId: string, owner: Desktop, request: DoEntity): JQuery.Promise<DoEntity> {
        throw 'Test Error';
      }
    }

    const callbackId = 'ThrowingUiCallback';
    expectUiCallbackResponse(ThrowingUiCallbackHandler, callbackId, {
      id: callbackId,
      data: null,
      error: {
        _type: 'scout.UiCallbackError',
        message: 'Test Error',
        code: 'P4'
      }
    }).then(() => done());
  });

  function expectUiCallbackResponse<TObject extends DoEntity = DoEntity>(Handler: new() => UiCallbackHandler, callbackId: string, expectedResponse: Partial<UiCallbackResponse<TObject>>): JQuery.Promise<void> {
    const handlerObjectType = 'SpecUiCallbackHandler';
    const desktop = session.desktop;

    const objectFactory = ObjectFactory.get();
    objectFactory.register(handlerObjectType, () => new Handler());

    const expectation: UiCallbackResponse<TObject> = $.extend({}, expectedResponse, {id: callbackId});
    const uiCallbacks = scout.create(UiCallbacks, {parent: desktop});
    const responseAvailable = uiCallbacks.when('uiResponse').then(e => expect(e.data).toEqual(expectation));
    uiCallbacks.onUiCallbackRequest(handlerObjectType, callbackId, desktop, null);

    objectFactory.unregister(handlerObjectType);
    return responseAvailable;
  }
});
