/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {ObjectFactory, SomePartial, System, UiNotificationDo, uiNotifications, UiNotificationSystem} from '../../index';
import $ from 'jquery';

export class UiNotificationsMock {
  /**
   * Replaces the default {@link UiNotificationSystem} with {@link LocalUiNotificationSystem}.
   */
  static register() {
    // Remove systems so a call to uiNotifications.subscribe() will create new mocked systems.
    uiNotifications.tearDown();

    ObjectFactory.get().register(UiNotificationSystem, () => new LocalUiNotificationSystem());
  }

  /**
   * Unregisters the {@link LocalUiNotificationSystem} so the default {@link UiNotificationSystem} will be active.
   */
  static unregister() {
    // Remove mocked systems so a call to uiNotifications.subscribe() will create new real systems.
    uiNotifications.tearDown();

    ObjectFactory.get().unregister(UiNotificationSystem);
  }

  /**
   * Simulates publishing a notification that will trigger the subscribed handlers without issuing any http requests.
   * The required {@link LocalUiNotificationSystem} is registered automatically during tests unless {@link UiNotificationsMock.unregister} is called explicitly.
   */
  static putNotification(notification: SomePartial<UiNotificationDo, 'id' | 'nodeId' | 'creationTime'>, system?: string) {
    system = system || System.MAIN_SYSTEM;
    let systemObj = uiNotifications.systems.get(system);
    if (!(systemObj instanceof LocalUiNotificationSystem)) {
      throw new Error('You need to register the local system first using UiNotificationsMock.registerLocalSystem');
    }
    systemObj.put(notification);
  }
}

/**
 * A local system that does not use a {@link UiNotificationPoller} and therefore does not issue any http requests.
 * Publishing a notification can be simulated by using {@link putNotification}.
 */
export class LocalUiNotificationSystem extends UiNotificationSystem {

  override whenSubscriptionStart(topic: string): JQuery.Promise<string> {
    return $.resolvedPromise(topic);
  }

  override updatePoller() {
    // Don't create a poller
  }

  put(notification: SomePartial<UiNotificationDo, 'id' | 'nodeId' | 'creationTime'>) {
    super._dispatch([
      $.extend({
        id: '1',
        creationTime: new Date(),
        nodeId: 'node1'
      }, notification)
    ]);
  }
}
