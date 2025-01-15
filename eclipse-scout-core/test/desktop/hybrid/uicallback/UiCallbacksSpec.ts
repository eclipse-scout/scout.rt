/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {BaseDoEntity, DataObjectInventory, DeepPartial, Desktop, ObjectFactory, scout, typeName, UiCallbackErrorDo, UiCallbackHandler, UiCallbackResponse, UiCallbacks} from '../../../../src/index';

describe('UiCallbacks', () => {

  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  it('returns result of handler', done => {
    @typeName('Test')
    class TestDo extends BaseDoEntity {
      foo: string;
    }

    DataObjectInventory.get().add(TestDo);

    class SimpleUiCallbackHandler implements UiCallbackHandler {
      handle(callbackId: string, owner: Desktop, request: BaseDoEntity): JQuery.Promise<BaseDoEntity> {
        return $.resolvedPromise(scout.create(TestDo, {_type: 'Test', foo: 'bar'}));
      }
    }

    const callbackId = 'simpleUiCallback';
    const expectedResponse = {
      id: callbackId,
      data: {_type: 'Test', foo: 'bar'},
      error: null
    };
    expectUiCallbackResponse(SimpleUiCallbackHandler, callbackId, expectedResponse).then(() => done());
    DataObjectInventory.get().remove(TestDo);
  });

  it('uses UiCallbackErrorDo returned by handler', done => {
    const err = scout.create(UiCallbackErrorDo, {
      message: 'Test Error',
      code: 'Test Code'
    });

    class UiCallbackHandlerReturningError implements UiCallbackHandler {
      handle(callbackId: string, owner: Desktop, request: BaseDoEntity): JQuery.Promise<BaseDoEntity> {
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
      handle(callbackId: string, owner: Desktop, request: BaseDoEntity): JQuery.Promise<BaseDoEntity> {
        return $.rejectedPromise('Test Error');
      }
    }

    const callbackId = 'RejectedUiCallback';
    expectUiCallbackResponse(RejectedUiCallbackHandler, callbackId, {
      id: callbackId,
      data: null,
      error: scout.create(UiCallbackErrorDo, {
        message: 'Test Error',
        code: 'P4'
      })
    }).then(() => done());
  });

  it('returns UiCallbackErrorDo if handler throws error', done => {
    class ThrowingUiCallbackHandler implements UiCallbackHandler {
      handle(callbackId: string, owner: Desktop, request: BaseDoEntity): JQuery.Promise<BaseDoEntity> {
        throw 'Test Error';
      }
    }

    const callbackId = 'ThrowingUiCallback';
    expectUiCallbackResponse(ThrowingUiCallbackHandler, callbackId, {
      id: callbackId,
      data: null,
      error: scout.create(UiCallbackErrorDo, {
        message: 'Test Error',
        code: 'P4'
      })
    }).then(() => done());
  });

  function expectUiCallbackResponse<TObject extends BaseDoEntity = BaseDoEntity>(Handler: new() => UiCallbackHandler, callbackId: string, expectedResponse: DeepPartial<UiCallbackResponse<TObject>>): JQuery.Promise<void> {
    const handlerObjectType = 'SpecUiCallbackHandler';
    const desktop = session.desktop;

    const objectFactory = ObjectFactory.get();
    objectFactory.register(handlerObjectType, () => new Handler());

    const expectation = scout.create(UiCallbackResponse<TObject>, $.extend({}, expectedResponse, {id: callbackId}));
    const uiCallbacks = scout.create(UiCallbacks, {parent: desktop});
    const responseAvailable = uiCallbacks.when('uiResponse').then(e => expect(e.data.equals(expectation)).toBeTrue());
    uiCallbacks.onUiCallbackRequest(handlerObjectType, callbackId, desktop, null);

    objectFactory.unregister(handlerObjectType);
    return responseAvailable;
  }
});
