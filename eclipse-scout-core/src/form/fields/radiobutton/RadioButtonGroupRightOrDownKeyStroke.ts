/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, RadioButtonGroup} from '../../../index';
import $ from 'jquery';

export class RadioButtonGroupRightOrDownKeyStroke extends KeyStroke {
  declare field: RadioButtonGroup<any>;

  constructor(radioButtonGroup: RadioButtonGroup<any>) {
    super();
    this.field = radioButtonGroup;
    this.which = [keys.RIGHT, keys.DOWN];
    this.renderingHints.render = false;
  }

  // Find the radio button that received the keystroke, then find the next selectable radio button and select it
  override handle(event: JQuery.KeyboardEventBase) {
    let fieldBefore,
      focusedButton = $(event.target).data('radiobutton');

    this.field.radioButtons.some(radioButton => {
      if (fieldBefore && radioButton.enabledComputed && radioButton.visible) {
        radioButton.select();
        radioButton.focus();
        return true;
      }
      // do not check for enabled here, we also want the keystroke to work if the focused radio button was disabled dynamically
      if (radioButton === focusedButton) {
        fieldBefore = radioButton;
      }
      return false;
    }, this);
  }
}
