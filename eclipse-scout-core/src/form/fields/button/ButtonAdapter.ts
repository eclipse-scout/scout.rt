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
import {Button, FormFieldAdapter} from '../../../index';

export default class ButtonAdapter extends FormFieldAdapter {

  constructor() {
    super();
    this._addRemoteProperties(['selected']);
  }

  _onWidgetClick(event) {
    if (this.widget.displayStyle === Button.DisplayStyle.TOGGLE || this.widget.menus.length > 0) {
      return;
    }
    this._send('click');
  }

  _onWidgetEvent(event) {
    if (event.type === 'click') {
      this._onWidgetClick(event);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
