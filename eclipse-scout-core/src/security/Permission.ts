/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {DoEntity, EnumObject, FullModelOf, InitModelOf, ObjectModel, ObjectOrModel, scout, SomeRequired, strings} from '../index';

export class Permission implements PermissionModel {
  declare self: Permission;
  declare model: PermissionModel;
  declare initModel: SomeRequired<this['model'], 'name'>;

  name: string;
  level: PermissionLevel;

  constructor() {
    this.name = null;
    this.level = Permission.Level.UNDEFINED;
  }

  static Level = {
    UNDEFINED: -1,
    NONE: 0,
    ALL: 100
  };

  init(model: InitModelOf<this>) {
    model = model || {} as InitModelOf<this>;
    this.name = scout.assertParameter('name', strings.nullIfEmpty(model.name));
    this.level = scout.nvl(model.level, this.level);
  }

  /**
   * Check if the given {@link Permission} is implied.
   * Quick implies is executed synchronously while non-quick implies is executed asynchronously.
   */
  implies(permission: Permission, quick: true): boolean;
  implies(permission: Permission, quick?: false): JQuery.Promise<boolean>;
  implies(permission: Permission, quick?: boolean): boolean | JQuery.Promise<boolean>;
  implies(permission: Permission, quick?: boolean): boolean | JQuery.Promise<boolean> {
    if (this.matches(permission) && this.level !== Permission.Level.NONE) {
      return quick ? this._evalPermissionQuick(permission) : this._evalPermission(permission);
    }
    return quick ? false : $.resolvedPromise(false);
  }

  matches(permission: Permission): boolean {
    return permission instanceof Permission && this.name === permission.name;
  }

  /**
   * Precondition: `matches(permission)`
   */
  protected _evalPermissionQuick(permission: Permission): boolean {
    return true;
  }

  /**
   * Precondition: `matches(permission)`
   */
  protected _evalPermission(permission: Permission): JQuery.Promise<boolean> {
    return $.resolvedPromise(this._evalPermissionQuick(permission));
  }

  /**
   * @returns a {@link Permission} with the given `name`.
   */
  static quick(name: string): Permission {
    if (!name) {
      return null;
    }
    return Permission.ensure({name});
  }

  /**
   * Ensures that the given `permission` is of type {@link Permission}. If a model is provided, a new {@link Permission} will be created.
   */
  static ensure<T extends Permission = Permission>(permission: ObjectOrModel<T>): T {
    if (!permission) {
      return permission as T;
    }
    if (permission instanceof Permission) {
      return permission;
    }
    // May return a specialized subclass of Permission
    if (!permission.objectType) {
      permission.objectType = Permission;
    }
    return scout.create(permission as FullModelOf<T>);
  }
}

export interface PermissionModel extends ObjectModel<Permission>, DoEntity {
  /**
   * The name and identifier of the {@link Permission}.
   */
  name?: string;
  /**
   * The level of the {@link Permission}.
   */
  level?: PermissionLevel;
}

export type PermissionLevel = EnumObject<typeof Permission.Level>;
