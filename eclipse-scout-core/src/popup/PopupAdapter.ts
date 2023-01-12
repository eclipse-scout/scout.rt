/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, ModelAdapter, Popup} from '../index';

export class PopupAdapter extends ModelAdapter {
  protected _onWidgetClose(event: Event<Popup>) {
    // Do not close the popup immediately, server will send the close event
    event.preventDefault();
    this._send('close');
  }

  protected override _onWidgetEvent(event: Event<Popup>) {
    if (event.type === 'close') {
      this._onWidgetClose(event);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
