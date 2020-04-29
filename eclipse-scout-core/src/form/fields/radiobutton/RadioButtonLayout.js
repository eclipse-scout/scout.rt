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
import {ButtonLayout} from '../../../index';

export default class RadioButtonLayout extends ButtonLayout {

  constructor(radioButton) {
    super(radioButton);
    this.radioButton = radioButton;
  }

  layout($container) {
    super.layout($container);

    let $icon = this.radioButton.get$Icon(),
      $circle = this.radioButton.$radioButton,
      $label = this.radioButton.$buttonLabel,
      $fieldContainer = this.radioButton.$fieldContainer;

    let labelMaxWidth = $fieldContainer.width() - ($circle.outerWidth(true) + ($icon.length ? $icon.outerWidth(true) : 0));
    $label.css('max-width', labelMaxWidth);
  }
}
