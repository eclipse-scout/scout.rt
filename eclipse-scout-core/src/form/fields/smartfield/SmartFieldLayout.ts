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
import {FormFieldLayout, SmartField} from '../../../index';

/**
 * SmartFieldLayout works like FormLayout but additionally layouts its proposal-chooser popup.
 */
export default class SmartFieldLayout extends FormFieldLayout {
  protected _smartField: SmartField<any>;

  constructor(smartField: SmartField<any>) {
    super(smartField);
    this._smartField = smartField;
  }

  override layout($container: JQuery) {
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
