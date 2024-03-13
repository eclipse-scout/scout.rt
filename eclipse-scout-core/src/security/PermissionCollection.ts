/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {FullModelOf, InitModelOf, ObjectModel, ObjectOrModel, Permission, PermissionLevel, PropertyChangeEvent, PropertyEventEmitter, PropertyEventMap, scout} from '../index';

export class PermissionCollection extends PropertyEventEmitter implements PermissionCollectionModel {
  declare self: PermissionCollection;
  declare model: PermissionCollectionModel;
  declare initModel: PermissionCollectionModel;
  declare eventMap: PermissionCollectionEventMap;

  permissions: PermissionMap;
  type: PermissionCollectionType;

  constructor() {
    super();
    this.permissions = new Map();
    this.type = PermissionCollectionType.DEFAULT;
  }

  override init(model: InitModelOf<this>) {
    model = model || {} as InitModelOf<this>;
    this.permissions = scout.nvl(model.permissions, this.permissions);
    this.type = scout.nvl(model.type, this.type);

    this._setPermissions(this.permissions);
    this._setType(this.type);
  }

  setPermissions(permissions: PermissionModelMapModel) {
    this.setProperty('permissions', permissions);
  }

  protected _setPermissions(permissions: PermissionModelMapModel) {
    permissions = PermissionCollection._ensurePermissionMap(permissions);
    this._setProperty('permissions', permissions);
  }

  setType(type: PermissionCollectionType) {
    this.setProperty('type', type);
  }

  protected _setType(type: PermissionCollectionType) {
    if (!Object.values(PermissionCollectionType).includes(type)) {
      type = PermissionCollectionType.DEFAULT;
    }
    this._setProperty('type', type);
  }

  /**
   * Check if the given {@link Permission} is implied (see {@link PermissionCollectionType}).
   * Quick implies is executed synchronously while non-quick implies is executed asynchronously.
   */
  implies(permission: Permission, quick: true): boolean;
  implies(permission: Permission, quick?: false): JQuery.Promise<boolean>;
  implies(permission: Permission, quick?: boolean): boolean | JQuery.Promise<boolean>;
  implies(permission: Permission, quick?: boolean): boolean | JQuery.Promise<boolean> {
    if (!permission) {
      return quick ? false : $.resolvedPromise(false);
    }
    switch (this.type) {
      case PermissionCollectionType.DEFAULT: {
        const permissions = this.permissions.get(permission.id);
        if (!permissions || !permissions.size) {
          return quick ? false : $.resolvedPromise(false);
        }

        if (quick) {
          // check if at least one of the permissions implies permission
          for (const p of permissions) {
            if (p.implies(permission, quick)) {
              return true;
            }
          }
          return false;
        }

        const deferred = $.Deferred();
        // collect all promises
        const impliedPromises: JQuery.Promise<void>[] = [];
        for (const p of permissions) {
          impliedPromises.push(p.implies(permission, false)
            .then(implies => {
              if (implies) {
                // resolve with true if first one implies
                deferred.resolve(true);
              }
            }));
        }

        // resolve with false if not already resolved (i.e. not implied by any permission)
        $.promiseAll(impliedPromises).then(values => deferred.resolve(false));

        return deferred.promise();
      }
      case PermissionCollectionType.ALL:
        return quick ? true : $.resolvedPromise(true);
      case PermissionCollectionType.NONE:
        return quick ? false : $.resolvedPromise(false);
    }
  }

  /**
   * Returns the granted {@link PermissionLevel} for a given permission instance `permission`.
   * - {@link Permission.Level.UNDEFINED} if `permission` is `null` or in general 'not an {@link Permission}'
   * - {@link Permission.Level.NONE} if no level at all is granted to `permission`
   * - {@link PermissionLevel} if the level can be determined exactly.
   * - {@link Permission.Level.UNDEFINED} if there are multiple granted permission levels possible and there is not enough data in the `permission` contained to determine the result closer.
   */
  getGrantedPermissionLevel(permission: Permission): PermissionLevel {
    if (!permission || !(permission instanceof Permission)) {
      return Permission.Level.UNDEFINED;
    }
    switch (this.type) {
      case PermissionCollectionType.DEFAULT: {
        const permissions = this.permissions.get(permission.id);
        if (!permissions || !permissions.size) {
          return Permission.Level.NONE;
        }

        const grantedLevels = new Set<PermissionLevel>();
        for (const p of permissions) {
          if (p.matches(permission)) {
            grantedLevels.add(p.level);
          }
        }

        switch (grantedLevels.size) {
          case 0:
            return Permission.Level.NONE;
          case 1:
            return grantedLevels.values().next().value;
          default:
            return Permission.Level.UNDEFINED;
        }
      }
      case PermissionCollectionType.ALL:
        return Permission.Level.ALL;
      case PermissionCollectionType.NONE:
        return Permission.Level.NONE;
    }
  }

  /**
   * Ensures that the given `permissionCollection` is of type {@link PermissionCollection}. If a model is provided, a new {@link PermissionCollection} will be created.
   */
  static ensure<T extends PermissionCollection = PermissionCollection>(permissionCollection: ObjectOrModel<T>): T {
    if (!permissionCollection) {
      return permissionCollection as T;
    }
    if (permissionCollection instanceof PermissionCollection) {
      return permissionCollection;
    }
    // May return a specialized subclass of PermissionCollection
    if (!permissionCollection.objectType) {
      permissionCollection.objectType = PermissionCollection;
    }
    return scout.create(permissionCollection as FullModelOf<T>);
  }

  /**
   * Ensures that the given `permissionModelMapModel` is of type {@link PermissionMap}.
   *
   * @param permissionModelMapModel map/object of {@link Permission}s or their model grouped by `id`.
   */
  protected static _ensurePermissionMap(permissionModelMapModel: PermissionModelMapModel): PermissionMap {
    // permissionModelMapModel is a map or an object
    if ($.isPlainObject(permissionModelMapModel)) {
      // permissionModelMapModel is an object, create a map containing the entries of the object
      permissionModelMapModel = new Map(Object.entries(permissionModelMapModel));
    }
    if (!(permissionModelMapModel instanceof Map)) {
      return new Map();
    }

    const permissionMap: PermissionMap = new Map();
    // permissionModelMapModel is a map and its values are sets or arrays of Permission's or their model
    for (const [name, modelSetOrArray] of permissionModelMapModel) {
      if (!name || !modelSetOrArray) {
        continue;
      }
      if (Array.isArray(modelSetOrArray) && !modelSetOrArray.length) {
        continue;
      }
      if (modelSetOrArray instanceof Set && !modelSetOrArray.size) {
        continue;
      }
      // transform set or array of Permission's or their model into a set of Permission's
      const permissionSet: Set<Permission> = new Set();
      for (const model of modelSetOrArray) {
        // ensure that element is a Permission
        const permission = Permission.ensure(model);
        if (permission) {
          permissionSet.add(permission);
        }
      }
      // only add if set has elements
      if (permissionSet.size) {
        permissionMap.set(name, permissionSet);
      }
    }
    return permissionMap;
  }
}

export interface PermissionCollectionModel extends ObjectModel<PermissionCollection> {
  /**
   * {@link Permission}s grouped by `id`.
   */
  permissions?: PermissionModelMapModel;
  /**
   * The type of the {@link PermissionCollection} (see {@link PermissionCollectionType}).
   */
  type?: PermissionCollectionType;
}

export type PermissionMap = Map<string, Set<Permission>>;
export type PermissionModelMapModel = Map<string, Set<ObjectOrModel<Permission>> | ObjectOrModel<Permission>[]> | Record<string, Set<ObjectOrModel<Permission>> | ObjectOrModel<Permission>[]>;

export interface PermissionCollectionEventMap extends PropertyEventMap {
  'propertyChange:permissions': PropertyChangeEvent<PermissionMap>;
  'propertyChange:type': PropertyChangeEvent<PermissionCollectionType>;
}

export enum PermissionCollectionType {
  /**
   * Check if {@link Permission} is contained {@link PermissionCollection}.
   */
  DEFAULT = 'DEFAULT',
  /**
   * All {@link Permission}s are implied.
   */
  ALL = 'ALL',
  /**
   * No {@link Permission} is implied.
   */
  NONE = 'NONE'
}
