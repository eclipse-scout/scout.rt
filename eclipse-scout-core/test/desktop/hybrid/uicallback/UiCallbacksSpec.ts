/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {BaseDoEntity, Constructor, DataObjectInventory, DeepPartial, ObjectFactory, objects, scout, typeName, UiCallbackErrorDo, UiCallbackHandler, UiCallbackParam, UiCallbackResult, UiCallbacks} from '../../../../src/index';

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
      handle(param: UiCallbackParam): JQuery.Promise<any> {
        return $.resolvedPromise(scout.create(TestDo, {
          foo: 'bar'
        }));
      }
    }

    const callbackId = 'simpleUiCallback';
    const expectedResult = scout.create(UiCallbackResult, {
      data: scout.create(TestDo, {
        foo: 'bar'
      })
    });
    const expectedError = null;
    expectUiCallbackResponse(SimpleUiCallbackHandler, callbackId, expectedResult, expectedError).then(() => done());
    DataObjectInventory.get().remove(TestDo);
  });

  it('uses UiCallbackErrorDo returned by handler', done => {
    class UiCallbackHandlerReturningError implements UiCallbackHandler {
      handle(param: UiCallbackParam): JQuery.Promise<any> {
        let err = scout.create(UiCallbackErrorDo, {
          message: 'Test Error',
          code: 'Test Code'
        });
        return $.rejectedPromise(err);
      }
    }

    const callbackId = 'ErrorUiCallback';
    const expectedResult = null;
    const expectedError = scout.create(UiCallbackErrorDo, {
      message: 'Test Error',
      code: 'Test Code'
    });
    expectUiCallbackResponse(UiCallbackHandlerReturningError, callbackId, expectedResult, expectedError).then(() => done());
  });

  it('returns UiCallbackErrorDo if handler rejects', done => {
    class RejectedUiCallbackHandler implements UiCallbackHandler {
      handle(param: UiCallbackParam): JQuery.Promise<any> {
        return $.rejectedPromise('Test Error');
      }
    }

    const callbackId = 'RejectedUiCallback';
    const expectedResult = null;
    const expectedError = scout.create(UiCallbackErrorDo, {
      message: 'Test Error',
      code: 'P4'
    });
    expectUiCallbackResponse(RejectedUiCallbackHandler, callbackId, expectedResult, expectedError).then(() => done());
  });

  it('returns UiCallbackErrorDo if handler throws error', done => {
    class ThrowingUiCallbackHandler implements UiCallbackHandler {
      handle(param: UiCallbackParam): JQuery.Promise<any> {
        throw 'Test Error';
      }
    }

    const callbackId = 'ThrowingUiCallback';
    const expectedResult = null;
    const expectedError = scout.create(UiCallbackErrorDo, {
      message: 'Test Error',
      code: 'P4'
    });
    expectUiCallbackResponse(ThrowingUiCallbackHandler, callbackId, expectedResult, expectedError).then(() => done());
  });

  function expectUiCallbackResponse<TObject extends BaseDoEntity = BaseDoEntity>(
    Handler: Constructor<UiCallbackHandler>,
    callbackId: string,
    expectedResult: DeepPartial<UiCallbackResult>,
    expectedError: DeepPartial<UiCallbackErrorDo>
  ): JQuery.Promise<void> {
    const handlerObjectType = 'SpecUiCallbackHandler';
    const desktop = session.desktop;

    const objectFactory = ObjectFactory.get();
    objectFactory.register(handlerObjectType, () => new Handler());

    const uiCallbacks = scout.create(UiCallbacks, {parent: desktop});
    const responseAvailable = uiCallbacks.when('callbackEnd').then(event => {
      expect(event.callbackId).toEqual(callbackId);
      expect(objects.equalsRecursive(event.result, expectedResult)).toBe(true);
      expect(objects.equalsRecursive(event.error, expectedError)).toBe(true);
    });
    uiCallbacks.onCallback(handlerObjectType, callbackId, desktop, null, null);

    return responseAvailable.always(() => objectFactory.unregister(handlerObjectType));
  }
});
