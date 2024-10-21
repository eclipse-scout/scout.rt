/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {ObjectFactory, UiNotificationHandler, uiNotifications, UiNotificationSystem} from '../../index';
import $ from 'jquery';

export class UiNotificationsMock {
  static register() {
    // Remove systems so a call to uiNotifications.subscribe() will create new mocked systems.
    uiNotifications.tearDown();

    ObjectFactory.get().register(UiNotificationSystem, () => new UiNotificationSystemMock());
  }

  static unregister() {
    // Remove mocked systems so a call to uiNotifications.subscribe() will create new real systems.
    uiNotifications.tearDown();

    ObjectFactory.get().unregister(UiNotificationSystem);
  }
}

export class UiNotificationSystemMock extends UiNotificationSystem {
  override subscribe(topic: string, handler: UiNotificationHandler): JQuery.Promise<string> {
    return $.Deferred().promise();
  }

  override subscribeOne(topic: string, handler: UiNotificationHandler): JQuery.Promise<string> {
    return $.Deferred().promise();
  }

  override unsubscribe(topic: string, handler?: UiNotificationHandler) {
    // nop
  }
}
