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
import {DateField, ParsingFailedStatus, scout, TimePicker, TouchPopup} from '../index';
import TimePickerTouchPopupModel from './TimePickerTouchPopupModel';

export default class TimePickerTouchPopup extends TouchPopup {
  declare model: TimePickerTouchPopupModel;
  declare _widget: TimePicker;
  declare _field: DateField;

  constructor() {
    super();
  }

  protected override _init(options: TimePickerTouchPopupModel) {
    super._init(options);
    this._field.on('acceptInput', this._onFieldAcceptInput.bind(this));
    this.addCssClass('time-picker-touch-popup');
  }

  protected override _initWidget(options: TimePickerTouchPopupModel) {
    this._widget = scout.create(TimePicker, {
      parent: this,
      timeResolution: options.timeResolution
    });
  }

  protected override _render() {
    super._render();
    this._field.$container.addClass('time');
  }

  getTimePicker(): TimePicker {
    return this._widget;
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
    this._field.acceptTime();
    this.close();
  }
}
