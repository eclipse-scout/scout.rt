/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ButtonLayout, RadioButton} from '../../../index';

export class RadioButtonLayout extends ButtonLayout {
  radioButton: RadioButton<any>;

  constructor(radioButton: RadioButton<any>) {
    super(radioButton);
    this.radioButton = radioButton;
  }

  override layout($container: JQuery) {
    super.layout($container);

    let $icon = this.radioButton.get$Icon(),
      $circle = this.radioButton.$radioButton,
      $label = this.radioButton.$buttonLabel,
      $fieldContainer = this.radioButton.$fieldContainer;

    let labelMaxWidth = $fieldContainer.width() - ($circle.outerWidth(true) + ($icon.length ? $icon.outerWidth(true) : 0));
    $label.css('max-width', labelMaxWidth);
  }
}
