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
scout.TriStateField = function() {
  scout.TriStateField.parent.call(this);
  this.$checkBox;
  this.$checkBoxLabel;
};
scout.inherits(scout.TriStateField, scout.ValueField);

/**
 * The value of the TriStateField widget is a string: 'false', 'true', 'undefined'
 * @override
 */
scout.TriStateField.prototype._initKeyStrokeContext = function() {
  scout.TriStateField.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke(new scout.TriStateFieldToggleKeyStroke(this));
};

scout.TriStateField.prototype._render = function($parent) {
  this.addContainer($parent, 'check-box-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addField($parent.makeDiv());

  this.$checkBox = this.$field
    .appendDiv('check-box')
    .on('mousedown', this._onMouseDown.bind(this))
    .data('valuefield', this);

  this.$checkBoxLabel = this.$field
    .appendDiv('label')
    .on('mousedown', this._onMouseDown.bind(this));

  scout.tooltips.installForEllipsis(this.$checkBoxLabel, {
    parent: this
  });

  this.addStatus();
};

scout.TriStateField.prototype._remove = function() {
  scout.tooltips.uninstall(this.$checkBoxLabel);
  scout.TriStateField.parent.prototype._remove.call(this);
};

scout.TriStateField.prototype.acceptInput = function(whileTyping, forceSend) {
  //nop
};

scout.TriStateField.prototype._renderDisplayText = function() {
  //nop
};

scout.TriStateField.prototype._onMouseDown = function(event) {
  this.toggleValue();
  if (event.currentTarget === this.$checkBoxLabel[0]) {
    this.session.focusManager.requestFocus(this.$checkBox);
  }
};

scout.TriStateField.prototype.toggleValue = function() {
  if (!this.enabled) {
    return;
  }
  if(this.value===false){
    this.setValue(true);
  }
  else if(this.value===true){
    this.setValue('');
  }
  else{
    this.setValue(false);
  }
};

scout.TriStateField.prototype.setValue = function(value) {
  this.setProperty('value', value);
};

/**
 * @override
 */
scout.TriStateField.prototype._renderEnabled = function() {
  scout.TriStateField.parent.prototype._renderEnabled.call(this);
  this.$checkBox
    .setTabbable(this.enabled && !scout.device.supportsTouch())
    .setEnabled(this.enabled);
};

scout.TriStateField.prototype._renderProperties = function() {
  scout.TriStateField.parent.prototype._renderProperties.call(this);
  this._renderValue(this.value);
};

scout.TriStateField.prototype._renderValue = function() {
  this.$checkBox.toggleClass('checked', this.value===true);
  this.$checkBox.toggleClass('tristate', this.value!==true && this.value!==false);
};

/**
 * @override
 */
scout.TriStateField.prototype._renderLabel = function() {
  this.$checkBoxLabel.textOrNbsp(this.label, 'empty');
  // Make sure the empty label is as height as the other labels, especially important for top labels
  this.$label.html('&nbsp;');
};

scout.TriStateField.prototype._renderGridData = function() {
  this.updateInnerAlignment({
    useHorizontalAlignment: true
  });
};
