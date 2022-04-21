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
import {DatePickerTouchPopupLayout, ParsingFailedStatus, scout, TouchPopup} from '../index';

export default class DatePickerTouchPopup extends TouchPopup {

  constructor() {
    super();
  }

  _init(options) {
    super._init(options);
    this._field.on('acceptInput', this._onFieldAcceptInput.bind(this));
    this.addCssClass('date-picker-touch-popup');
  }

  /**
   * @override TouchPopup.js
   */
  _initWidget(options) {
    this._widget = scout.create('DatePicker', {
      parent: this,
      dateFormat: options.dateFormat,
      allowedDates: options.allowedDates
    });
  }

  _render() {
    super._render();
    this._field.$container.addClass('date');
  }

  _onMouseDownOutside() {
    this._acceptInput();
  }

  getDatePicker() {
    return this._widget;
  }

  /**
   * @override
   */
  _createLayout() {
    return new DatePickerTouchPopupLayout(this);
  }

  _onFieldAcceptInput(event) {
    // Delegate to original field
    this._touchField.setDisplayText(event.displayText);
    this._touchField.setErrorStatus(event.errorStatus);
    let hasParsingFailedError = event.errorStatus ? event.errorStatus.containsStatus(ParsingFailedStatus) : false;
    if (!hasParsingFailedError) {
      this._touchField.setValue(event.value);
    }
    this._touchField._triggerAcceptInput();
  }

  /**
   * @override
   */
  _acceptInput() {
    this._field.acceptDate();
    this.close();
  }
}
