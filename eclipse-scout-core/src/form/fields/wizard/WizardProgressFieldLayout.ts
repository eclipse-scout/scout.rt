/*
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FormFieldLayout} from '../../../index';

export default class WizardProgressFieldLayout extends FormFieldLayout {

  constructor(formField) {
    super(formField);
  }

  layout($container) {
    super.layout($container);

    // Remember old scroll position, because setting the body width might change it
    let oldScrollLeft = this.formField.$field.scrollLeft();

    // Explicitly set width of body to scrollWidth because container is scrollable. Otherwise,
    // the body would have the wrong size because it has "overflow: hidden" set.
    let $body = this.formField.$wizardStepsBody;
    $body.width('auto'); // reset previously set width to ensure 'scrollWidth' returns the preferred size
    let bodyWidth = $body[0].scrollWidth;
    $body.width(bodyWidth);

    // Ensure scrolling position did not change because of the width change (prevents flickering effect)
    this.formField.$field.scrollLeft(oldScrollLeft);
    // But also ensure the current step is visible
    this.formField.scrollToActiveStep();
  }
}
