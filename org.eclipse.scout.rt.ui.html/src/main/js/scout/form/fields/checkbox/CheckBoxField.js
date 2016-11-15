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
scout.CheckBoxField = function() {
  scout.CheckBoxField.parent.call(this);
  this.triStateEnabled = false;
  this.$checkBox;
  this.$checkBoxLabel;
};
scout.inherits(scout.CheckBoxField, scout.ValueField);

/**
 * The value of the CheckBoxField widget is: false, true (and null in tri-state mode)
 * @override
 */
scout.CheckBoxField.prototype._initKeyStrokeContext = function() {
  scout.CheckBoxField.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke(new scout.CheckBoxToggleKeyStroke(this));
};

scout.CheckBoxField.prototype._render = function($parent) {
  this.addContainer($parent, 'check-box-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addFieldContainer($parent.makeDiv());

  this.$checkBox = this.$fieldContainer
    .appendDiv('check-box')
    .on('mousedown', this._onMouseDown.bind(this))
    .data('valuefield', this);
  this.addField(this.$checkBox);

  this.$checkBoxLabel = this.$fieldContainer
    .appendDiv('label')
    .on('mousedown', this._onMouseDown.bind(this));

  scout.tooltips.installForEllipsis(this.$checkBoxLabel, {
    parent: this
  });

  this.addStatus();
};

scout.CheckBoxField.prototype._remove = function() {
  scout.tooltips.uninstall(this.$checkBoxLabel);
  scout.CheckBoxField.parent.prototype._remove.call(this);
};

scout.CheckBoxField.prototype.acceptInput = function(whileTyping, forceSend) {
  //nop
};

scout.CheckBoxField.prototype._renderDisplayText = function() {
  //nop
};

scout.CheckBoxField.prototype._onMouseDown = function(event) {
  this.toggleChecked();
  if (event.currentTarget === this.$checkBoxLabel[0]) {
    this.session.focusManager.requestFocus(this.$checkBox);
  }
};

scout.CheckBoxField.prototype.toggleChecked = function() {
  if (!this.enabledComputed) {
    return;
  }
  if (this.triStateEnabled) {
    if (this.value === false) {
      this.setValue(true);
    } else if (this.value === true) {
      this.setValue(null);
    } else {
      this.setValue(false);
    }
  } else {
    this.setValue(!this.value);
  }
};

scout.CheckBoxField.prototype.setValue = function(value) {
  this.setProperty('value', value);
};

/**
 * @override
 */
scout.CheckBoxField.prototype._renderEnabled = function() {
  scout.CheckBoxField.parent.prototype._renderEnabled.call(this);
  this.$checkBox
    .setTabbable(this.enabledComputed && !scout.device.supportsTouch())
    .setEnabled(this.enabledComputed);
};

scout.CheckBoxField.prototype._renderProperties = function() {
  scout.CheckBoxField.parent.prototype._renderProperties.call(this);
  this._renderValue();
};

scout.CheckBoxField.prototype._renderValue = function() {
  this.$checkBox.toggleClass('checked', this.value === true);
  this.$checkBox.toggleClass('undefined', this.triStateEnabled && this.value !== true && this.value !== false);
};

/**
 * @override
 */
scout.CheckBoxField.prototype._renderLabel = function() {
  this.$checkBoxLabel.textOrNbsp(this.label, 'empty');
  // Make sure the empty label is as height as the other labels, especially important for top labels
  this.$label.html('&nbsp;');
};

/**
 * @override
 */
scout.CheckBoxField.prototype._renderFont = function() {
  scout.styles.legacyStyle(this, this.$fieldContainer);
};

/**
 * @override
 */
scout.CheckBoxField.prototype._renderForegroundColor = function() {
  scout.styles.legacyStyle(this, this.$fieldContainer);
};

/**
 * @override
 */
scout.CheckBoxField.prototype._renderBackgroundColor = function() {
  scout.styles.legacyStyle(this, this.$fieldContainer);
};

scout.CheckBoxField.prototype._renderGridData = function() {
  scout.CheckBoxField.parent.prototype._renderGridData.call(this);
  this.updateInnerAlignment({
    useHorizontalAlignment: true
  });
};

scout.CheckBoxField.prototype.prepareForCellEdit = function(opts) {
  scout.CheckBoxField.parent.prototype.prepareForCellEdit.call(this, opts);
  this.$checkBoxLabel.hide();
};
