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
scout.RadioButton = function() {
  scout.RadioButton.parent.call(this);

  this.gridDataHints.fillHorizontal = true;
  this.focusWhenSelected = true;
  this.buttonKeyStroke = new scout.RadioButtonKeyStroke(this, null);
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

  scout.fields.linkElementWithLabel(this.$radioButton, this.$buttonLabel);

  this.$fieldContainer.on('mousedown', this._onMouseDown.bind(this));

  scout.tooltips.installForEllipsis(this.$buttonLabel, {
    parent: this
  });

  this.addStatus();
  this.session.keyStrokeManager.installKeyStrokeContext(this.formKeyStrokeContext);
};

scout.RadioButton.prototype._remove = function() {
  scout.tooltips.uninstall(this.$buttonLabel);
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.formKeyStrokeContext);
  scout.RadioButton.parent.prototype._remove.call(this);
};

scout.RadioButton.prototype._onMouseDown = function(event) {
  var $icon = this.get$Icon();
  if (!this.enabledComputed || !scout.isOneOf(event.target, this.$radioButton[0], this.$buttonLabel[0], $icon[0])) {
    return;
  }
  this.select();
  if (this.focusWhenSelected && scout.isOneOf(event.target, this.$buttonLabel[0], $icon[0])) {
    this.focusAndPreventDefault(event);
  }
};

/**
 * Convenience for {@link #setSelected(true)}
 */
scout.RadioButton.prototype.select = function() {
  this.setSelected(true);
};

scout.RadioButton.prototype.setSelected = function(selected) {
  this.setProperty('selected', selected);
};

/**
 * @override Button.js
 */
scout.RadioButton.prototype.doAction = function(event) {
  if (!this.enabledComputed || !this.visible) {
    return false;
  }
  // Since RadioButton extends Button, doAction should do something useful because it may be called (and actually is by ButtonKeyStroke)
  this.select();
  return true;
};

scout.RadioButton.prototype.setTabbable = function(tabbable) {
  if (!this.rendered) {
    return;
  }
  this.$field.setTabbable(tabbable && !scout.device.supportsTouch());
};

scout.RadioButton.prototype.isTabbable = function() {
  return this.rendered && this.$field.isTabbable();
};

/**
 * @override
 */
scout.RadioButton.prototype._renderProperties = function() {
  scout.RadioButton.parent.prototype._renderProperties.call(this);
  this._renderSelected();
};

scout.RadioButton.prototype._renderSelected = function() {
  this.$fieldContainer.toggleClass('checked', this.selected);
  this.$field.toggleClass('checked', this.selected);
};

scout.RadioButton.prototype._renderIconId = function() {
  scout.RadioButton.parent.prototype._renderIconId.call(this);
  var $icon = this.get$Icon();
  if ($icon.length > 0) {
    $icon.insertAfter(this.$radioButton);
  }
};
