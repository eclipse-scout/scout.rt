/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DatePickerTouchPopup, TouchPopupLayout} from '../index';

export class DatePickerTouchPopupLayout extends TouchPopupLayout {
  declare popup: DatePickerTouchPopup;

  constructor(popup: DatePickerTouchPopup) {
    super(popup);
  }

  override layout($container: JQuery) {
    super.layout($container);
    this.popup.getDatePicker()._layoutWeekendSeparators();
  }
}
