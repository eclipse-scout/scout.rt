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
import {AbstractLayout, DateField, DatePicker, DatePickerTouchPopupLayout, DatePickerTouchPopupModel, ParsingFailedStatus, scout, TouchPopup} from '../index';

export default class DatePickerTouchPopup extends TouchPopup {
  declare model: DatePickerTouchPopupModel;
  declare _widget: DatePicker;
  declare _field: DateField;
  declare _touchField: DateField;

  constructor() {
    super();
  }

  protected override _init(options: DatePickerTouchPopupModel) {
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

  protected _onFieldAcceptInput(event) { // FIXME TS: add event type as soon as DateField has been migrated
    // Delegate to original field
    this._touchField.setDisplayText(event.displayText);
    this._touchField.setErrorStatus(event.errorStatus);
    let hasParsingFailedError = event.errorStatus ? event.errorStatus.containsStatus(ParsingFailedStatus) : false;
    if (!hasParsingFailedError) {
      this._touchField.setValue(event.value);
    }
    this._touchField._triggerAcceptInput();
  }

  protected override _acceptInput() {
    this._field.acceptDate();
    this.close();
  }
}
