/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("scout.promises", function() {

  function createDeferredArray(len) {
    return scout.arrays.init(len, null);
  }

  function createPromiseCreatorForDeferredArray(deferredArray) {
    return new scout.PromiseCreator(deferredArray.map(function(v, i) { return function() { deferredArray[i] = new $.Deferred(); return deferredArray[i]; }; }));
  }

  it("oneByOne stops executing after failure", function(done) {
    var deferredArray = createDeferredArray(3);
    var promiseCreator = createPromiseCreatorForDeferredArray(deferredArray);
    scout.promises.oneByOne(promiseCreator).then(function() {
      fail('Unexpected code branch');
    }, function(msg) {
      expect(msg).toBe('Foo');
      expect(deferredArray[1]).toBeNull();
      expect(deferredArray[2]).toBeNull();
      done();
    });
    setTimeout(deferredArray[0].reject.bind(deferredArray[0], 'Foo'));
  });

  it("groupwise stops executing after failed group", function(done) {
    var deferredArray = createDeferredArray(3);
    var promiseCreator = createPromiseCreatorForDeferredArray(deferredArray);
    scout.promises.groupwise(2, promiseCreator).then(function() {
      fail('Unexpected code branch');
    }, function(msg) {
      expect(msg).toBe('Bar');
      expect(deferredArray[1]).not.toBeNull();
      expect(deferredArray[2]).toBeNull();
      done();
    });
    setTimeout(deferredArray[0].reject.bind(deferredArray[0], 'Bar'));
  });

  it("parallel stops executing after failed promise", function(done) {
    var deferredArray = createDeferredArray(9);
    var promiseCreator = createPromiseCreatorForDeferredArray(deferredArray);
    scout.promises.parallel(3, promiseCreator).then(function() {
      fail('Unexpected code branch');
    }, function(msg) {
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

  it("does not cut off error arguments", function(done) {
    var deferredArray = createDeferredArray(1);
    var promiseCreator = createPromiseCreatorForDeferredArray(deferredArray);
    scout.promises.oneByOne(promiseCreator).then(function() {
      fail('Unexpected code branch');
    }, function() {
      expect(arguments).toBeTruthy();
      expect(arguments.length).toBe(2);
      expect(arguments[0]).toBe('Foo');
      expect(arguments[1]).toBe('Bar');
      done();
    });
    setTimeout(deferredArray[0].reject.bind(deferredArray[0], 'Foo', 'Bar'));
  });

  it("adds all result arguments, one for each deferred", function(done) {
    var deferredArray = scout.arrays.init(3, null).map(function() { return new $.Deferred(); });
    var promiseCreator = new scout.PromiseCreator(deferredArray.map(function(v, i) { return function() { return deferredArray[i].promise(); }; }));
    scout.promises.groupwise(4, promiseCreator).then(function() {
      expect(arguments).toBeTruthy();
      expect(arguments.length).toBe(3);
      // same behavior as if multiple Deferred or Promise or Thenable objects have been used with $.when or $.promiseAll method
      // empty argument resolve call adds an undefined to result
      expect(arguments[0]).toBeUndefined();
      // one argument resolve call just adds the argument to result
      expect(arguments[1]).toBe('Foo');
      // multiple argument resolve call adds all arguments as an array to result
      expect(arguments[2]).toEqual(['Bar', true]);
      done();
    }, function(msg) {
      fail('Unexpected code branch');
      done();
    });
    // resolve order 2, 1, 0
    deferredArray[1].then(function() {
      setTimeout(deferredArray[0].resolve.bind(deferredArray[0]));
    });
    deferredArray[2].then(function() {
      setTimeout(deferredArray[1].resolve.bind(deferredArray[1], 'Foo'));
    });
    deferredArray[2].resolve('Bar', true);
  });

});
