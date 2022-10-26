/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ModelAdapter} from '../index';

export default class NotificationAdapter extends ModelAdapter {

  constructor() {
    super();
  }

  _onWidgetClose(event) {
    this._send('close', {
      ref: event.ref
    });
  }

  _onWidgetAppLinkAction(event) {
    this._send('appLinkAction', {
      ref: event.ref
    });
  }

  _onWidgetEvent(event) {
    if (event.type === 'close') {
      this._onWidgetClose(event);
    } else if (event.type === 'appLinkAction') {
      this._onWidgetAppLinkAction(event);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
