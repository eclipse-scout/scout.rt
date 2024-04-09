/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {
  ajax, AjaxCall, AjaxError, DoEntity, ErrorHandler, Event, EventHandler, InitModelOf, ObjectModel, Permission, PermissionCollection, PermissionCollectionModel, PermissionCollectionType, PermissionLevel, PropertyEventEmitter,
  PropertyEventMap, scout, SomeRequired, UiNotificationEvent, uiNotifications
} from '../index';
import $ from 'jquery';

export class AccessControl extends PropertyEventEmitter implements AccessControlModel {
  declare self: AccessControl;
  declare model: AccessControlModel;
  declare initModel: SomeRequired<this['model'], 'permissionsUrl'>;
  declare eventMap: AccessControlEventMap;

  permissionsUrl: string;

  protected _permissionCollection: PermissionCollection;
  protected _permissionUpdateEventHandler: EventHandler<UiNotificationEvent>;
  protected _reloadTimeoutId;
  protected _call: AjaxCall;

  constructor() {
    super();
    this.permissionsUrl = null;
    this._call = null;
    this._reloadTimeoutId = -1;
    this._permissionCollection = null;
    this._permissionUpdateEventHandler = this._onPermissionUpdateNotify.bind(this);
  }

  override init(model: InitModelOf<this>) {
    this.permissionsUrl = scout.assertParameter('permissionsUrl', model.permissionsUrl);
    this.startSync();
  }

  startSync() {
    this._subscribeForNotifications();
    this._sync();
  }

  stopSync() {
    this._unsubscribeFromNotifications();
  }

  protected _subscribeForNotifications() {
    uiNotifications.subscribe('permissionsUpdate', this._permissionUpdateEventHandler);
  }

  protected _unsubscribeFromNotifications() {
    uiNotifications.unsubscribe('permissionsUpdate', this._permissionUpdateEventHandler);
  }

  protected _onPermissionUpdateNotify(event: UiNotificationEvent) {
    let message = event.message as PermissionUpdateMessageDo;
    let reloadDelayWindow = scout.nvl(message.reloadDelayWindow, 0);
    let reloadDelay = this._computeReloadDelay(reloadDelayWindow);
    $.log.info(`About to refresh permission cache with a delay of ${reloadDelay}ms.`);
    if (this._reloadTimeoutId > 0) {
      // cancel current update and schedule a new one to ensure the newest changes from the backend are fetched in case the fetch has already started.
      clearTimeout(this._reloadTimeoutId);
    }
    this._reloadTimeoutId = setTimeout(() => {
      this._reloadTimeoutId = -1;
      this._sync();
    }, reloadDelay);
  }

  protected _computeReloadDelay(reloadDelayWindow: number): number {
    if (reloadDelayWindow < 3) {
      // no delay if the window is very small (not necessary)
      return 0;
    }
    return Math.ceil(Math.random() * 1000 * reloadDelayWindow); // randomly delay the reload (milliseconds)
  }

  protected _sync() {
    this._loadPermissionCollection()
      .always(() => {
        this._call = null; // call ended. Not necessary anymore
      })
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
        if (sync) {
          // notify listeners
          this._onSyncSuccess();
        }
      });
  }

  protected _loadPermissionCollection(): JQuery.Promise<PermissionCollectionModel, AjaxError> {
    this._call?.abort(); // abort in case there is already a call running
    this._call = ajax.createCallJson({
      url: this.permissionsUrl,
      type: 'GET',
      cache: true
    }, {
      maxRetries: -1, // unlimited retries
      retryIntervals: [300, 500, 1000, 5000]
    });
    return this._call.call();
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
}

export interface AccessControlEventMap extends PropertyEventMap {
  'syncSuccess': Event<AccessControl>;
  'syncError': Event<AccessControl>;
}

export interface PermissionUpdateMessageDo extends DoEntity {
  reloadDelayWindow: number;
}
