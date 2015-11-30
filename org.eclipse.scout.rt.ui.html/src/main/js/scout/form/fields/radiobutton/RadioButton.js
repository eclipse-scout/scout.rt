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
};
scout.inherits(scout.RadioButton, scout.Button);

scout.RadioButton.prototype._render = function($parent) {
  this.addContainer($parent, 'radio-button', new scout.ButtonLayout(this));
  this.addField($parent.makeDiv()
    .attr('value', this.radioValue)
    .on('mousedown', this._mouseDown.bind(this)));
  this.addStatus();

  scout.tooltips.installForEllipsis(this.$field, {
    parent: this
  });
};

scout.RadioButton.prototype._remove = function($parent) {
  scout.tooltips.uninstall(this.$field);
  scout.RadioButton.parent.prototype._remove.call(this);
};

scout.RadioButton.prototype._mouseDown = function() {
  this._toggleChecked();
};

scout.RadioButton.prototype._toggleChecked = function() {
  if (!this.enabled) {
    return;
  }
  if (this.parent instanceof scout.RadioButtonGroup) {
    this.parent.setNewSelection(this);
  } else {
    this.selected = true;
    this.$field.toggleClass('checked', true);
    this._send('selected');
    this.$field.focus();
  }
};

/**
 * @override
 */
scout.RadioButton.prototype._renderProperties = function() {
  scout.RadioButton.parent.prototype._renderProperties.call(this);
  this._renderSelected(this.selected);
};

/**
 * @override
 */
scout.RadioButton.prototype._renderLabel = function() {
  if (this.$field) {
    this.$field.textOrNbsp(scout.strings.removeAmpersand(this.label));
  }

};

scout.RadioButton.prototype._renderRadioValue = function(radioValue) {
  this.$field.attr('value', radioValue);
};

scout.RadioButton.prototype._renderTabbable = function(tabbable) {
  if (tabbable) {
    this.$field.attr('tabindex', '0');
  } else {
    this.$field.removeAttr('tabindex');
  }
};

scout.RadioButton.prototype._handleTabIndex = function() {
  if (this.parent instanceof scout.RadioButtonGroup) {
    return;
  }
  this._renderTabbable(this.enabled);
};

scout.RadioButton.prototype._renderSelected = function(selected) {
  this.$field.toggleClass('checked', selected);
};
