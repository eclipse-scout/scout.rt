/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormFieldLayout, SliderField} from '../../../index';

export class SliderFieldLayout extends FormFieldLayout {
  sliderField: SliderField;

  constructor(sliderField: SliderField) {
    super(sliderField);
    this.sliderField = sliderField;
  }

  override layout($container: JQuery) {
    super.layout($container);

    let $fieldContainer = this.sliderField.$fieldContainer;
    $fieldContainer.removeClass('compact');
    if ($fieldContainer[0].clientWidth < $fieldContainer[0].scrollWidth) {
      if (this.sliderField.valueEditable) {
        // Because enabling the compact mode hides the slider, we have to set the focus
        // to somewhere else (otherwise, it would be set to the <body> by the browser)
        let $activeElement = $fieldContainer.activeElement();
        if ($activeElement.is(this.sliderField.slider.$container)) {
          this.sliderField.session.focusManager.focusNextTabbable($activeElement);
        }
      }
      $fieldContainer.addClass('compact');
    }
  }
}
