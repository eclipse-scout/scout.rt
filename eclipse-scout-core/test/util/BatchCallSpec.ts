/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {BaseDoEntity, BatchCall, dataObjects, scout, Session, typeName} from '../../src/index';

describe('BatchCall', () => {
  let session: Session;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  function specBatchCall(keys: string[]): JQuery.Promise<Map<string, string>> {
    return $.resolvedPromise(new Map(keys.map(key => [key, `[${key}]`])));
  }

  describe('coalesce', () => {
    it('can be disabled', async () => {

      function dataObjectBatchCall(keys: BaseDoEntity[]): JQuery.Promise<Map<BaseDoEntity, string>> {
        expect(keys).toHaveSize(5); // 2 times inserted do1 is in the array only once
        return $.resolvedPromise(new Map(keys.map(key => [key, key._type])));
      }

      const batchCall = new BatchCall(dataObjectBatchCall, false);
      const do1 = scout.create(BaseDoEntity, {_type: 'coalesce1'});
      const do1_1 = scout.create(BaseDoEntity, {_type: 'coalesce1'});
      const do1_2 = scout.create(BaseDoEntity, {_type: 'coalesce1'});

      const do2 = scout.create(BaseDoEntity, {_type: 'coalesce2'});
      const do3 = scout.create(BaseDoEntity, {_type: 'coalesce3'});

      batchCall.addKey(do1);
      batchCall.addKey(do1_1);
      batchCall.addKey(do2);
      batchCall.addKey(do3);
      batchCall.addKey(do1_2);
      batchCall.addKey(do1);

      const result = await batchCall.promise();
      expect(result).toHaveSize(5);
      expect(result.get(do1)).toBe('coalesce1');
      expect(result.get(do1_1)).toBe('coalesce1');
      expect(result.get(do1_2)).toBe('coalesce1');
      expect(result.get(do2)).toBe('coalesce2');
      expect(result.get(do3)).toBe('coalesce3');
    });
  });

  describe('promise', () => {

    it('returns a new promise after each call', async () => {
      const batchCallSpy = jasmine.createSpy('batchCall', specBatchCall).and.callThrough();
      const batchCall = new BatchCall(batchCallSpy);

      let promise1 = batchCall.promise();
      let result1 = await promise1;
      expect(batchCallSpy).toHaveBeenCalledTimes(0); // because no keys were requested
      expect(result1.size).toBe(0);
      let promise2 = batchCall.promise();
      expect(promise1).not.toBe(promise2);
      let result2 = await promise2;
      expect(batchCallSpy).toHaveBeenCalledTimes(0);
      expect(result2.size).toBe(0);
      expect(result2).not.toBe(result1);

      jasmine.clock().install();
      let promise3 = batchCall.promise();
      jasmine.clock().tick(1000);
      let promise4 = batchCall.promise();
      jasmine.clock().tick(1000);
      jasmine.clock().uninstall();

      expect(promise4).not.toBe(promise3);
      let result3 = await promise3;
      let result4 = await promise4;
      expect(batchCallSpy).toHaveBeenCalledTimes(0);
      expect(result3.size).toBe(0);
      expect(result4.size).toBe(0);
      expect(result3).not.toBe(result4);

      let promise5 = batchCall.promise();
      let promise6 = batchCall.promise();
      let result5 = await promise5;
      let result6 = await promise6;
      expect(batchCallSpy).toHaveBeenCalledTimes(0);
      expect(result5.size).toBe(0);
      expect(result6.size).toBe(0);
      expect(promise5).toBe(promise6);
      expect(result5).toBe(result6);
    });
  });

  describe('addKey', () => {

    it('invokes a single call for all added keys', async () => {
      const batchCallSpy = jasmine.createSpy('batchCall', specBatchCall).and.callThrough();
      const batchCall = new BatchCall(batchCallSpy);

      batchCall.addKey('one');
      batchCall.addKey('two');
      batchCall.addKey('three');

      let result = await batchCall.promise();
      expect(result.size).toBe(3);
      expect(result.get('one')).toBe('[one]');
      expect(result.get('two')).toBe('[two]');
      expect(result.get('three')).toBe('[three]');

      expect(batchCallSpy).toHaveBeenCalledOnceWith(['one', 'two', 'three']);
    });

    it('only looks up each key once', async () => {
      const batchCallSpy = jasmine.createSpy('batchCall', specBatchCall).and.callThrough();
      const batchCall = new BatchCall(batchCallSpy);

      batchCall.addKey('one');
      batchCall.addKey('two');
      batchCall.addKey('two');
      batchCall.addKey('one');
      batchCall.addKey('zero');

      let result = await batchCall.promise();
      expect(result.size).toBe(3);
      expect(result.get('one')).toBe('[one]');
      expect(result.get('two')).toBe('[two]');
      expect(result.get('zero')).toBe('[zero]');

      expect(batchCallSpy).toHaveBeenCalledOnceWith(['one', 'two', 'zero']);
    });

    it('can handle missing data', async () => {
      function emptyBatchCall(keys: string[]): JQuery.Promise<Map<string, string>> {
        return null;
      }

      const batchCallSpy = jasmine.createSpy('batchCall', emptyBatchCall).and.callThrough();
      const batchCall = new BatchCall(batchCallSpy);

      batchCall.addKey('one');
      batchCall.addKey('two');
      batchCall.addKey('three');

      let result = await batchCall.promise();
      expect(result.size).toBe(0);

      expect(batchCallSpy).toHaveBeenCalledOnceWith(['one', 'two', 'three']);
    });

    it('propagates call errors', async () => {
      function throwingBatchCall(keys: string[]): JQuery.Promise<Map<string, string>> {
        throw new Error('NullPointerException');
      }

      const batchCallSpy = jasmine.createSpy('batchCall', throwingBatchCall).and.callThrough();
      const batchCall = new BatchCall(batchCallSpy);

      batchCall.addKey('one');
      batchCall.addKey('two');
      batchCall.addKey('three');

      try {
        await batchCall.promise();
        fail('Expected call to fail');
      } catch (error) {
        // expected
      }

      expect(batchCallSpy).toHaveBeenCalledOnceWith(['one', 'two', 'three']);
    });

    it('propagates promise errors', async () => {
      function throwingBatchCall(keys: string[]): JQuery.Promise<Map<string, string>> {
        return $.rejectedPromise('error');
      }

      const batchCallSpy = jasmine.createSpy('batchCall', throwingBatchCall).and.callThrough();
      const batchCall = new BatchCall(batchCallSpy);

      batchCall.addKey('one');
      batchCall.addKey('two');
      batchCall.addKey('three');

      try {
        await batchCall.promise();
        fail('Expected call to fail');
      } catch (error) {
        // expected
      }

      expect(batchCallSpy).toHaveBeenCalledOnceWith(['one', 'two', 'three']);
    });

    it('considers order of returned data', async () => {
      function sortedSpecBatchCall(keys: string[]): JQuery.Promise<Map<string, string>> {
        return $.resolvedPromise(new Map(keys.slice().sort().map(key => [key, `[${key}]`])));
      }

      const batchCallSpy = jasmine.createSpy('batchCall', sortedSpecBatchCall).and.callThrough();
      const batchCall = new BatchCall(batchCallSpy);

      batchCall.addKey('zero');
      batchCall.addKey('one');
      batchCall.addKey('two');
      batchCall.addKey('three');
      batchCall.addKey('four');

      let result = await batchCall.promise();
      expect(result.size).toBe(5);
      expect(result.get('four')).toBe('[four]');
      expect(result.get('one')).toBe('[one]');
      expect(result.get('three')).toBe('[three]');
      expect(result.get('two')).toBe('[two]');
      expect(result.get('zero')).toBe('[zero]');

      expect(batchCallSpy).toHaveBeenCalledOnceWith(['zero', 'one', 'two', 'three', 'four']);
    });

    it('preserves key instances', async () => {
      @typeName('scout.SpecKey')
      class SpecKey extends BaseDoEntity {
        type: string;
        nr: number;

        static of(type: string, nr: number) {
          return scout.create(SpecKey, {type, nr});
        }
      }

      function keySpecBatchCall(key: SpecKey[]): JQuery.Promise<Map<SpecKey, string>> {
        // simulate serialization over ajax call
        let serializedKeys = key.map(key => dataObjects.serialize(key));
        let deserializedKeys = serializedKeys.map(key => dataObjects.deserialize(key, SpecKey));
        return $.resolvedPromise(new Map(deserializedKeys.map(key => [key, `#${key.nr} (${key.type})`])));
      }

      const batchCallSpy = jasmine.createSpy('batchCall', keySpecBatchCall).and.callThrough();
      const batchCall = new BatchCall(batchCallSpy);

      let key1 = SpecKey.of('a', 1);
      let key2 = SpecKey.of('a', 1);
      let key3 = SpecKey.of('a', 2);
      let key4 = SpecKey.of('b', 1);

      batchCall.addKey(key1);
      batchCall.addKey(key2);
      batchCall.addKey(key3);
      batchCall.addKey(key4);
      batchCall.addKey(key4); // twice

      let result = await batchCall.promise();
      expect(result.size).toBe(4);
      expect(result.get(key1)).toBe('#1 (a)');
      expect(result.get(key2)).toBe('#1 (a)');
      expect(result.get(key1)).toBe(result.get(key2));
      expect(result.get(key3)).toBe('#2 (a)');
      expect(result.get(key4)).toBe('#1 (b)');

      expect(batchCallSpy).toHaveBeenCalledOnceWith([key1, key3, key4]);
    });
  });
});
