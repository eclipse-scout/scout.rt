/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {access, Permission, scout} from '../../src/index';
import {accessSpecHelper} from '../../src/testing/index';

describe('access', () => {
  let same: Permission,
    name: Permission,
    none: Permission;

  beforeAll(async () => {
    same = Permission.ensure({objectType: SamePermission, id: 'same', level: Permission.Level.ALL});
    name = Permission.quick('name');
    none = scout.create(Permission, {id: 'none', level: Permission.Level.NONE});
    await accessSpecHelper.install(accessSpecHelper.permissionCollectionModel(same, name, none));
  });

  afterAll(() => {
    accessSpecHelper.uninstall();
  });

  class SamePermission extends Permission {
    protected override _evalPermissionQuick(permission: Permission): boolean {
      return this === permission;
    }
  }

  describe('quickCheck', () => {

    it('checks synchronously against granted permissions', () => {
      expect(access.quickCheck('same')).toBeFalse();
      expect(access.quickCheck(Permission.quick('same'))).toBeFalse();
      expect(access.quickCheck(Permission.ensure({objectType: SamePermission, id: 'same'}))).toBeFalse();
      expect(access.quickCheck(same)).toBeTrue();

      expect(access.quickCheck('name')).toBeTrue();
      expect(access.quickCheck(Permission.quick('name'))).toBeTrue();
      expect(access.quickCheck(Permission.ensure({objectType: SamePermission, id: 'name'}))).toBeTrue();
      expect(access.quickCheck(name)).toBeTrue();

      expect(access.quickCheck('none')).toBeFalse();
      expect(access.quickCheck(Permission.quick('none'))).toBeFalse();
      expect(access.quickCheck(Permission.ensure({objectType: SamePermission, id: 'none'}))).toBeFalse();
      expect(access.quickCheck(none)).toBeFalse();

      expect(access.quickCheck('other')).toBeFalse();
      expect(access.quickCheck(Permission.quick('other'))).toBeFalse();
      expect(access.quickCheck(Permission.ensure({objectType: SamePermission, id: 'other'}))).toBeFalse();
    });
  });

  describe('check', () => {

    it('checks asynchronously against granted permissions', async () => {
      expect(await access.check('same')).toBeFalse();
      expect(await access.check(Permission.quick('same'))).toBeFalse();
      expect(await access.check(Permission.ensure({objectType: SamePermission, id: 'same'}))).toBeFalse();
      expect(await access.check(same)).toBeTrue();

      expect(await access.check('name')).toBeTrue();
      expect(await access.check(Permission.quick('name'))).toBeTrue();
      expect(await access.check(Permission.ensure({objectType: SamePermission, id: 'name'}))).toBeTrue();
      expect(await access.check(name)).toBeTrue();

      expect(await access.check('none')).toBeFalse();
      expect(await access.check(Permission.quick('none'))).toBeFalse();
      expect(await access.check(Permission.ensure({objectType: SamePermission, id: 'none'}))).toBeFalse();
      expect(await access.check(none)).toBeFalse();

      expect(await access.check('other')).toBeFalse();
      expect(await access.check(Permission.quick('other'))).toBeFalse();
      expect(await access.check(Permission.ensure({objectType: SamePermission, id: 'other'}))).toBeFalse();
    });
  });

  describe('getGrantedPermissionLevel', () => {

    it('gets the correct permission level', () => {
      expect(access.getGrantedPermissionLevel(null)).toBe(Permission.Level.UNDEFINED);
      expect(access.getGrantedPermissionLevel('same')).toBe(Permission.Level.ALL);
      expect(access.getGrantedPermissionLevel('name')).toBe(Permission.Level.UNDEFINED);
      expect(access.getGrantedPermissionLevel('none')).toBe(Permission.Level.NONE);
      expect(access.getGrantedPermissionLevel('other')).toBe(Permission.Level.NONE);
    });
  });
});
