/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {PopupLayout} from '../index';

export default class DatePickerPopupLayout extends PopupLayout {

  constructor(popup) {
    super();
    this.popup = popup;
    this.doubleCalcPrefSize = false;
  }

  layout($container) {
    let size,
      htmlComp = this.popup.htmlComp,
      htmlPicker = this.popup.picker.htmlComp;

    super.layout($container);

    size = htmlComp.size()
      .subtract(htmlComp.insets())
      .subtract(htmlPicker.margins());

    htmlPicker.setSize(size);

    // Reposition because opening direction may have to be switched if popup gets bigger
    // Don't do it the first time (will be done by popup.open), only if the popup is already open and gets layouted again
    if (this.popup.htmlComp.layouted) {
      this.popup.position();
    }

    this.popup.getDatePicker()._layoutWeekendSeparators();
  }

  preferredLayoutSize($container) {
    let prefSize,
      htmlComp = this.popup.htmlComp,
      htmlPicker = this.popup.picker.htmlComp;

    prefSize = htmlPicker.prefSize()
      .add(htmlComp.insets())
      .add(htmlPicker.margins());

    return prefSize;
  }
}
