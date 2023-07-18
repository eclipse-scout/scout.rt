/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {
  ajax, AjaxError, ErrorHandler, Event, InitModelOf, ObjectModel, Permission, PermissionCollection, PermissionCollectionModel, PermissionCollectionType, PermissionLevel, PropertyChangeEvent, PropertyEventEmitter, PropertyEventMap, scout,
  SomeRequired
} from '../index';
import $ from 'jquery';

export class AccessControl extends PropertyEventEmitter implements AccessControlModel {
  declare self: AccessControl;
  declare model: AccessControlModel;
  declare initModel: SomeRequired<this['model'], 'permissionsUrl'>;
  declare eventMap: AccessControlEventMap;

  permissionsUrl: string;
  interval: number;

  protected _permissionCollection: PermissionCollection;
  protected _retryIntervals: number[];
  protected _syncTimeoutId: number;

  constructor() {
    super();
    this.permissionsUrl = null;
    this.interval = 1800000; // 30 * 60 * 1000 (30 minutes)

    this._permissionCollection = null;
    this._retryIntervals = [];
    this._syncTimeoutId = null;
  }

  override init(model: InitModelOf<this>) {
    this.permissionsUrl = scout.assertParameter('permissionsUrl', model.permissionsUrl);
    this._setInterval(scout.nvl(model.interval, this.interval));

    this.startSync();
  }

  setInterval(interval: number) {
    this.setProperty('interval', interval);
  }

  protected _setInterval(interval: number) {
    const retryIntervals = [];
    if (interval) {
      let retryInterval = 1000;
      while (retryInterval < interval) {
        retryIntervals.push(retryInterval);
        retryInterval = retryInterval * 2;
      }
    }
    this._retryIntervals = retryIntervals;
    this._setProperty('interval', interval);
  }

  startSync() {
    this._sync();
  }

  stopSync() {
    clearTimeout(this._syncTimeoutId);
  }

  protected _sync() {
    this._loadPermissionCollection()
      .catch((error: AjaxError) => {
        // handle error and return null
        scout.create(ErrorHandler, {displayError: false}).handle(error);
        this._onSyncError();
        return null;
      })
      .then((model: PermissionCollectionModel) => {
        const sync = !!model;
        // if no model was loaded keep last permission collection (or none collection if no permission collection is present)
        model = model || this._permissionCollection || {type: PermissionCollectionType.NONE};
        // update permission collection
        this._permissionCollection = PermissionCollection.ensure(model);
        // schedule next sync
        this._syncTimeoutId = setTimeout(this._sync.bind(this), this.interval);
        if (sync) {
          // notify listeners
          this._onSyncSuccess();
        }
      });
  }

  protected _loadPermissionCollection(): JQuery.Promise<PermissionCollectionModel, AjaxError> {
    return ajax.getJson(this.permissionsUrl, {cache: true}, {retryIntervals: this._retryIntervals});
  }

  /**
   * @returns promise which is resolved if the next sync is successful and rejected if it results in an error.
   */
  whenSync(): JQuery.Promise<void> {
    const success = $.Deferred();
    this.whenSyncSuccess().then(e => success.resolve());
    this.whenSyncError().then(e => success.reject('Permissions were not synchronized successfully.'));
    return success.promise();
  }

  protected _onSyncSuccess() {
    this.trigger('syncSuccess');
  }

  /**
   * @returns promise which is resolved after the next successful sync.
   */
  whenSyncSuccess(): JQuery.Promise<Event<AccessControl>> {
    return this.when('syncSuccess');
  }

  protected _onSyncError() {
    this.trigger('syncError');
  }

  /**
   * @returns promise which is resolved after the next sync error.
   */
  whenSyncError(): JQuery.Promise<Event<AccessControl>> {
    return this.when('syncError');
  }

  /**
   * Check `permission` against the current {@link PermissionCollection}.
   * Quick check is executed synchronously while non-quick check is executed asynchronously.
   */
  check(permission: Permission, quick: true): boolean;
  check(permission: Permission, quick?: false): JQuery.Promise<boolean>;
  check(permission: Permission, quick?: boolean): boolean | JQuery.Promise<boolean>;
  check(permission: Permission, quick?: boolean): boolean | JQuery.Promise<boolean> {
    if (!this._permissionCollection) {
      return quick ? false : $.resolvedPromise(false);
    }
    return this._permissionCollection.implies(permission, quick);
  }

  /**
   * Returns the granted {@link PermissionLevel} for a given permission instance `permission`.
   * - {@link Permission.Level.UNDEFINED} if `permission` is `null` or in general 'not an {@link Permission}'
   * - {@link Permission.Level.NONE} if no level at all is granted to `permission`
   * - {@link PermissionLevel} if the level can be determined exactly.
   * - {@link Permission.Level.UNDEFINED} if there are multiple granted permission levels possible and there is not enough data in the `permission` contained to determine the result closer.
   */
  getGrantedPermissionLevel(permission: Permission): PermissionLevel {
    if (!this._permissionCollection) {
      return Permission.Level.UNDEFINED;
    }
    return this._permissionCollection.getGrantedPermissionLevel(permission);
  }
}

export interface AccessControlModel extends ObjectModel<AccessControl> {
  /**
   * URL pointing to a json resource that provides information about permissions (see {@link PermissionCollectionModel}).
   */
  permissionsUrl?: string;
  /**
   * Interval in which sync is performed (in milliseconds).
   *
   * Default is 1800000 (30 minutes).
   */
  interval?: number;
}

export interface AccessControlEventMap extends PropertyEventMap {
  'syncSuccess': Event<AccessControl>;
  'syncError': Event<AccessControl>;
  'propertyChange:interval': PropertyChangeEvent<number>;
}
