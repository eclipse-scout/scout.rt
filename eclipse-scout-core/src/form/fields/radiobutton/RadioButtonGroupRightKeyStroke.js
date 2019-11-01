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
import {keys} from '../../../index';
import {KeyStroke} from '../../../index';
import * as $ from 'jquery';

export default class RadioButtonGroupRightKeyStroke extends KeyStroke {

constructor(radioButtonGroup) {
  super();
  this.field = radioButtonGroup;
  this.which = [keys.RIGHT];
  this.renderingHints.render = false;
}


handle(event) {
  var fieldBefore,
    focusedButton = $(event.target).data('radiobutton');

  this.field.radioButtons.some(function(radioButton) {
    if (fieldBefore && radioButton.enabledComputed && radioButton.visible) {
      radioButton.select();
      radioButton.focus();
      return true;
    }
    if (radioButton === focusedButton && radioButton.enabledComputed && radioButton.visible) {
      fieldBefore = radioButton;
    }
  }, this);
}
}
