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

scout.DatePickerPopup.prototype._render = function($parent) {
  this.picker.render($parent);
  this.$container = this.picker.$container;
  this.$container.addClass('date-picker-popup');
  this.htmlComp = this.picker.htmlComp;
  this.htmlComp.validateRoot = true;
};

/**
 * @implements DatePickerPopup
 */
scout.DatePickerPopup.prototype.getDatePicker = function() {
  return this.picker;
};
