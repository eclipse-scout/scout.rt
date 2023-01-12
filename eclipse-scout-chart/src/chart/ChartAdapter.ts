/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, ModelAdapter} from '@eclipse-scout/core';
import {Chart} from '../index';
import {ChartValueClickEvent} from './ChartEventMap';

export class ChartAdapter extends ModelAdapter {
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
