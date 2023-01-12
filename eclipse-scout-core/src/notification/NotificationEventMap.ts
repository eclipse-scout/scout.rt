/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, Notification, PropertyChangeEvent, Status, WidgetEventMap} from '../index';

export interface NotificationAppLinkActionEvent<N extends Notification = Notification> extends Event<N> {
  ref: string;
}

export interface NotificationEventMap extends WidgetEventMap {
  'appLinkAction': NotificationAppLinkActionEvent;
  'close': Event<Notification>;
  'propertyChange:closable': PropertyChangeEvent<boolean>;
  'propertyChange:status': PropertyChangeEvent<Status>;
}
