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
// eslint-disable-next-line max-classes-per-file
import {Call} from '../../src/index';

describe('scout.Call', () => {

  beforeEach(() => {
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  // ----- Test classes -----

  class SuccessCall extends Call {
    _callImpl() {
      let deferred = $.Deferred();
      deferred.resolve();
      return deferred.promise();
    }
  }

  class FailCall extends Call {
    constructor() {
      super();
      this.maxRetries = 0;
    }

    _callImpl() {
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

    _callImpl() {
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

});
