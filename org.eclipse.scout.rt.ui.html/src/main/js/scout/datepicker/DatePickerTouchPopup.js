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
  this._field.on('acceptInput', this._onFieldAcceptInput.bind(this));
};

scout.DatePickerTouchPopup.prototype._fieldOverrides = function() {
  var overrides = scout.DatePickerTouchPopup.parent.prototype._fieldOverrides.call(this);
  overrides.hasTime = false;
  return overrides;
};

/**
 * @override TouchPopup.js
 */
scout.DatePickerTouchPopup.prototype._initWidget = function(options) {
  this._widget = scout.create('DatePicker', {
    parent: this,
    dateFormat: options.dateFormat,
    allowedDates: options.allowedDates
  });
};
scout.DatePickerTouchPopup.prototype._render = function() {
  scout.DatePickerTouchPopup.parent.prototype._render.call(this);
  this._field.$container.addClass('date');
};

scout.DatePickerTouchPopup.prototype._onMouseDownOutside = function() {
  this._acceptInput();
};

/**
 * @implements DatePickerPopup
 */
scout.DatePickerTouchPopup.prototype.getDatePicker = function() {
  return this._widget;
};

/**
 * @override
 */
scout.DatePickerTouchPopup.prototype._createLayout = function() {
  return new scout.DatePickerTouchPopupLayout(this);
};

scout.DatePickerTouchPopup.prototype._onFieldAcceptInput = function(event) {
  // Delegate to original field
  this._touchField.setDisplayText(event.displayText);
  this._touchField.setErrorStatus(event.errorStatus);
  if (!event.errorStatus) {
    this._touchField.setValue(event.value);
  }
  this._touchField._triggerAcceptInput();
};
