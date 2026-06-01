/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * AI Disclosure: This file was partially AI-generated.
 * The AI-generated portions are made available under CC0-1.0
 * and not subject to the project's licence.
 *
 * SPDX-License-Identifier: EPL-2.0 and CC0-1.0
 */
import {AbortablePromise, AbortError, arrays, Deferred, PromiseCreator, promises} from '../../src/index';

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

  describe('thenOrNow', () => {
    it('executes the function asynchronously if the value is a promise', async () => {
      await expectAsync(promises.thenOrNow($.resolvedPromise('value'), value => 'async ' + value)).toBeResolvedTo('async value');
    });

    it('executes the function immediately if the value is not a promise', () => {
      expect(promises.thenOrNow('value', value => {
        expect(value).toBe('value');
      })).toBeUndefined();

      expect(promises.thenOrNow('value', value => 'immediate ' + value)).toBe('immediate value');
    });
  });

  describe('ensure', () => {
    it('returns the promise as it is if the value is a promise', () => {
      let promise = $.resolvedPromise();
      expect(promises.ensure(promise)).toBe(promise);
    });

    it('returns a new promise if the value is not a promise', async () => {
      await expectAsync(promises.ensure('3')).toBeResolvedTo('3');
    });
  });

  describe('AbortablePromise', () => {

    describe('constructor', () => {

      it('resolves when executor calls resolve', async () => {
        const abortablePromise = new AbortablePromise<number>(resolve => resolve(42));
        await expectAsync(abortablePromise).toBeResolvedTo(42);
      });

      it('rejects when executor calls reject', async () => {
        const abortablePromise = new AbortablePromise<number>((resolve, reject) => reject('error'));
        await expectAsync(abortablePromise).toBeRejectedWith('error');
      });

      it('is a native Promise', () => {
        const abortablePromise = new AbortablePromise<void>(resolve => resolve());
        expect(abortablePromise).toBeInstanceOf(Promise);
      });
    });

    describe('abort', () => {

      it('rejects the promise with AbortError', async () => {
        const abortablePromise = new AbortablePromise<void>(() => {
        });
        abortablePromise.abort();
        await expectAsync(abortablePromise).toBeRejectedWith(new AbortError());
      });

      it('has no effect after the promise has already resolved', async () => {
        const abortablePromise = new AbortablePromise<string>(resolve => resolve('done'));
        await expectAsync(abortablePromise).toBeResolvedTo('done');
        abortablePromise.abort();
        await expectAsync(abortablePromise).toBeResolvedTo('done');
      });

      it('has no effect after the promise has already rejected', async () => {
        const abortablePromise = new AbortablePromise<void>((resolve, reject) => reject('fail'));
        await expectAsync(abortablePromise).toBeRejectedWith('fail');
        abortablePromise.abort();
        await expectAsync(abortablePromise).toBeRejectedWith('fail');
      });

      it('can be called multiple times without throwing', async () => {
        const abortablePromise = new AbortablePromise<void>(() => {
        });
        abortablePromise.abort();
        abortablePromise.abort();
        await expectAsync(abortablePromise).toBeRejectedWith(new AbortError());
      });
    });

    describe('of', () => {

      it('returns undefined for a falsy argument', () => {
        expect(AbortablePromise.of(null)).toBeUndefined();
        expect(AbortablePromise.of(undefined)).toBeUndefined();
      });

      it('returns an AbortablePromise instance', () => {
        const abortablePromise = AbortablePromise.of(Promise.resolve());
        expect(abortablePromise).toBeInstanceOf(AbortablePromise);
      });

      it('wraps a resolved native Promise', async () => {
        const abortablePromise = AbortablePromise.of(Promise.resolve('hello'));
        await expectAsync(abortablePromise).toBeResolvedTo('hello');
      });

      it('wraps a rejected native Promise', async () => {
        const abortablePromise = AbortablePromise.of(Promise.reject('boom'));
        await expectAsync(abortablePromise).toBeRejectedWith('boom');
      });

      it('is resolved when the wrapped native Promise is resolved', async () => {
        const deferred = new Deferred();
        const abortablePromise = AbortablePromise.of(deferred.promise());
        deferred.resolve('hello');
        await expectAsync(abortablePromise).toBeResolvedTo('hello');
      });

      it('is rejected when the wrapped native Promise is rejected', async () => {
        const deferred = new Deferred();
        const abortablePromise = AbortablePromise.of(deferred.promise());
        deferred.reject('boom');
        await expectAsync(abortablePromise).toBeRejectedWith('boom');
      });

      it('aborting does not affect the original promise', async () => {
        const deferred = new Deferred<string>();
        const abortablePromise = AbortablePromise.of(deferred.promise());
        abortablePromise.abort();
        deferred.resolve('original');
        // the original promise still resolves normally
        await expectAsync(deferred.promise()).toBeResolvedTo('original');
        await expectAsync(abortablePromise).toBeRejectedWith(new AbortError());
      });
    });
  });
});
