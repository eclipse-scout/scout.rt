/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, AppLinkActionEvent, Event, ModelAdapter, Notification, objects} from '../index';

export class NotificationAdapter extends ModelAdapter {

  protected _onWidgetClose(event: Event<Notification>) {
    this._send('close');
  }

  protected _onWidgetAppLinkAction(event: AppLinkActionEvent) {
    this._send('appLinkAction', {
      ref: event.ref
    });
  }

  protected override _onWidgetEvent(event: Event<Notification>) {
    if (event.type === 'close') {
      this._onWidgetClose(event);
    } else if (event.type === 'appLinkAction') {
      this._onWidgetAppLinkAction(event as AppLinkActionEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }

  /**
   * Static method to modify the prototype of Widget.
   */
  static modifyNotificationPrototype(event: Event) {
    if (!App.get().remote) {
      return;
    }

    // resolveTextKeys
    objects.replacePrototypeFunction(Notification, 'resolveStatusTextKeys', function(this: Notification & { resolveStatusTextKeysOrig }, properties: string[]) {
      if (this.modelAdapter) {
        // Never resolve '${textKey:...}' references in texts from the server
        return;
      }
      return this.resolveStatusTextKeysOrig(properties);
    }, true);
  }
}

App.addListener('bootstrap', NotificationAdapter.modifyNotificationPrototype);
