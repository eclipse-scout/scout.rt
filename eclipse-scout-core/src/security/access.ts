/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AccessControl, Permission, PermissionLevel, scout, systems} from '../index';
import $ from 'jquery';

let accessControl: AccessControl = null;

export const access = {

  /**
   * Loads all permissions from the given url to a client cache.
   * @param permissionsUrl The url to fetch the permissions from.
   */
  bootstrap(permissionsUrl: string): JQuery.Promise<any> {
    if (!permissionsUrl) {
      return $.resolvedPromise();
    }
    if (!accessControl) {
      accessControl = scout.create(AccessControl, {permissionsUrl});
    }
    return accessControl.bootstrap();
  },

  /**
   * Loads permissions from the main system using the url configuration of that {@link System}.
   */
  bootstrapSystem(): JQuery.Promise<void> {
    const url = systems.getOrCreate().getEndpointUrl('permissions', 'permissions');
    return access.bootstrap(url);
  },

  tearDown() {
    if (!accessControl) {
      return;
    }
    accessControl.destroy();
    accessControl = null;
  },

  /**
   * Quick check (only local checks) `permission` against granted permissions of the current user.
   */
  quickCheck(permission: string | Permission): boolean {
    return check(permission, true);
  },

  /**
   * Check `permission` against granted permissions of the current user.
   */
  check(permission: string | Permission): JQuery.Promise<boolean> {
    return check(permission);
  },

  /**
   * Returns the granted {@link PermissionLevel} for a given permission instance `permission`.
   * - {@link Permission.Level.UNDEFINED} if `permission` is `null` or in general 'not an {@link Permission}'
   * - {@link Permission.Level.NONE} if no level at all is granted to `permission`
   * - {@link PermissionLevel} if the level can be determined exactly.
   * - {@link Permission.Level.UNDEFINED} if there are multiple granted permission levels possible and there is not enough data in the `permission` contained to determine the result closer.
   */
  getGrantedPermissionLevel(permission: string | Permission): PermissionLevel {
    if (!accessControl) {
      return Permission.Level.UNDEFINED;
    }
    if (!(permission instanceof Permission)) {
      permission = Permission.quick(permission);
    }
    return accessControl.getGrantedPermissionLevel(permission);
  },

  getPermissionsUrl(): string {
    return accessControl && accessControl.permissionsUrl;
  }
};

function check(permission: string | Permission, quick: true): boolean;
function check(permission: string | Permission, quick?: false): JQuery.Promise<boolean>;
function check(permission: string | Permission, quick?: boolean): boolean | JQuery.Promise<boolean> {
  if (!accessControl) {
    return quick ? false : $.resolvedPromise(false);
  }
  if (!(permission instanceof Permission)) {
    permission = Permission.quick(permission);
  }
  return accessControl.check(permission, quick);
}
