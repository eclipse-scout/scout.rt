/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BackgroundJobPollingStatus, Event, PropertyChangeEvent, PropertyEventMap, UiNotificationDo, UiNotificationPoller} from '../index';

export interface UiNotificationPollerSubscriptionStartEvent<TSource = UiNotificationPoller> extends Event<TSource> {
  notification: UiNotificationDo[];
}

export interface UiNotificationPollerNotificationsEvent<TSource = UiNotificationPoller> extends Event<TSource> {
  notifications: UiNotificationDo[];
}

export interface UiNotificationPollerEventMap extends PropertyEventMap {
  'propertyChange:status': PropertyChangeEvent<BackgroundJobPollingStatus>;
  'subscriptionStart': UiNotificationPollerSubscriptionStartEvent;
  'notifications': UiNotificationPollerNotificationsEvent;
}
