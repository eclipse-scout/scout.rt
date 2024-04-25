/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, PromiseCreator, promises} from '../../src/index';

describe('promises', () => {

  function createDeferredArray(len) {
    return arrays.init(len, null);
  }

  function createPromiseCreatorForDeferredArray(deferredArray) {
    return new PromiseCreator(deferredArray.map((v, i) => {
      return () => {
        deferredArray[i] = $.Deferred();
        return deferredArray[i];
      };
    }));
  }

  it('oneByOne stops executing after failure', done => {
    let deferredArray = createDeferredArray(3);
    let promiseCreator = createPromiseCreatorForDeferredArray(deferredArray);
    promises.oneByOne(promiseCreator).then(() => {
      fail('Unexpected code branch');
    }, msg => {
      expect(msg).toBe('Foo');
      expect(deferredArray[1]).toBeNull();
      expect(deferredArray[2]).toBeNull();
      done();
    });
    setTimeout(deferredArray[0].reject.bind(deferredArray[0], 'Foo'));
  });

  it('groupwise stops executing after failed group', done => {
    let deferredArray = createDeferredArray(3);
    let promiseCreator = createPromiseCreatorForDeferredArray(deferredArray);
    promises.groupwise(2, promiseCreator).then(() => {
      fail('Unexpected code branch');
    }, msg => {
      expect(msg).toBe('Bar');
      expect(deferredArray[1]).not.toBeNull();
      expect(deferredArray[2]).toBeNull();
      done();
    });
    setTimeout(deferredArray[0].reject.bind(deferredArray[0], 'Bar'));
  });

  it('parallel stops executing after failed promise', done => {
    let deferredArray = createDeferredArray(9);
    let promiseCreator = createPromiseCreatorForDeferredArray(deferredArray);
    promises.parallel(3, promiseCreator).then(() => {
      fail('Unexpected code branch');
    }, msg => {
      expect(msg).toBe(4);
      expect(deferredArray[0].state()).toBe('resolved');
      expect(deferredArray[1].state()).toBe('resolved');
      expect(deferredArray[2].state()).toBe('pending');
      expect(deferredArray[3].state()).toBe('resolved');
      expect(deferredArray[4].state()).toBe('rejected');
      expect(deferredArray[5].state()).toBe('pending');
      expect(deferredArray[6]).toBeNull();
      expect(deferredArray[7]).toBeNull();
      expect(deferredArray[8]).toBeNull();
      done();
    });
    deferredArray[1].then(function() {
      setTimeout(deferredArray[0].resolve.bind(this, 2), 0);
      expect(deferredArray[0]).not.toBeNull();
      expect(deferredArray[1]).not.toBeNull();
      expect(deferredArray[2]).not.toBeNull();
      expect(deferredArray[3]).toBeNull();
      expect(deferredArray[4]).toBeNull();
    });
    deferredArray[0].then(function() {
      setTimeout(deferredArray[3].resolve.bind(this, 3), 0);
      expect(deferredArray[2]).not.toBeNull();
      expect(deferredArray[3]).not.toBeNull();
      expect(deferredArray[4]).toBeNull();
      expect(deferredArray[5]).toBeNull();
      expect(deferredArray[6]).toBeNull();
      deferredArray[3].then(function() {
        setTimeout(deferredArray[4].reject.bind(this, 4), 0);
      });
    });
    deferredArray[1].resolve(1);
  });

  it('does not cut off error arguments', done => {
    let deferredArray = createDeferredArray(1);
    let promiseCreator = createPromiseCreatorForDeferredArray(deferredArray);
    promises.oneByOne(promiseCreator).then(() => {
      fail('Unexpected code branch');
    }, (...args) => {
      expect(args).toBeTruthy();
      expect(args.length).toBe(2);
      expect(args[0]).toBe('Foo');
      expect(args[1]).toBe('Bar');
      done();
    });
    setTimeout(deferredArray[0].reject.bind(deferredArray[0], 'Foo', 'Bar'));
  });

  it('adds all result arguments, one for each deferred', done => {
    let deferredArray = arrays.init(3, null).map(() => {
      return $.Deferred();
    });
    let promiseCreator = new PromiseCreator(deferredArray.map((v, i) => {
      return () => deferredArray[i].promise();
    }));
    promises.groupwise(4, promiseCreator).then((...args) => {
      expect(args).toBeTruthy();
      expect(args.length).toBe(3);
      // same behavior as if multiple Deferred or Promise or Thenable objects have been used with $.when or $.promiseAll method
      // empty argument resolve call adds an undefined to result
      expect(args[0]).toBeUndefined();
      // one argument resolve call just adds the argument to result
      expect(args[1]).toBe('Foo');
      // multiple argument resolve call adds all arguments as an array to result
      expect(args[2]).toEqual(['Bar', true]);
      done();
    }, msg => {
      fail('Unexpected code branch');
      done();
    });
    // resolve order 2, 1, 0
    deferredArray[1].then(() => {
      setTimeout(deferredArray[0].resolve.bind(deferredArray[0]));
    });
    deferredArray[2].then(() => {
      setTimeout(deferredArray[1].resolve.bind(deferredArray[1], 'Foo'));
    });
    deferredArray[2].resolve('Bar', true);
  });

});
