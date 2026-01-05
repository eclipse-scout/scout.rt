/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, PromiseCreator, promises, TaskQueue} from '../../src/index';

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

  describe('TaskQueue', () => {

    const sleep = async (delay: number) => {
      return new Promise((resolve, reject) => setTimeout(resolve, delay));
    };

    it('runs async tasks in scheduled order', async () => {
      let taskQueue = new TaskQueue();
      let result: string[] = [];

      let task1 = async () => {
        result.push('1a');
        await sleep(1);
        result.push('1b');
        await sleep(10);
        result.push('1c');
      };
      let subtask = async () => {
        result.push('sa');
        await sleep(1);
        result.push('sb');
      };
      let task2 = async () => {
        result.push('2a');
        await sleep(1);
        result.push('2b');
        taskQueue.submit(subtask);
        result.push('2c');
        await sleep(1);
        result.push('2d');
      };
      let task3 = async () => {
        result.push('3a');
        await sleep(10);
        result.push('3b');
      };

      expect(taskQueue.length).toBe(0);
      taskQueue.submit(task1);
      taskQueue.submit(task2);
      taskQueue.submit(task3);
      expect(taskQueue.length).toBe(3);

      expect(result).toEqual([]);
      await taskQueue.whenIdle();
      expect(taskQueue.length).toBe(0);
      expect(result).toEqual(['1a', '1b', '1c', '2a', '2b', '2c', '2d', '3a', '3b', 'sa', 'sb']);
    });

    it('creates a new promise when task is scheduled', async () => {
      let taskQueue = new TaskQueue();

      let p1 = taskQueue.whenIdle();
      let p2 = taskQueue.whenIdle();
      taskQueue.submit(async () => {
      });
      let p3 = taskQueue.whenIdle();
      taskQueue.submit(async () => {
      });
      let p4 = taskQueue.whenIdle();
      expect(p1).not.toBe(p2); // because no task was scheduled initially
      expect(p1).not.toBe(p3);
      expect(p3).toBe(p4);

      await p4;
      let p5 = taskQueue.whenIdle();
      let p6 = taskQueue.whenIdle();
      expect(p5).toBe(p4);
      expect(p6).toBe(p5);
    });

    it('ignores failing tasks', async () => {
      let taskQueue = new TaskQueue();
      let result: string[] = [];

      let task1 = async () => {
        result.push('1a');
        await sleep(1);
        result.push('1b');
      };
      let task2 = async () => {
        result.push('2a');
        await sleep(1);
        result.push('2b');
        throw Error('Task failed');
      };
      let task3 = async () => {
        result.push('3a');
        await sleep(1);
        result.push('3b');
      };

      expect(taskQueue.length).toBe(0);
      taskQueue.submit(task1);
      taskQueue.submit(task2);
      taskQueue.submit(task3);
      expect(taskQueue.length).toBe(3);

      expect(result).toEqual([]);
      await taskQueue.whenIdle();
      expect(taskQueue.length).toBe(0);
      expect(result).toEqual(['1a', '1b', '2a', '2b', '3a', '3b']);
    });

    it('can handle task errors explicitly', async () => {
      let taskQueue = new TaskQueue();
      let result: string[] = [];
      let errors: any[] = [];

      taskQueue.withErrorHandler(error => errors.push(error));

      let task1 = async () => {
        result.push('1a');
        await sleep(1);
        result.push('1b');
      };
      let task2 = async () => {
        result.push('2a');
        await sleep(1);
        result.push('2b');
        throw 'Task 2 failed';
      };
      let task3 = async () => {
        result.push('3a');
        await sleep(1);
        result.push('3b');
      };

      expect(taskQueue.length).toBe(0);
      taskQueue.submit(task1);
      taskQueue.submit(task2);
      taskQueue.submit(task3);
      taskQueue.submit(task2);
      expect(taskQueue.length).toBe(4);

      expect(result).toEqual([]);
      await taskQueue.whenIdle();
      expect(taskQueue.length).toBe(0);
      expect(result).toEqual(['1a', '1b', '2a', '2b', '3a', '3b', '2a', '2b']);
      expect(errors).toEqual(['Task 2 failed', 'Task 2 failed']);
    });
  });
});
