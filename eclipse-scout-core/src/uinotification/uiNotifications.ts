/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, objects, scout, System, systems, UiNotificationHandler, UiNotificationSystem} from '../index';

export class UiNotifications {
  systems = new Map<string, UiNotificationSystem>();

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
    return this._getOrInitSystem(system).subscribe(topic, handler);
  }

  /**
   * Subscribes for a specific topic and executes the given handler when the first notification for that topic arrives.
   * After that, the handler will automatically be unsubscribed.
   *
   * @see subscribe
   */
  subscribeOne(topic: string, handler: UiNotificationHandler, system?: string): JQuery.Promise<string> {
    return this._getOrInitSystem(system).subscribeOne(topic, handler);
  }

  /**
   * Unsubscribes the handler from a specific topic, so it won't be called anymore when a notification for that topic is published.
   *
   * If the last handler was removed for the given system, the connection will be closed and not opened again until a new topic will be subscribed.
   */
  unsubscribe(topic: string, handler?: UiNotificationHandler, system?: string) {
    this._getOrInitSystem(system).unsubscribe(topic, handler);
  }

  /**
   * Unregisters the system and closes the pending connection, if there is any.
   */
  unregisterSystem(name: string) {
    let system = this.systems.get(name);
    if (!system) {
      return;
    }
    system.destroy();
    this.systems.delete(name);
  }

  /**
   * Computes a reload delay for the current client which lies within the given time window.
   * @param reloadDelayWindow The time window in which the random delay must be (maximum) in seconds.
   * @returns The delay in milliseconds.
   */
  computeReloadDelay(reloadDelayWindow: number): number {
    reloadDelayWindow = scout.nvl(reloadDelayWindow, 0);
    if (reloadDelayWindow < 3) {
      // no delay if the window is very small (not necessary)
      return 0;
    }
    return Math.ceil(Math.random() * 1000 * reloadDelayWindow); // randomly delay the reload within the necessary time window (milliseconds)
  }

  /**
   * Unregisters all systems and closes the pending connections, if there are any.
   */
  tearDown() {
    this.systems.forEach(system => system.destroy());
    this.systems.clear();
  }

  protected _getOrInitSystem(system?: string): UiNotificationSystem {
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
    let systemObj = this.systems.get(system);
    if (!systemObj) {
      systemObj = scout.create(UiNotificationSystem, {name: system});
      this.systems.set(system, systemObj);
    }
    return systemObj;
  }
}

export const uiNotifications: UiNotifications = objects.createSingletonProxy(UiNotifications);

App.addListener('fail', event => {
  // Stop polling if application could not be started
  uiNotifications.tearDown();
});
