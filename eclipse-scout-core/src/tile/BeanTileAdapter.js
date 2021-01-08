/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {TileAdapter} from '../index';

export default class BeanTileAdapter extends TileAdapter {

  constructor() {
    super();
  }

  _onWidgetAppLinkAction(event) {
    this._send('appLinkAction', {
      ref: event.ref
    });
  }

  _onWidgetEvent(event) {
    if (event.type === 'appLinkAction') {
      this._onWidgetAppLinkAction(event);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
