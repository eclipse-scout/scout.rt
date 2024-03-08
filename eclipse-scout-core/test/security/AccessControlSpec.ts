/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {AccessControl, Permission, PermissionCollection, PermissionCollectionType, scout} from '../../src/index';

describe('AccessControl', () => {

  beforeEach(() => {
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  class SpecAccessControl extends AccessControl {
    declare _permissionCollection: PermissionCollection;

    override _sync() {
      super._sync();
    }

    protected override _subscribeForNotifications() {
      // nop
    }

    protected override _unsubscribeFromNotifications() {
      // nop
    }
  }

  describe('_sync', () => {

    it('creates a PermissionCollection for the returned model', async () => {
      jasmine.clock().uninstall();
      const accessControl = scout.create(SpecAccessControl, {permissionsUrl: 'permissions'});
      expect(accessControl._permissionCollection).toBeNull();

      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(0), {
        status: 200,
        responseText: JSON.stringify({
          permissions: {
            some: [{
              objectType: 'Permission',
              id: 'some'
            }],
            other: [{
              objectType: 'Permission',
              id: 'other'
            }]
          }
        })
      });
      await accessControl.whenSync();

      expect(accessControl._permissionCollection).not.toBeNull();
      expect(accessControl._permissionCollection.type).toBe(PermissionCollectionType.DEFAULT);

      expect(accessControl.check(Permission.quick('some'), true)).toBeTrue();
      expect(accessControl.check(Permission.quick('other'), true)).toBeTrue();
      expect(accessControl.check(Permission.quick('test'), true)).toBeFalse();

      expect(await accessControl.check(Permission.quick('some'))).toBeTrue();
      expect(await accessControl.check(Permission.quick('other'))).toBeTrue();
      expect(await accessControl.check(Permission.quick('test'))).toBeFalse();
    });

    it('keeps last collection if request fails', () => {
      const accessControl = scout.create(SpecAccessControl, {permissionsUrl: 'permissions'});
      expect(accessControl._permissionCollection).toBeNull();

      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(0), {
        status: 200,
        responseText: JSON.stringify({
          permissions: {
            some: [{
              objectType: 'Permission',
              id: 'some'
            }],
            other: [{
              objectType: 'Permission',
              id: 'other'
            }]
          }
        })
      });
      jasmine.clock().tick(1);

      expect(accessControl._permissionCollection).not.toBeNull();
      expect(accessControl._permissionCollection.type).toBe(PermissionCollectionType.DEFAULT);

      expect(accessControl.check(Permission.quick('some'), true)).toBeTrue();
      expect(accessControl.check(Permission.quick('other'), true)).toBeTrue();
      expect(accessControl.check(Permission.quick('test'), true)).toBeFalse();

      accessControl.check(Permission.quick('some')).then(result => expect(result).toBeTrue());
      accessControl.check(Permission.quick('other')).then(result => expect(result).toBeTrue());
      accessControl.check(Permission.quick('test')).then(result => expect(result).toBeFalse());

      accessControl._sync();
      jasmine.clock().tick(1000);
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(1), {
        status: 500
      });
      jasmine.clock().tick(1);

      expect(accessControl._permissionCollection).not.toBeNull();
      expect(accessControl._permissionCollection.type).toBe(PermissionCollectionType.DEFAULT);

      expect(accessControl.check(Permission.quick('some'), true)).toBeTrue();
      expect(accessControl.check(Permission.quick('other'), true)).toBeTrue();
      expect(accessControl.check(Permission.quick('test'), true)).toBeFalse();

      accessControl.check(Permission.quick('some')).then(result => expect(result).toBeTrue());
      accessControl.check(Permission.quick('other')).then(result => expect(result).toBeTrue());
      accessControl.check(Permission.quick('test')).then(result => expect(result).toBeFalse());

      accessControl._sync();
      jasmine.clock().tick(1000);
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(2), {
        status: 200,
        responseText: JSON.stringify({
          type: 'ALL'
        })
      });
      jasmine.clock().tick(1);

      expect(accessControl._permissionCollection).not.toBeNull();
      expect(accessControl._permissionCollection.type).toBe(PermissionCollectionType.ALL);

      expect(accessControl.check(Permission.quick('some'), true)).toBeTrue();
      expect(accessControl.check(Permission.quick('other'), true)).toBeTrue();
      expect(accessControl.check(Permission.quick('test'), true)).toBeTrue();

      accessControl.check(Permission.quick('some')).then(result => expect(result).toBeTrue());
      accessControl.check(Permission.quick('other')).then(result => expect(result).toBeTrue());
      accessControl.check(Permission.quick('test')).then(result => expect(result).toBeTrue());
      jasmine.clock().tick(1);
    });

    it('creates NONE collection if the first request fails', () => {
      const accessControl = scout.create(SpecAccessControl, {permissionsUrl: 'permissions'});
      expect(accessControl._permissionCollection).toBeNull();

      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(0), {
        status: 500
      });
      jasmine.clock().tick(1);

      expect(accessControl._permissionCollection).not.toBeNull();
      expect(accessControl._permissionCollection.type).toBe(PermissionCollectionType.NONE);

      expect(accessControl.check(Permission.quick('some'), true)).toBeFalse();
      expect(accessControl.check(Permission.quick('other'), true)).toBeFalse();
      expect(accessControl.check(Permission.quick('test'), true)).toBeFalse();

      accessControl.check(Permission.quick('some')).then(result => expect(result).toBeFalse());
      accessControl.check(Permission.quick('other')).then(result => expect(result).toBeFalse());
      accessControl.check(Permission.quick('test')).then(result => expect(result).toBeFalse());
      jasmine.clock().tick(1);
    });

    it('triggers \'sync\' if successful', async () => {
      jasmine.clock().uninstall();
      const accessControl = scout.create(SpecAccessControl, {permissionsUrl: 'permissions'});
      expect(accessControl._permissionCollection).toBeNull();

      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(0), {
        status: 200,
        responseText: JSON.stringify({
          type: 'ALL'
        })
      });
      accessControl.whenSyncError().then(fail);
      await Promise.all([
        accessControl.whenSyncSuccess(),
        accessControl.whenSync()
      ]);

      expect(accessControl._permissionCollection).not.toBeNull();
    });

    it('does NOT triggers \'sync\' if unsuccessful', () => {
      const accessControl = scout.create(SpecAccessControl, {permissionsUrl: 'permissions'});
      expect(accessControl._permissionCollection).toBeNull();

      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(0), {
        status: 500
      });
      accessControl.whenSync().then(fail);
      accessControl.whenSyncSuccess().then(fail);
      jasmine.clock().tick(1);

      expect(accessControl._permissionCollection).not.toBeNull();
    });
  });
});
