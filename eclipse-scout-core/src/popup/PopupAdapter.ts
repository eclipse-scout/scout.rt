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
import {Event, ModelAdapter, Popup} from '../index';

export default class PopupAdapter extends ModelAdapter {

  constructor() {
    super();
  }

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
