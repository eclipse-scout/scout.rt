/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {access, AccessControl, arrays, ObjectFactory, ObjectOrModel, Permission, PermissionCollection, PermissionCollectionModel, PermissionCollectionType} from '../../index';
import $ from 'jquery';

export const accessSpecHelper = {

  install(permissionCollectionModel?: PermissionCollectionModel): JQuery.Promise<any> {
    permissionCollectionModel = permissionCollectionModel || accessSpecHelper.allPermissionCollectionModel();
    ObjectFactory.get().register(AccessControl, () => new StaticAccessControl(permissionCollectionModel));
    return access.bootstrap('permissions');
  },

  uninstall() {
    ObjectFactory.get().register(AccessControl, () => new AccessControl());
    access.tearDown();
  },

  permissionCollectionModel(...permissions: ObjectOrModel<Permission>[]): PermissionCollectionModel {
    const permissionMap = new Map();
    arrays.ensure(permissions)
      .forEach(p => {
        p = Permission.ensure(p);
        permissionMap.set(p.id, [...arrays.ensure(permissionMap.get(p.id)), p]);
      });

    return {
      permissions: permissionMap,
      type: PermissionCollectionType.DEFAULT
    };
  },

  allPermissionCollectionModel(): PermissionCollectionModel {
    return {type: PermissionCollectionType.ALL};
  },

  nonePermissionCollectionModel(): PermissionCollectionModel {
    return {type: PermissionCollectionType.NONE};
  }
};

class StaticAccessControl extends AccessControl {

  constructor(permissionCollectionModel: PermissionCollectionModel) {
    super();

    this._permissionCollection = PermissionCollection.ensure(permissionCollectionModel);
  }

  protected override _sync() {
    // nop
  }

  override whenSync(): JQuery.Promise<void> {
    return $.resolvedPromise();
  }
}
