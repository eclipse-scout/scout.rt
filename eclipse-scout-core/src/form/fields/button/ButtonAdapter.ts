/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
