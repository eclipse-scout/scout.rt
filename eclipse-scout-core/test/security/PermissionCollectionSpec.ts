/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Deferred, Permission, PermissionCollection, PermissionCollectionType, scout} from '../../src/index';
import {accessSpecHelper} from '../../src/testing';

describe('PermissionCollection', () => {

  class PermissionCollectionEx extends PermissionCollection {
  }

  class SpecPermission extends Permission {
    override _evalPermission(permission: Permission): Promise<boolean> {
      return super._evalPermission(permission);
    }
  }

  /**
   * Native promises don't expose a synchronous state() like a JQuery.Promise, so track it ourselves.
   */
  function trackState(promise: Promise<any>): { value: string } {
    let state = {value: 'pending'};
    promise.then(() => {
      state.value = 'resolved';
    }, () => {
      state.value = 'rejected';
    });
    return state;
  }

  /**
   * Waits a real macrotask tick so that any number of chained microtask reactions (promise then-chains)
   * have fully drained, without depending on jasmine's fake clock.
   */
  function flush(): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, 0));
  }

  describe('implies', () => {

    beforeEach(() => {
      jasmine.clock().install();
    });

    afterEach(() => {
      jasmine.clock().uninstall();
    });

    it('all permissions if type is ALL', async () => {
      jasmine.clock().uninstall();
      const collection = scout.create(PermissionCollection, {type: PermissionCollectionType.ALL});

      expect(collection.implies(null, true)).toBeFalse();
      expect(collection.implies(Permission.quick('some'), true)).toBeTrue();
      expect(collection.implies(Permission.quick('other'), true)).toBeTrue();

      expect(await collection.implies(null)).toBeFalse();
      expect(await collection.implies(Permission.quick('some'))).toBeTrue();
      expect(await collection.implies(Permission.quick('other'))).toBeTrue();
    });

    it('no permission if type is NONE', async () => {
      jasmine.clock().uninstall();
      const collection = scout.create(PermissionCollection, {type: PermissionCollectionType.NONE});

      expect(collection.implies(null, true)).toBeFalse();
      expect(collection.implies(Permission.quick('some'), true)).toBeFalse();
      expect(collection.implies(Permission.quick('other'), true)).toBeFalse();

      expect(await collection.implies(null)).toBeFalse();
      expect(await collection.implies(Permission.quick('some'))).toBeFalse();
      expect(await collection.implies(Permission.quick('other'))).toBeFalse();
    });

    it('only permission contained in the collection if type is DEFAULT', async () => {
      jasmine.clock().uninstall();
      const collection = scout.create(PermissionCollection, {
        permissions: {
          some: [Permission.quick('some')]
        }
      });

      expect(collection.implies(null, true)).toBeFalse();
      expect(collection.implies(Permission.quick('some'), true)).toBeTrue();
      expect(collection.implies(Permission.quick('other'), true)).toBeFalse();

      expect(await collection.implies(null)).toBeFalse();
      expect(await collection.implies(Permission.quick('some'))).toBeTrue();
      expect(await collection.implies(Permission.quick('other'))).toBeFalse();
    });

    it('resolves if first check is succeeds', async () => {
      // this test drives native promises via real microtask/macrotask waits, jasmine's fake clock is not needed
      jasmine.clock().uninstall();

      let evalDeferred1: Deferred<boolean>;
      let evalDeferred2: Deferred<boolean>;
      const permission1 = scout.create(SpecPermission, {id: 'test'});
      const permission2 = scout.create(SpecPermission, {id: 'test'});
      const collection = scout.create(PermissionCollection, accessSpecHelper.permissionCollectionModel(permission1, permission2));

      permission1._evalPermission = (permission: Permission) => {
        evalDeferred1 = new Deferred();
        return evalDeferred1.promise();
      };
      permission2._evalPermission = (permission: Permission) => {
        evalDeferred2 = new Deferred();
        return evalDeferred2.promise();
      };

      let state = trackState(collection.implies(Permission.quick('test')).then(implies => expect(implies).toBeTrue()));
      expect(state.value).toBe('pending');
      evalDeferred1.resolve(true);
      await flush();
      expect(state.value).toBe('resolved');

      state = trackState(collection.implies(Permission.quick('test')).then(implies => expect(implies).toBeTrue()));
      expect(state.value).toBe('pending');
      evalDeferred2.resolve(false);
      await flush();
      expect(state.value).toBe('pending');
      evalDeferred1.resolve(true);
      await flush();
      expect(state.value).toBe('resolved');

      state = trackState(collection.implies(Permission.quick('test')).then(implies => expect(implies).toBeTrue()));
      expect(state.value).toBe('pending');
      evalDeferred2.resolve(true);
      await flush();
      expect(state.value).toBe('resolved');

      state = trackState(collection.implies(Permission.quick('test')).then(implies => expect(implies).toBeFalse()));
      expect(state.value).toBe('pending');
      evalDeferred2.resolve(false);
      await flush();
      expect(state.value).toBe('pending');
      evalDeferred1.resolve(false);
      await flush();
      expect(state.value).toBe('resolved');
    });
  });

  describe('getGrantedPermissionLevel', () => {

    it('is Permission.Level.ALL if type is ALL', () => {
      const collection = scout.create(PermissionCollection, {type: PermissionCollectionType.ALL});

      expect(collection.getGrantedPermissionLevel(null)).toBe(Permission.Level.UNDEFINED);
      expect(collection.getGrantedPermissionLevel(Permission.quick('some'))).toBe(Permission.Level.ALL);
      expect(collection.getGrantedPermissionLevel(Permission.quick('other'))).toBe(Permission.Level.ALL);
    });

    it('is Permission.Level.NONE if type is NONE', () => {
      const collection = scout.create(PermissionCollection, {type: PermissionCollectionType.NONE});

      expect(collection.getGrantedPermissionLevel(null)).toBe(Permission.Level.UNDEFINED);
      expect(collection.getGrantedPermissionLevel(Permission.quick('some'))).toBe(Permission.Level.NONE);
      expect(collection.getGrantedPermissionLevel(Permission.quick('other'))).toBe(Permission.Level.NONE);
    });

    it('gets the correct permission level if type is DEFAULT', () => {
      const collection = scout.create(PermissionCollection, {
        permissions: {
          undef: [scout.create(Permission, {id: 'undef', level: Permission.Level.UNDEFINED})],
          none: [scout.create(Permission, {id: 'none', level: Permission.Level.NONE})],
          all: [scout.create(Permission, {id: 'all', level: Permission.Level.ALL})],
          multiple: [scout.create(Permission, {id: 'multiple', level: Permission.Level.NONE}), scout.create(Permission, {id: 'multiple', level: Permission.Level.ALL})],
          multipleSame: [scout.create(Permission, {id: 'multipleSame', level: Permission.Level.ALL}), scout.create(Permission, {id: 'multipleSame', level: Permission.Level.ALL})]
        }
      });

      expect(collection.getGrantedPermissionLevel(null)).toBe(Permission.Level.UNDEFINED);
      expect(collection.getGrantedPermissionLevel(Permission.quick('undef'))).toBe(Permission.Level.UNDEFINED);
      expect(collection.getGrantedPermissionLevel(Permission.quick('none'))).toBe(Permission.Level.NONE);
      expect(collection.getGrantedPermissionLevel(Permission.quick('all'))).toBe(Permission.Level.ALL);
      expect(collection.getGrantedPermissionLevel(Permission.quick('multiple'))).toBe(Permission.Level.UNDEFINED);
      expect(collection.getGrantedPermissionLevel(Permission.quick('multipleSame'))).toBe(Permission.Level.ALL);
      expect(collection.getGrantedPermissionLevel(Permission.quick('other'))).toBe(Permission.Level.NONE);
    });
  });

  describe('permissions', () => {

    it('is always of type PermissionMap', () => {
      const collection = scout.create(PermissionCollection);

      const permissionsObject = {
        array: [Permission.quick('array')],
        arrayModel: [{id: 'arrayModel'}],
        arrayEmpty: []
      };
      const permissionsMap = new Map(Object.entries({
        set: new Set([Permission.quick('set')]),
        setModel: new Set([{id: 'setModel'}]),
        setEmpty: new Set()
      }));

      expect(collection.permissions).toBeInstanceOf(Map);
      expect(collection.permissions.size).toBe(0);

      collection.setPermissions(permissionsObject);
      expect(collection.permissions).toBeInstanceOf(Map);
      expect(collection.permissions.size).toBe(2);
      expect(collection.permissions.has('array')).toBeTrue();
      expect(collection.permissions.get('array')).toBeInstanceOf(Set);
      expect(collection.permissions.get('array').size).toBe(1);
      expect([...collection.permissions.get('array')][0]).toBeInstanceOf(Permission);
      expect(collection.permissions.has('arrayModel')).toBeTrue();
      expect(collection.permissions.get('arrayModel')).toBeInstanceOf(Set);
      expect(collection.permissions.get('arrayModel').size).toBe(1);
      expect([...collection.permissions.get('arrayModel')][0]).toBeInstanceOf(Permission);
      expect(collection.permissions.has('arrayEmpty')).toBeFalse();

      collection.setPermissions(permissionsMap);
      expect(collection.permissions).toBeInstanceOf(Map);
      expect(collection.permissions.size).toBe(2);
      expect(collection.permissions.has('set')).toBeTrue();
      expect(collection.permissions.get('set')).toBeInstanceOf(Set);
      expect(collection.permissions.get('set').size).toBe(1);
      expect([...collection.permissions.get('set')][0]).toBeInstanceOf(Permission);
      expect(collection.permissions.has('setModel')).toBeTrue();
      expect(collection.permissions.get('setModel')).toBeInstanceOf(Set);
      expect(collection.permissions.get('setModel').size).toBe(1);
      expect([...collection.permissions.get('setModel')][0]).toBeInstanceOf(Permission);
      expect(collection.permissions.has('setEmpty')).toBeFalse();
    });
  });

  describe('ensure', () => {

    it('creates a permission collection if a model is provided', () => {
      const collection = scout.create(PermissionCollection, {type: PermissionCollectionType.ALL});

      const ensureCollection = PermissionCollection.ensure(collection);
      expect(ensureCollection).toBe(collection);
      expect(ensureCollection).toBeInstanceOf(PermissionCollection);
      expect(ensureCollection).not.toBeInstanceOf(PermissionCollectionEx);

      const ensureModel = PermissionCollection.ensure({type: PermissionCollectionType.ALL});
      expect(ensureModel).not.toBe(collection);
      expect(ensureModel.type).toBe(PermissionCollectionType.ALL);
      expect(ensureModel).toBeInstanceOf(PermissionCollection);
      expect(ensureModel).not.toBeInstanceOf(PermissionCollectionEx);

      const ensureSpecModel = PermissionCollection.ensure({objectType: PermissionCollectionEx, type: PermissionCollectionType.ALL});
      expect(ensureSpecModel).not.toBe(collection);
      expect(ensureSpecModel.type).toBe(PermissionCollectionType.ALL);
      expect(ensureSpecModel).toBeInstanceOf(PermissionCollection);
      expect(ensureSpecModel).toBeInstanceOf(PermissionCollectionEx);
    });
  });
});
