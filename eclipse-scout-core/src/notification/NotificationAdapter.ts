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
import {Event, ModelAdapter, Notification} from '../index';
import {NotificationAppLinkActionEvent} from './NotificationEventMap';

export default class NotificationAdapter extends ModelAdapter<Notification> {

  constructor() {
    super();
  }

  protected _onWidgetClose(event: Event<Notification>) {
    this._send('close');
  }

  protected _onWidgetAppLinkAction(event: NotificationAppLinkActionEvent) {
    this._send('appLinkAction', {
      ref: event.ref
    });
  }

  protected override _onWidgetEvent(event: Event<Notification>) {
    if (event.type === 'close') {
      this._onWidgetClose(event);
    } else if (event.type === 'appLinkAction') {
      this._onWidgetAppLinkAction(event as NotificationAppLinkActionEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
