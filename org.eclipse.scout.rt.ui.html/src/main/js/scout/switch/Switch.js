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
import {Event} from '../index';
import {Widget} from '../index';

export default class Switch extends Widget {

constructor() {
  super();

  this.activated = false;
  this.switchLabel = null;
  this.htmlEnabled = false;

  this.$switchLabel = null;
  this.$switchButton = null;
}


_render() {
  this.$container = this.$parent.appendDiv('switch');
  this.$switchLabel = this.$container.appendDiv('switch-label');
  this.$switchButton = this.$container.appendDiv('switch-button')
    .on('click', this._onSwitchButtonClick.bind(this));
}

_renderProperties() {
  super._renderProperties();
  this._renderActivated();
  this._renderSwitchLabel();
}

_onSwitchButtonClick() {
  var event = new Event();
  this.trigger('switch', event);
  if (!event.defaultPrevented) {
    this.setActivated(!this.activated);
  }
}

setSwitchLabel(switchLabel) {
  this.setProperty('switchLabel', switchLabel);
}

_renderSwitchLabel() {
  if (this.htmlEnabled) {
    this.$switchLabel.html(this.switchLabel);
  } else {
    this.$switchLabel.text(this.switchLabel);
  }
}

setActivated(activated) {
  this.setProperty('activated', activated);
}

_renderActivated() {
  this.$switchButton.toggleClass('activated', this.activated);
}
}
