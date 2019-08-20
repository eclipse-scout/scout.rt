/*
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.Switch = function() {
  scout.Switch.parent.call(this);

  this.activated = false;
  this.switchLabel = null;
  this.htmlEnabled = false;

  this.$switchLabel = null;
  this.$switchButton = null;
};
scout.inherits(scout.Switch, scout.Widget);

scout.Switch.prototype._render = function() {
  this.$container = this.$parent.appendDiv('switch');
  this.$switchLabel = this.$container.appendDiv('switch-label');
  this.$switchButton = this.$container.appendDiv('switch-button')
    .on('click', this._onSwitchButtonClick.bind(this));
};

scout.Switch.prototype._renderProperties = function() {
  scout.Switch.parent.prototype._renderProperties.call(this);
  this._renderActivated();
  this._renderSwitchLabel();
};

scout.Switch.prototype._onSwitchButtonClick = function() {
  var event = new scout.Event();
  this.trigger('switch', event);
  if (!event.defaultPrevented) {
    this.setActivated(!this.activated);
  }
};

scout.Switch.prototype.setSwitchLabel = function(switchLabel) {
  this.setProperty('switchLabel', switchLabel);
};

scout.Switch.prototype._renderSwitchLabel = function() {
  if (this.htmlEnabled) {
    this.$switchLabel.html(this.switchLabel);
  } else {
    this.$switchLabel.text(this.switchLabel);
  }
};

scout.Switch.prototype.setActivated = function(activated) {
  this.setProperty('activated', activated);
};

scout.Switch.prototype._renderActivated = function() {
  this.$switchButton.toggleClass('activated', this.activated);
};
