/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Event, Notification, PropertyChangeEvent, Status, WidgetEventMap} from '../index';

export interface NotificationAppLinkActionEvent<N extends Notification = Notification> extends Event<N> {
  ref: string;
}

export default interface NotificationEventMap extends WidgetEventMap {
  'appLinkAction': NotificationAppLinkActionEvent;
  'close': Event<Notification>;
  'propertyChange:closable': PropertyChangeEvent<boolean>;
  'propertyChange:status': PropertyChangeEvent<Status>;
}
