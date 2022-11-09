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
import {AbstractLayout, DateField, DateFieldAcceptInputEvent, DatePicker, DatePickerTouchPopupLayout, DatePickerTouchPopupModel, InitModelOf, ParsingFailedStatus, scout, TouchPopup} from '../index';

export class DatePickerTouchPopup extends TouchPopup {
  declare model: DatePickerTouchPopupModel;
  declare _widget: DatePicker;
  declare _field: DateField;
  declare _touchField: DateField;

  protected override _init(options: InitModelOf<this>) {
    super._init(options);
    this._field.on('acceptInput', this._onFieldAcceptInput.bind(this));
    this.addCssClass('date-picker-touch-popup');
  }

  protected override _initWidget(options: DatePickerTouchPopupModel) {
    this._widget = scout.create(DatePicker, {
      parent: this,
      dateFormat: options.dateFormat,
      allowedDates: options.allowedDates
    });
  }

  protected override _render() {
    super._render();
    this._field.$container.addClass('date');
  }

  protected override _onMouseDownOutside(event: MouseEvent) {
    this._acceptInput();
  }

  getDatePicker(): DatePicker {
    return this._widget;
  }

  protected override _createLayout(): AbstractLayout {
    return new DatePickerTouchPopupLayout(this);
  }

  protected _onFieldAcceptInput(event: DateFieldAcceptInputEvent) {
    // Delegate to original field
    this._touchField.setDisplayText(event.displayText);
    this._touchField.setErrorStatus(event.errorStatus);
    let hasParsingFailedError = event.errorStatus ? event.errorStatus.containsStatus(ParsingFailedStatus) : false;
    if (!hasParsingFailedError) {
      this._touchField.setValue(event.value);
    }
    this._touchField._triggerAcceptInput(event.whileTyping);
  }

  protected override _acceptInput() {
    this._field.acceptDate();
    this.close();
  }
}
