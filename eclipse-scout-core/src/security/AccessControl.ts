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
  ajax, AjaxCall, AjaxError, DoEntity, ErrorHandler, EventHandler, InitModelOf, ObjectModel, Permission, PermissionCollection, PermissionCollectionModel, PermissionCollectionType, PermissionLevel, PropertyEventEmitter, scout, SomeRequired,
  UiNotificationEvent, uiNotifications
} from '../index';
import $ from 'jquery';

export class AccessControl extends PropertyEventEmitter implements AccessControlModel {
  declare self: AccessControl;
  declare model: AccessControlModel;
  declare initModel: SomeRequired<this['model'], 'permissionsUrl'>;

  permissionsUrl: string;

  protected _permissionCollection: PermissionCollection;
  protected _permissionUpdateEventHandler: EventHandler<UiNotificationEvent>;
  protected _reloadTimeoutId: number;
  protected _call: AjaxCall;

  constructor() {
    super();
    this.permissionsUrl = null;
    this._call = null;
    this._reloadTimeoutId = -1;
    this._permissionCollection = PermissionCollection.ensure({type: PermissionCollectionType.NONE});
    this._permissionUpdateEventHandler = this._onPermissionUpdateNotify.bind(this);
  }

  override init(model: InitModelOf<this>) {
    this.permissionsUrl = scout.assertParameter('permissionsUrl', model.permissionsUrl);
  }

  bootstrap(): JQuery.Promise<void> {
    this._subscribeForNotifications();
    return this._load();
  }

  destroy() {
    this._unsubscribeFromNotifications();
  }

  protected _subscribeForNotifications() {
    uiNotifications.subscribe('permissionsUpdate', this._permissionUpdateEventHandler);
  }

  protected _unsubscribeFromNotifications() {
    uiNotifications.unsubscribe('permissionsUpdate', this._permissionUpdateEventHandler);
  }

  protected _onPermissionUpdateNotify(event: UiNotificationEvent) {
    const message = event.message as PermissionUpdateMessageDo;
    const reloadDelay = uiNotifications.computeReloadDelay(message.reloadDelayWindow);
    $.log.info(`About to refresh permission cache with a delay of ${reloadDelay}ms.`);
    if (this._reloadTimeoutId > 0) {
      // cancel current update and schedule a new one to ensure the newest changes from the backend are fetched in case the fetch has already started.
      clearTimeout(this._reloadTimeoutId);
    }
    this._reloadTimeoutId = setTimeout(() => {
      this._reloadTimeoutId = -1;
      this._load()
        .catch((error: AjaxError) => {
          // Log error
          scout.create(ErrorHandler, {displayError: false}).handle(error);
        });
    }, reloadDelay);
  }

  protected _load(): JQuery.Promise<void> {
    return this._loadPermissionCollection()
      .always(() => {
        this._call = null; // call ended. Not necessary anymore
      })
      .then((model: PermissionCollectionModel) => {
        // update permission collection
        this._permissionCollection = PermissionCollection.ensure(model);
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

export interface PermissionUpdateMessageDo extends DoEntity {
  reloadDelayWindow: number;
}
