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
scout.DatePickerPopup = function() {
  scout.DatePickerPopup.parent.call(this);
};
scout.inherits(scout.DatePickerPopup, scout.Popup);

scout.DatePickerPopup.prototype._init = function(options) {
  options.scrollType = options.scrollType || 'layoutAndPosition';
  options.withFocusContext = false;
  scout.DatePickerPopup.parent.prototype._init.call(this, options);

  this.picker = scout.create('DatePicker', {
    parent: this,
    dateFormat: options.dateFormat,
    allowedDates: options.allowedDates
  });
};

scout.DatePickerPopup.prototype._createLayout = function() {
  return new scout.DatePickerPopupLayout(this);
};

scout.DatePickerPopup.prototype._render = function() {
  this.$container = this.$parent.appendDiv('date-picker-popup');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(this._createLayout());
  this.htmlComp.validateRoot = true;
  this.picker.render();
};

/**
 * @implements DatePickerPopup
 */
scout.DatePickerPopup.prototype.getDatePicker = function() {
  return this.picker;
};

/**
 * @override because the icon is not in the $anchor container.
 */
scout.DatePickerPopup.prototype._isMouseDownOnAnchor = function(event) {
  return this.field.$dateField.isOrHas(event.target) || this.field.$dateFieldIcon.isOrHas(event.target) || (this.field.$dateClearIcon && this.field.$dateClearIcon.isOrHas(event.target));
};
