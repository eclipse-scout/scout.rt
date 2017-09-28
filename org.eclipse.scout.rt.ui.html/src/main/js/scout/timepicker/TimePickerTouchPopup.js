/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TimePickerTouchPopup = function() {
  scout.TimePickerTouchPopup.parent.call(this);
};
scout.inherits(scout.TimePickerTouchPopup, scout.TouchPopup);

scout.TimePickerTouchPopup.prototype._init = function(options) {
  scout.TimePickerTouchPopup.parent.prototype._init.call(this, options);
  this._field.on('acceptInput', this._onFieldAcceptInput.bind(this));
};

/**
 * @override TouchPopup.js
 */
scout.TimePickerTouchPopup.prototype._initWidget = function(options) {
  this._widget = scout.create('TimePicker', {
    parent: this,
    timeResolution: options.timeResolution
  });
};

scout.TimePickerTouchPopup.prototype._render = function() {
  scout.TimePickerTouchPopup.parent.prototype._render.call(this);
  this._field.$container.addClass('time');
};
/**
 * @implements DatePickerPopup
 */
scout.TimePickerTouchPopup.prototype.getTimePicker = function() {
  return this._widget;
};

/**
 * @override
 */
scout.TimePickerTouchPopup.prototype._createLayout = function() {
  return new scout.TimePickerTouchPopupLayout(this);
};

scout.TimePickerTouchPopup.prototype._onFieldAcceptInput = function(event) {
  // Delegate to original field
  this._touchField.setDisplayText(event.displayText);
  this._touchField.setErrorStatus(event.errorStatus);
  if (!event.errorStatus) {
    this._touchField.setValue(event.value);
  }
  this._touchField._triggerAcceptInput();
};
