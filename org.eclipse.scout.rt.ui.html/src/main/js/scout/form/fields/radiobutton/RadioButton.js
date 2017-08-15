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

  this.gridDataHints.fillHorizontal = true;
};
scout.inherits(scout.RadioButton, scout.Button);

/**
 * @override Button.js
 */
scout.RadioButton.prototype._initDefaultKeyStrokes = function() {
  this.keyStrokeContext.registerKeyStroke([
    new scout.RadioButtonKeyStroke(this, 'ENTER'),
    new scout.RadioButtonKeyStroke(this, 'SPACE')
  ]);
};

scout.RadioButton.prototype._render = function() {
  this.addContainer(this.$parent, 'radio-button', new scout.RadioButtonLayout(this));
  this.addFieldContainer(this.$parent.makeDiv());
  this.$radioButton = this.$fieldContainer
    .appendDiv('radio-button-circle')
    .data('radiobutton', this);
  this.addField(this.$radioButton);

  // $buttonLabel is used by Button.js as well -> Button.js handles label
  this.$buttonLabel = this.$fieldContainer
    .appendDiv('label');

  this.$fieldContainer.on('mousedown', this._onMouseDown.bind(this));

  scout.tooltips.installForEllipsis(this.$buttonLabel, {
    parent: this
  });

  this.addStatus();
};

scout.RadioButton.prototype._remove = function() {
  scout.tooltips.uninstall(this.$buttonLabel);
  scout.RadioButton.parent.prototype._remove.call(this);
};

scout.RadioButton.prototype._onMouseDown = function(event) {
  var $icon = this.get$Icon();
  if (!this.enabledComputed || !scout.isOneOf(event.target, this.$radioButton[0], this.$buttonLabel[0], $icon[0])) {
    return;
  }
  this.select();
  if (scout.isOneOf(event.target, this.$buttonLabel[0], $icon[0])) {
    this.focus();
  }
};

scout.RadioButton.prototype.select = function() {
  if (!this.enabledComputed) {
    return;
  }
  if (this.parent instanceof scout.RadioButtonGroup) {
    this.parent.selectButton(this);
  } else {
    this.setSelected(true);
  }
};

scout.RadioButton.prototype.setSelected = function(selected) {
  this.setProperty('selected', selected);
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
  this.$field.setTabbable(tabbable && !scout.device.supportsTouch());
};

/**
 * @override
 */
scout.RadioButton.prototype._renderProperties = function() {
  scout.RadioButton.parent.prototype._renderProperties.call(this);
  this._renderSelected();
};

scout.RadioButton.prototype._renderSelected = function() {
  this.$field.toggleClass('checked', this.selected);
};

scout.RadioButton.prototype._renderIconId = function() {
  scout.RadioButton.parent.prototype._renderIconId.call(this);
  var $icon = this.get$Icon();
  if ($icon.length > 0) {
    $icon.insertAfter(this.$radioButton);
  }
};
