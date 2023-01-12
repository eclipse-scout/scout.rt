/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DatePickerPopup, Dimension, HtmlCompPrefSizeOptions, PopupLayout} from '../index';

export class DatePickerPopupLayout extends PopupLayout {
  declare popup: DatePickerPopup;

  constructor(popup: DatePickerPopup) {
    super(popup);
    this.doubleCalcPrefSize = false;
  }

  override layout($container: JQuery) {
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

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    let prefSize,
      htmlComp = this.popup.htmlComp,
      htmlPicker = this.popup.picker.htmlComp;

    prefSize = htmlPicker.prefSize()
      .add(htmlComp.insets())
      .add(htmlPicker.margins());

    return prefSize;
  }
}
