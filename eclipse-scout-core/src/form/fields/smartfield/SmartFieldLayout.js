/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FormFieldLayout} from '../../../index';

/**
 * SmartFieldLayout works like FormLayout but additionally layouts its proposal-chooser popup.
 */
export default class SmartFieldLayout extends FormFieldLayout {

  constructor(smartField) {
    super(smartField);
    this._smartField = smartField;
  }

  layout($container) {
    super.layout($container);

    // when embedded smart-field layout must not validate the popup
    // since this would lead to an endless recursion because the smart-field
    // is a child of the popup.
    if (this._smartField.embedded) {
      return;
    }

    let popup = this._smartField.popup;
    if (popup && popup.rendered) {
      // Make sure the popup is correctly layouted and positioned
      popup.position();
      popup.validateLayout();
    }
  }
}
