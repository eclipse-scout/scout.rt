/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, ModelAdapter, Notification, NotificationAppLinkActionEvent} from '../index';

export class NotificationAdapter extends ModelAdapter {

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
