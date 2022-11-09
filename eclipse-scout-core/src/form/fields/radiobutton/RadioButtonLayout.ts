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
