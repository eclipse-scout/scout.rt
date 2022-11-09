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
import {DateField, DateFieldAcceptInputEvent, InitModelOf, ParsingFailedStatus, scout, SomeRequired, TimePicker, TimePickerTouchPopupModel, TouchPopup} from '../index';

export class TimePickerTouchPopup extends TouchPopup implements TimePickerTouchPopupModel {
  declare model: TimePickerTouchPopupModel;
  declare initModel: SomeRequired<this['model'], 'parent' | 'field' | 'timeResolution'>;
  declare _widget: TimePicker;
  declare _field: DateField;

  protected override _init(options: InitModelOf<this>) {
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
    this._field.acceptTime();
    this.close();
  }
}
