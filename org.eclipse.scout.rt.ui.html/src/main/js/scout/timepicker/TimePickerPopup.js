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
scout.TimePickerPopup = function() {
  scout.TimePickerPopup.parent.call(this);
};
scout.inherits(scout.TimePickerPopup, scout.Popup);

scout.TimePickerPopup.prototype._init = function(options) {
  options.scrollType = options.scrollType || 'layoutAndPosition';
  options.withFocusContext = false;
  scout.TimePickerPopup.parent.prototype._init.call(this, options);

  this.picker = scout.create('TimePicker', {
    parent: this,
    timeResolution : options.timeResolution
  });
};

scout.TimePickerPopup.prototype._createLayout = function() {
  return new scout.TimePickerPopupLayout(this);
};

scout.TimePickerPopup.prototype._render = function() {
  this.$container = this.$parent.appendDiv('time-picker-popup');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(this._createLayout());
  this.htmlComp.validateRoot = true;
  this.picker.render();
};

/**
 * @implements DatePickerPopup
 */
scout.TimePickerPopup.prototype.getTimePicker = function() {
  return this.picker;
};
