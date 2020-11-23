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
import {keys, KeyStroke} from '../../../index';
import $ from 'jquery';

export default class RadioButtonGroupLeftOrUpKeyStroke extends KeyStroke {

  constructor(radioButtonGroup) {
    super();
    this.field = radioButtonGroup;
    this.which = [keys.LEFT, keys.UP];
    this.renderingHints.render = false;
  }

  handle(event) {
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
