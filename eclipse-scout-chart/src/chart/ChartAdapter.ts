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
import {Event, ModelAdapter} from '@eclipse-scout/core';
import {Chart} from '../index';
import {ChartValueClickEvent} from './ChartEventMap';

export default class ChartAdapter extends ModelAdapter {

  constructor() {
    super();
  }

  protected _onWidgetValueClick(event: ChartValueClickEvent) {
    this._send('valueClick', event.data);
  }

  protected override _onWidgetEvent(event: Event<Chart>) {
    if (event.type === 'valueClick') {
      this._onWidgetValueClick(event as ChartValueClickEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
