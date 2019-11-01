/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ModelAdapter} from '@eclipse-scout/core';

export default class ChartAdapter extends ModelAdapter {

  constructor() {
    super();
  }


  _onWidgetValueClick(event) {
    this._send('valueClick', event.data);
  }

  _onWidgetEvent(event) {
    if (event.type === 'valueClick') {
      this._onWidgetValueClick(event);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
