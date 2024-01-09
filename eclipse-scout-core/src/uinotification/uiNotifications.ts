/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {UiNotificationHandler, UiNotificationSystem} from '../index';

export const uiNotifications = {
  systems: new Map<string, UiNotificationSystem>(),

  /**
   * Subscribes for a specific topic and executes the given handler whenever a notification for that topic arrives.
   *
   * The same topic can be subscribed multiple times by different handlers.
   * The first subscription establishes the connection.
   *
   * @param topic Can be any string.
   *    Try to make the topic as specific as possible, e.g. by appending the id of an element to the topic name, instead of filtering the notifications in the UI.
   *    This reduces the overhead of sending unnecessary notifications to clients.
   * @param system The system that publishes the notification. If no system is provided, the `main` system is used, which points to `api/ui-notifications`.
   *    To subscribe to another system, the system must be registered first using {@link registerSystem}.
   * @returns a promise that will be resolved as soon as the subscription was successful and notifications can be received.
   */
  subscribe(topic: string, handler: UiNotificationHandler, system?: string): JQuery.Promise<string> {
    return getOrInitSystem(system).subscribe(topic, handler);
  },

  /**
   * Subscribes for a specific topic and executes the given handler when the first notification for that topic arrives.
   * After that, the handler will automatically be unsubscribed.
   *
   * @see subscribe
   */
  subscribeOne(topic: string, handler: UiNotificationHandler, system?: string): JQuery.Promise<string> {
    return getOrInitSystem(system).subscribeOne(topic, handler);
  },

  /**
   * Unsubscribes the handler from a specific topic, so it won't be called anymore when a notification for that topic is published.
   *
   * If the last handler was removed for the given system, the connection will be closed and not opened again until a new topic will be subscribed.
   */
  unsubscribe(topic: string, handler?: UiNotificationHandler, system?: string) {
    getOrInitSystem(system).unsubscribe(topic, handler);
  },

  /**
   * Registers a new system that can publish ui notifications.
   *
   * To subscribe topics from that system, pass the name as parameter when calling {@link subscribe}.
   */
  registerSystem(name: string, url: string) {
    if (uiNotifications.systems.has(name)) {
      throw new Error(`System ${name} is already registered.`);
    }
    uiNotifications.systems.set(name, new UiNotificationSystem(url));
  },

  /**
   * Unregisters the system and closes the pending connection, if there is any.
   */
  unregisterSystem(name: string) {
    let system = uiNotifications.systems.get(name);
    if (!system) {
      return;
    }
    system.destroy();
    uiNotifications.systems.delete(name);
  },

  tearDown() {
    for (const system of uiNotifications.systems.values()) {
      system.destroy();
    }
    uiNotifications.systems.clear();
  }
};

function getOrInitSystem(system?: string): UiNotificationSystem {
  if (!system && !uiNotifications.systems.has('main')) {
    uiNotifications.registerSystem('main', 'api/ui-notifications');
  }

  let systemObj = uiNotifications.systems.get(system || 'main');
  if (!systemObj) {
    throw new Error(`Unknown system ${system}`);
  }
  return systemObj;
}
