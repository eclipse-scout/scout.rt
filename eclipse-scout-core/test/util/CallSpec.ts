/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Call} from '../../src/index';
import {InitModelOf, scout} from '../../src/scout';

describe('Call', () => {

  beforeEach(() => {
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  // ----- Test classes -----

  class SuccessCall extends Call {
    override init(model?: InitModelOf<this>) {
      super.init(model);
    }

    override _callImpl() {
      let deferred = $.Deferred();
      deferred.resolve();
      return deferred.promise();
    }
  }

  class TestingCall extends Call {
    override _nextRetryImpl(...args): number | boolean {
      return super._nextRetryImpl(...args);
    }

    callAndGetRetry() {
      this.call();
      return this._nextRetryImpl();
    }

    protected _callImpl(): JQuery.Promise<any> {
      return $.resolvedPromise();
    }
  }

  class FailCall extends Call {
    constructor() {
      super();
      this.maxRetries = 0;
    }

    override init(model?: InitModelOf<this>) {
      super.init(model);
    }

    override _callImpl() {
      let deferred = $.Deferred();
      deferred.reject();
      return deferred.promise();
    }
  }

  class FailOnFirstTryCall extends Call {
    constructor() {
      super();
      this.maxRetries = 5;
    }

    override init(model?: InitModelOf<this>) {
      super.init(model);
    }

    override _callImpl() {
      let deferred = $.Deferred();
      if (this.callCounter > 1) {
        deferred.resolve();
      } else {
        deferred.reject();
      }
      return deferred.promise();
    }
  }

  // ----- Tests -----

  it('calls done on success', () => {
    let call = new SuccessCall();
    call.init();
    let done = false;
    call.call()
      .done(() => {
        done = true;
      });

    expect(done).toBe(true);
    expect(call.callCounter).toBe(1);
  });

  it('calls fail on failure', () => {
    let call = new FailCall();
    call.init();
    let failed = false;
    call.call()
      .fail(() => {
        failed = true;
      });

    expect(failed).toBe(true);
    expect(call.callCounter).toBe(1);
  });

  it('retries on failure', () => {
    let call = new FailOnFirstTryCall();
    call.init();
    let done = false;
    let failed = false;
    call.call()
      .done(() => {
        done = true;
      })
      .fail(() => {
        failed = true;
      });

    jasmine.clock().tick(1000);
    expect(done).toBe(true);
    expect(failed).toBe(false);
    expect(call.callCounter).toBe(2);
  });

  it('correctly initializes retry internals', () => {
    let call1 = scout.create(SuccessCall);
    expect(call1.retryIntervals).toEqual([]);
    expect(call1.maxRetries).toBe(0);

    let call2 = scout.create(SuccessCall, {retryIntervals: [100, 200, 300]});
    expect(call2.retryIntervals).toEqual([100, 200, 300]);
    expect(call2.maxRetries).toBe(3);

    let call3 = scout.create(SuccessCall, {maxRetries: 2});
    expect(call3.retryIntervals).toEqual([]);
    expect(call3.maxRetries).toBe(2);

    let call4 = scout.create(SuccessCall, {retryIntervals: [100, 200, 300], maxRetries: 2}); // maxRetries takes precedence
    expect(call4.retryIntervals).toEqual([100, 200, 300]);
    expect(call4.maxRetries).toBe(2);
  });

  it('maxRetries uses default interval if no interval are given', () => {
    let call1 = scout.create(TestingCall);
    expect(call1.callAndGetRetry()).toBeFalsy();
    expect(call1.callAndGetRetry()).toBeFalsy();

    let call2 = scout.create(TestingCall, {maxRetries: 2});
    expect(call2.defaultRetryInterval).toBe(300);
    expect(call2.callAndGetRetry()).toBe(call2.defaultRetryInterval);
    expect(call2.callAndGetRetry()).toBe(call2.defaultRetryInterval);
    expect(call2.callAndGetRetry()).toBeFalsy();
  });

  it('maxRetries uses last interval for remaining calls', () => {
    let call = scout.create(TestingCall, {retryIntervals: [10, 20], maxRetries: 4});
    expect(call.defaultRetryInterval).toBe(300);
    expect(call.maxRetries).toBe(4);
    expect(call.callAndGetRetry()).toBe(10);
    expect(call.callAndGetRetry()).toBe(20);
    expect(call.callAndGetRetry()).toBe(20);
    expect(call.callAndGetRetry()).toBe(20);
    expect(call.callAndGetRetry()).toBeFalsy();
  });
  it('maxRetries can be unlimited', () => {
    let call = scout.create(TestingCall, {retryIntervals: [10, 20], maxRetries: -1});
    expect(call.maxRetries).toBe(-1);
    expect(call.callAndGetRetry()).toBe(10);
    expect(call.callAndGetRetry()).toBe(20);
    expect(call.callAndGetRetry()).toBe(20);
    expect(call.callAndGetRetry()).toBe(20);
    expect(call.callAndGetRetry()).toBe(20);
    expect(call.callAndGetRetry()).toBe(20);
    expect(call.callAndGetRetry()).toBe(20);
    expect(call.callAndGetRetry()).toBe(20);
  });
});
