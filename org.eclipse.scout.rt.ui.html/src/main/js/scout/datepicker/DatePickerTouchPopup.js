/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.DatePickerTouchPopup = function() {
  scout.DatePickerTouchPopup.parent.call(this);
};
scout.inherits(scout.DatePickerTouchPopup, scout.TouchPopup);

scout.DatePickerTouchPopup.prototype._init = function(options) {
  scout.DatePickerTouchPopup.parent.prototype._init.call(this, options);
  this._field.on('displayTextChanged', this._onFieldDisplayTextChanged.bind(this));
  this._field.on('timestampChanged', this._onFieldTimestampChanged.bind(this));
};

/**
 * @override TouchPopup.js
 */
scout.DatePickerTouchPopup.prototype._initWidget = function(options) {
  this._widget = scout.create('DatePicker', {
    parent: this,
    dateFormat: options.dateFormat
  });
};

/**
 * @implements DatePickerPopup
 */
scout.DatePickerTouchPopup.prototype.getDatePicker = function() {
  return this._widget;
};

scout.DatePickerTouchPopup.prototype._onFieldDisplayTextChanged = function(event) {
  // Delegate to original field
  this._touchField.dateDisplayText = this._field.dateDisplayText;
  this._touchField.timeDisplayText = this._field.timeDisplayText;
  this._touchField.setDisplayText(event.displayText);
};

scout.DatePickerTouchPopup.prototype._onFieldTimestampChanged = function(event) {
  // Delegate to original field
  this._touchField.timestamp = this._field.timestamp;
  this._touchField.timestampAsDate = this._field.timestampAsDate;
};
