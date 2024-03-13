/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Permission, scout} from '../../src/index';

describe('Permission', () => {

  class SpecPermission extends Permission {
    override _evalPermissionQuick(permission: Permission): boolean {
      return super._evalPermissionQuick(permission);
    }
  }

  describe('init', () => {

    it('asserts that name is set', () => {
      const permission = Permission.quick('some');

      expect(permission.id).toBe('some');

      permission.init({id: 'other'});
      expect(permission.id).toBe('other');

      expect(() => permission.init(null)).toThrowError('Missing required parameter \'id\'');
      expect(() => permission.init({} as any)).toThrowError('Missing required parameter \'id\'');
      expect(() => permission.init({id: null})).toThrowError('Missing required parameter \'id\'');
      expect(() => permission.init({id: ''})).toThrowError('Missing required parameter \'id\'');
      expect(() => permission.init({test: 'some'} as any)).toThrowError('Missing required parameter \'id\'');
    });
  });

  describe('matches', () => {

    it('by name and class', () => {
      const some1 = Permission.quick('some');
      const some2 = scout.create(Permission, {id: 'some'});
      const someObj = {id: 'some'} as Permission;
      const other = Permission.quick('other');

      expect(some1.matches(some1)).toBeTrue();
      expect(some1.matches(some2)).toBeTrue();
      expect(some1.matches(someObj)).toBeFalse();
      expect(some1.matches(other)).toBeFalse();

      expect(some2.matches(some1)).toBeTrue();
      expect(some2.matches(some2)).toBeTrue();
      expect(some2.matches(someObj)).toBeFalse();
      expect(some2.matches(other)).toBeFalse();

      expect(other.matches(some1)).toBeFalse();
      expect(other.matches(some2)).toBeFalse();
      expect(other.matches(someObj)).toBeFalse();
      expect(other.matches(other)).toBeTrue();
    });
  });

  describe('implies', () => {

    it('checks if permission matches', async () => {
      const some1 = Permission.quick('some');
      const some2 = Permission.quick('some');
      const other = Permission.quick('other');

      expect(some1.implies(some1, true)).toBeTrue();
      expect(some1.implies(some2, true)).toBeTrue();
      expect(some1.implies(other, true)).toBeFalse();

      expect(await some1.implies(some1)).toBeTrue();
      expect(await some1.implies(some2)).toBeTrue();
      expect(await some1.implies(other)).toBeFalse();

      expect(some2.implies(some1, true)).toBeTrue();
      expect(some2.implies(some2, true)).toBeTrue();
      expect(some2.implies(other, true)).toBeFalse();

      expect(await some2.implies(some1)).toBeTrue();
      expect(await some2.implies(some2)).toBeTrue();
      expect(await some2.implies(other)).toBeFalse();

      expect(other.implies(some1, true)).toBeFalse();
      expect(other.implies(some2, true)).toBeFalse();
      expect(other.implies(other, true)).toBeTrue();

      expect(await other.implies(some1)).toBeFalse();
      expect(await other.implies(some2)).toBeFalse();
      expect(await other.implies(other)).toBeTrue();

      some2.matches = p => p === other;

      expect(some2.implies(some1, true)).toBeFalse();
      expect(some2.implies(some2, true)).toBeFalse();
      expect(some2.implies(other, true)).toBeTrue();

      expect(await some2.implies(some1)).toBeFalse();
      expect(await some2.implies(some2)).toBeFalse();
      expect(await some2.implies(other)).toBeTrue();
    });

    it('evaluates permission', async () => {
      const some1 = Permission.quick('some');
      const some2 = scout.create(SpecPermission, {id: 'some'});

      expect(some1.implies(some1, true)).toBeTrue();
      expect(some1.implies(some2, true)).toBeTrue();

      expect(await some1.implies(some1)).toBeTrue();
      expect(await some1.implies(some2)).toBeTrue();

      expect(some2.implies(some1, true)).toBeTrue();
      expect(some2.implies(some2, true)).toBeTrue();

      expect(await some2.implies(some1)).toBeTrue();
      expect(await some2.implies(some2)).toBeTrue();

      some2._evalPermissionQuick = p => false;

      expect(some2.implies(some1, true)).toBeFalse();
      expect(some2.implies(some2, true)).toBeFalse();

      expect(await some2.implies(some1)).toBeFalse();
      expect(await some2.implies(some2)).toBeFalse();

      some2._evalPermissionQuick = p => p === some1;

      expect(some2.implies(some1, true)).toBeTrue();
      expect(some2.implies(some2, true)).toBeFalse();

      expect(await some2.implies(some1)).toBeTrue();
      expect(await some2.implies(some2)).toBeFalse();
    });
  });

  describe('ensure', () => {

    it('creates a permission if a model is provided', () => {
      const some = Permission.quick('some');

      const ensurePermission = Permission.ensure(some);
      expect(ensurePermission).toBe(some);
      expect(ensurePermission).toBeInstanceOf(Permission);
      expect(ensurePermission).not.toBeInstanceOf(SpecPermission);

      const ensureModel = Permission.ensure({id: 'some'});
      expect(ensureModel).not.toBe(some);
      expect(ensureModel.id).toBe('some');
      expect(ensureModel).toBeInstanceOf(Permission);
      expect(ensureModel).not.toBeInstanceOf(SpecPermission);

      const ensureSpecModel = Permission.ensure({objectType: SpecPermission, id: 'some'});
      expect(ensureSpecModel).not.toBe(some);
      expect(ensureSpecModel.id).toBe('some');
      expect(ensureSpecModel).toBeInstanceOf(Permission);
      expect(ensureSpecModel).toBeInstanceOf(SpecPermission);
    });
  });
});
