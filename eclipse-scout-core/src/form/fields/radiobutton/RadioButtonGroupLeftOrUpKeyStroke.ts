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

export class RadioButtonGroupLeftOrUpKeyStroke extends KeyStroke {
  declare field: RadioButtonGroup<any>;

  constructor(radioButtonGroup: RadioButtonGroup<any>) {
    super();
    this.field = radioButtonGroup;
    this.which = [keys.LEFT, keys.UP];
    this.renderingHints.render = false;
  }

  override handle(event: JQuery.KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    let fieldBefore,
      focusedButton = $(event.target).data('radiobutton');

    this.field.radioButtons.some(radioButton => {
      if (fieldBefore && radioButton === focusedButton) {
        fieldBefore.select();
        fieldBefore.focus();
        return true;
      }
      if (radioButton.enabledComputed && radioButton.visible) {
        fieldBefore = radioButton;
      }
      return false;
    }, this);
  }
}
