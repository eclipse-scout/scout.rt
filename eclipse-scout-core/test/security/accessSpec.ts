/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {access, Permission} from '../../src/index';
import {accessSpecHelper} from '../../src/testing/index';

describe('access', () => {
  let same: Permission,
    name: Permission;

  beforeAll(async () => {
    same = Permission.ensure({objectType: SamePermission, name: 'same'});
    name = Permission.quick('name');
    await accessSpecHelper.install(accessSpecHelper.permissionCollectionModel(same, name));
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
      expect(access.quickCheck(Permission.ensure({objectType: SamePermission, name: 'same'}))).toBeFalse();
      expect(access.quickCheck(same)).toBeTrue();

      expect(access.quickCheck('name')).toBeTrue();
      expect(access.quickCheck(Permission.quick('name'))).toBeTrue();
      expect(access.quickCheck(Permission.ensure({objectType: SamePermission, name: 'name'}))).toBeTrue();
      expect(access.quickCheck(name)).toBeTrue();

      expect(access.quickCheck('other')).toBeFalse();
      expect(access.quickCheck(Permission.quick('other'))).toBeFalse();
      expect(access.quickCheck(Permission.ensure({objectType: SamePermission, name: 'other'}))).toBeFalse();
    });
  });

  describe('check', () => {

    it('checks asynchronously against granted permissions', async () => {
      expect(await access.check('same')).toBeFalse();
      expect(await access.check(Permission.quick('same'))).toBeFalse();
      expect(await access.check(Permission.ensure({objectType: SamePermission, name: 'same'}))).toBeFalse();
      expect(await access.check(same)).toBeTrue();

      expect(await access.check('name')).toBeTrue();
      expect(await access.check(Permission.quick('name'))).toBeTrue();
      expect(await access.check(Permission.ensure({objectType: SamePermission, name: 'name'}))).toBeTrue();
      expect(await access.check(name)).toBeTrue();

      expect(await access.check('other')).toBeFalse();
      expect(await access.check(Permission.quick('other'))).toBeFalse();
      expect(await access.check(Permission.ensure({objectType: SamePermission, name: 'other'}))).toBeFalse();
    });
  });
});
