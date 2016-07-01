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
scout.RadioButton = function() {
  scout.RadioButton.parent.call(this);

  this.focusWhenSelected;
};
scout.inherits(scout.RadioButton, scout.Button);

/**
 * @override Button.js
 */
scout.RadioButton.prototype._initDefaultKeyStrokes = function(keyStrokeContext) {
  keyStrokeContext.registerKeyStroke([
    new scout.RadioButtonKeyStroke(this, 'ENTER'),
    new scout.RadioButtonKeyStroke(this, 'SPACE')
  ]);
};

scout.RadioButton.prototype._init = function(model) {
  scout.RadioButton.parent.prototype._init.call(this, model);
  this.focusWhenSelected = scout.nvl(model.focusWhenSelected, !scout.device.supportsFocusEmptyBeforeDiv());
};

scout.RadioButton.prototype._render = function($parent) {
  this.addContainer($parent, 'radio-button', new scout.ButtonLayout(this));
  this.addField($parent.makeDiv()
    .on('mousedown', this._mouseDown.bind(this)));
  this.$field.data('radiobutton', this);
  this.addStatus();

  scout.tooltips.installForEllipsis(this.$field, {
    parent: this
  });
};

scout.RadioButton.prototype._remove = function($parent) {
  scout.tooltips.uninstall(this.$field);
  scout.RadioButton.parent.prototype._remove.call(this);
};

scout.RadioButton.prototype._mouseDown = function(event) {
  this.select();
  if (this.focusWhenSelected) {
    this.session.focusManager.requestFocus(this.$field);
    event.preventDefault();
  }
};

scout.RadioButton.prototype.select = function() {
  if (!this.enabled) {
    return;
  }
  if (this.parent instanceof scout.RadioButtonGroup) {
    this.parent.selectButton(this);
  } else {
    this.setSelected(true);
  }
};

scout.RadioButton.prototype.setSelected = function(selected) {
  if (selected === this.selected) {
    return;
  }
  this._setProperty('selected', selected);
  this._sendProperty('selected');
  if (this.rendered) {
    this._renderSelected();
  }
};

/**
 * @override Button.js
 */
scout.RadioButton.prototype.doAction = function(event) {
  // Since RadioButton extends Button, doAction should do something useful because it may be called (and actually is by ButtonKeyStroke)
  this.select();
  return true;
};

scout.RadioButton.prototype.setTabbable = function(tabbable) {
  this.$field.setTabbable(tabbable);
};

/**
 * @override
 */
scout.RadioButton.prototype._renderProperties = function() {
  scout.RadioButton.parent.prototype._renderProperties.call(this);
  this._renderSelected();
};

/**
 * @override
 */
scout.RadioButton.prototype._renderLabel = function() {
  this.$field.textOrNbsp(scout.strings.removeAmpersand(this.label));
};

scout.RadioButton.prototype._renderSelected = function() {
  this.$field.toggleClass('checked', this.selected);
};
