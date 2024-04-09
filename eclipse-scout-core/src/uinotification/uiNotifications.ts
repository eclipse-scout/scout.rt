/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {System, systems, UiNotificationHandler, UiNotificationSystem} from '../index';

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
   *    To subscribe to a system with different url, the system must be registered first using {@link systems.getOrCreate} and then calling {@link System.setBaseUrl}
   *    and/or {@link System.setEndpointUrl}.
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
  system = system || System.MAIN_SYSTEM;
  if (system !== System.MAIN_SYSTEM && !systems.exists(system)) {
    // The custom system is not known (yet). Therefore, no custom baseUrl is present. The system will use the default endpoint url.
    // To prevent having multiple notification systems (and therefore pollers) for the same endpoint url, the system is reset to main if unknown.
    //
    // This ignores the case when the main system is registered to a non-default baseUrl. E.g. main system has '/api-main' and other system has '/api'.
    // Then subscribing to other system would not be possible before actually registering it even if the url would not require a registration (because it is default).
    // But as this case is rather uncommon and the '/api' url should be considered to be the main system instead, this case is ignored as there is an easy workaround:
    // register the other system first with default base url.
    system = System.MAIN_SYSTEM;
  }
  let systemObj = uiNotifications.systems.get(system);
  if (!systemObj) {
    systemObj = new UiNotificationSystem(system);
    uiNotifications.systems.set(system, systemObj);
  }
  return systemObj;
}
