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
import {CalendarComponentAppLinkActionEvent, Event, ModelAdapter, Widget} from '../index';

export class CalendarComponentAdapter extends ModelAdapter {

  protected _onWidgetAppLinkAction(event: CalendarComponentAppLinkActionEvent) {
    this._send('appLinkAction', {
      ref: event.ref
    });
  }

  protected override _onWidgetEvent(event: Event<Widget>) {
    if (event.type === 'appLinkAction') {
      this._onWidgetAppLinkAction(event as CalendarComponentAppLinkActionEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
