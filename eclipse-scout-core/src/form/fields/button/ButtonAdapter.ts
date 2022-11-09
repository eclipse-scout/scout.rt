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
import {Button, Event, FormFieldAdapter} from '../../../index';

export class ButtonAdapter extends FormFieldAdapter {
  declare widget: Button;

  constructor() {
    super();
    this._addRemoteProperties(['selected']);
  }

  protected _onWidgetClick(event: Event<Button>) {
    if (this.widget.displayStyle === Button.DisplayStyle.TOGGLE || this.widget.menus.length > 0) {
      return;
    }
    this._send('click');
  }

  protected override _onWidgetEvent(event: Event<Button>) {
    if (event.type === 'click') {
      this._onWidgetClick(event);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
