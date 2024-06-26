/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, EventHandler, EventListener, EventSupport, scout, systems, UiNotificationDo, UiNotificationHandler, UiNotificationPoller} from '../index';

export class UiNotificationSystem {
  poller: UiNotificationPoller;
  name: string;
  events: UiNotificationEventSupport;

  constructor(name: string) {
    this.name = name;
    this.events = new UiNotificationEventSupport(this);
  }

  subscribe(topic: string, handler: UiNotificationHandler): JQuery.Promise<string> {
    scout.assertParameter('topic', topic);
    scout.assertParameter('handler', handler);
    this.events.on(topic, handler);
    return this.whenSubscriptionStart(topic);
  }

  subscribeOne(topic: string, handler: UiNotificationHandler): JQuery.Promise<string> {
    scout.assertParameter('topic', topic);
    scout.assertParameter('handler', handler);
    this.events.one(topic, handler);
    return this.whenSubscriptionStart(topic);
  }

  unsubscribe(topic: string, handler?: UiNotificationHandler) {
    scout.assertParameter('topic', topic);
    this.events.off(topic, handler);
  }

  whenSubscriptionStart(topic: string): JQuery.Promise<string> {
    if (this.poller.notifications.get(topic)?.size) {
      // If there is at least one notification, the subscription has been started successfully.
      // The notification may be the subscriptionStart notification or another one if subscriptionStart notification has been removed because history size exceeded HISTORY_COUNT.
      return $.resolvedPromise(topic);
    }
    let deferred = $.Deferred();
    let handler = event => {
      if (event.notification.topic === topic) {
        deferred.resolve(topic);
        this.poller.off('subscriptionStart', handler);
      }
    };
    this.poller.on('subscriptionStart', handler);
    return deferred.promise();
  }

  protected _dispatch(notifications: UiNotificationDo[]) {
    for (const notification of notifications) {
      if (!this.events.count(notification.topic)) {
        $.log.isInfoEnabled() && $.log.info('Notification received but no handler registered');
        return;
      }
      this._trigger(notification.topic, notification);
    }
  }

  protected _trigger(type: string, notification: UiNotificationDo) {
    let event = new Event({
      id: notification.id,
      topic: notification.topic,
      creationTime: notification.creationTime,
      message: notification.message
    });
    event.source = this;
    this.events.trigger(type, event);
  }

  updatePoller() {
    let topics = this.events.types();
    let poller = this.poller;
    if (topics.length > 0 && !poller) {
      // First topic added -> create poller
      let endpointUrl = systems.getOrCreate(this.name).getEndpointUrl('ui-notifications', 'ui-notifications');
      poller = scout.create(UiNotificationPoller, {
        system: this,
        url: endpointUrl
      });
      poller.on('notifications', event => this._dispatch(event.notifications));
      this.poller = poller;
    }

    if (!poller) {
      // No topics registered and no poller started -> do nothing
      return;
    }

    // Update poller with new topics
    poller.setTopics(topics);
    if (poller.topics.length === 0) {
      // Last topic removed -> stop poller
      poller.stop();
      this.poller = null;
    } else {
      poller.restart();
    }
  }

  destroy() {
    this.poller?.stop();
    this.poller = null;
  }
}

export class UiNotificationEventSupport extends EventSupport {
  system: UiNotificationSystem;

  constructor(system: UiNotificationSystem) {
    super();
    this.system = system;
  }

  override on(type: string, func: EventHandler, origFunc?: EventHandler): EventListener {
    let listener = super.on(type, func, origFunc);
    this.system.updatePoller();
    return listener;
  }

  override off(type: string, func?: EventHandler) {
    super.off(type, func);
    this.system.updatePoller();
  }
}
