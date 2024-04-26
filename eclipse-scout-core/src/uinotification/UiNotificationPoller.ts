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
  ajax, AjaxCall, AjaxError, arrays, BackgroundJobPollingStatus, dates, ErrorHandler, JsonErrorResponse, objects, PropertyEventEmitter, scout, Session, TopicDo, UiNotificationDo, UiNotificationPollerEventMap, UiNotificationResponse
} from '../index';
import $ from 'jquery';

export class UiNotificationPoller extends PropertyEventEmitter {
  declare eventMap: UiNotificationPollerEventMap;
  declare self: UiNotificationPoller;

  /**
   * Configures in milliseconds the time to wait after a connection error occurs, before the polling will be retried.
   */
  static RETRY_INTERVAL = 5000;

  /**
   * The number of notifications per topic that should be kept until the topic is unsubscribed.
   */
  static HISTORY_COUNT = 10;

  /**
   * Configures in milliseconds how long the connection is allowed to stay open before it will be aborted.
   * This is more like a last resort timeout, the server will release the connection earlier (see scout.uinotification.waitTimeout).
   */
  requestTimeout: number;
  status: BackgroundJobPollingStatus;
  /**
   * Stores the received notifications per topic and per cluster node but not more than {@link UiNotificationPoller.HISTORY_COUNT}.
   */
  notifications: Map<string, Map<string, UiNotificationDo[]>>;
  url: string;
  protected _call: AjaxCall;

  constructor() {
    super();
    this.requestTimeout = 75000;
    this.status = BackgroundJobPollingStatus.STOPPED;
    this.notifications = new Map();
  }

  setTopics(topics: string[]) {
    // Create a new map but keep existing notifications per topic
    this.notifications = new Map<string, Map<string, UiNotificationDo[]>>(topics.map(topic => [topic, this.notifications.get(topic) || new Map()]));
  }

  get topics(): string[] {
    return Array.from(this.notifications.keys());
  }

  get topicsWithLastNotifications(): TopicDo[] {
    // Create an array of TopicDOs containing the last notification of each topic per node
    return Array.from(this.notifications.entries())
      .map(([name, notificationsByNode]) => {
        let lastNotifications = Array.from(notificationsByNode.values()).map(notifications => {
          let lastNotification = arrays.last(notifications);
          return {
            id: lastNotification.id,
            creationTime: lastNotification.creationTime,
            nodeId: lastNotification.nodeId
          } as UiNotificationDo;
        });
        return {name, lastNotifications: lastNotifications.length === 0 ? undefined : lastNotifications};
      });
  }

  restart() {
    this.stop();
    this.start();
  }

  start() {
    if (this.status === BackgroundJobPollingStatus.RUNNING) {
      return;
    }
    this.poll();
  }

  stop() {
    if (this.status === BackgroundJobPollingStatus.STOPPED) {
      return;
    }
    this._call?.abort();
    this.setStatus(BackgroundJobPollingStatus.STOPPED);
  }

  poll() {
    this._poll();
    this.setStatus(BackgroundJobPollingStatus.RUNNING);
  }

  protected _schedulePoll(timeout?: number) {
    if (this.status === BackgroundJobPollingStatus.STOPPED) {
      return;
    }
    setTimeout(() => {
      if (this.status === BackgroundJobPollingStatus.STOPPED) {
        return;
      }
      this.poll();
    }, scout.nvl(timeout, 0));
  }

  protected _poll() {
    this._call?.abort(); // abort in case there is already a call running
    this._call = ajax.createCallJson({
      url: this.url,
      timeout: this.requestTimeout,
      converters: {
        'text json': data => objects.parseJson(data, dates.parseJsonDateMapper('creationTime'))
      },
      data: objects.stringifyJson({
        topics: this.topicsWithLastNotifications
      }, dates.stringifyJsonDateMapper())
    });
    this._call.call()
      .then((response: UiNotificationResponse) => this._onSuccess(response))
      .catch(error => this._onError(error));
  }

  protected _onSuccess(response: UiNotificationResponse) {
    if (response.error) {
      this._onSuccessError(response.error);
      return;
    }
    if (this.status === BackgroundJobPollingStatus.STOPPED) {
      // Don't do anything if poller was stopped in the meantime -> discard notifications
      // In case the poller will be started again, the discarded notifications will be sent again by the server
      return;
    }
    let notifications = response.notifications || [];
    $.log.isInfoEnabled() && $.log.info(`${notifications.length} UI notification(s) received.`);
    notifications = notifications.filter(notification => {
      let {topic, id, nodeId} = notification;
      if (!this.notifications.has(topic)) {
        // Ignore topics that have been unsubscribed in the meantime
        return false;
      }

      // Add to notification history and drop the oldest ones
      let topicNotifications = this.notifications.get(topic);
      let nodeNotifications = objects.getOrSetIfAbsent(topicNotifications, nodeId, () => []);
      if (nodeNotifications.some(existingNotification => existingNotification.id === id)) {
        // Notification already known, ignore it
        $.log.isInfoEnabled() && $.log.info(`UI notification with id '${id}' is already known, dropping it.`);
        return false;
      }
      nodeNotifications.push(notification);
      nodeNotifications = nodeNotifications.sort((n1, n2) => n1.creationTime.getTime() - n2.creationTime.getTime());
      if (nodeNotifications.length > UiNotificationPoller.HISTORY_COUNT) {
        nodeNotifications.splice(0, 1);
      }

      if (notification.subscriptionStart) {
        $.log.isInfoEnabled() && $.log.info(`UI notification with id ${id} marks subscription start.`);
        this.trigger('subscriptionStart', {notification});
        // Just a marker notification -> discard it
        return false;
      }
      return true;
    });

    if (notifications.length) {
      $.log.isInfoEnabled() && $.log.info(`Dispatching UI notifications with ids ${notifications.map(n => n.id)}.`);
      this.trigger('notifications', {notifications});
    }

    this._schedulePoll();
  }

  protected _onSuccessError(error: JsonErrorResponse) {
    if (error.code === Session.JsonResponseError.SESSION_TIMEOUT) {
      $.log.isInfoEnabled() && $.log.info('Stopping ui notification poller due to session timeout');
      this.stop();
    } else {
      // Log every other error, even though they should actually never happen
      scout.create(ErrorHandler, {displayError: false}).handle(error);
    }
  }

  protected _onError(error: AjaxError) {
    if (error.textStatus === 'abort' && this.status === BackgroundJobPollingStatus.STOPPED || this._call.pendingCall) {
      // When poller is stopped, call is aborted. PendingCall is set if poller has already been restarted.
      return;
    }
    this.setStatus(BackgroundJobPollingStatus.FAILURE);

    if (scout.isOneOf(error.jqXHR.status, 401, 403)) {
      $.log.isInfoEnabled() && $.log.info(`Stopping ui notification poller because operation is not permitted (${error.jqXHR.status})`);
      this.stop();
      return;
    }

    if (AjaxCall.isOfflineError(error.jqXHR, error.textStatus, error.errorThrown)) {
      $.log.isInfoEnabled() && $.log.info('Connection failed', error);
    } else {
      scout.create(ErrorHandler, {displayError: false}).handle(error);
    }
    this._schedulePoll(UiNotificationPoller.RETRY_INTERVAL);
  }

  setStatus(status: BackgroundJobPollingStatus) {
    const changed = this.setProperty('status', status);
    if (changed) {
      $.log.isInfoEnabled() && $.log.info('UI notification poller status changed: ' + status);
    }
  }
}
