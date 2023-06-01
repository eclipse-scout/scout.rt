/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AccessControl, Permission, scout} from '../index';
import $ from 'jquery';

let accessControl: AccessControl = null;

export const access = {

  bootstrap(permissionsUrl: string): JQuery.Promise<any> {
    if (!permissionsUrl) {
      return $.resolvedPromise();
    }
    if (!accessControl) {
      accessControl = scout.create(AccessControl, {permissionsUrl});
    }
    return accessControl.whenSync();
  },

  tearDown() {
    if (!accessControl) {
      return;
    }
    accessControl.stopSync();
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
