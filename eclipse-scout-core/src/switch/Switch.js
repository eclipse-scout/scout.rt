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
import {Event, Widget} from '../index';

export default class Switch extends Widget {

  constructor() {
    super();

    this.activated = false;
    this.label = null;
    this.htmlEnabled = false;

    this.$label = null;
    this.$button = null;
  }

  _render() {
    this.$container = this.$parent.appendDiv('switch');
    this.$label = this.$container.appendDiv('switch-label');
    this.$button = this.$container.appendDiv('switch-button')
      .on('click', this._onSwitchButtonClick.bind(this));
  }

  _renderProperties() {
    super._renderProperties();
    this._renderActivated();
    this._renderLabel();
  }

  _onSwitchButtonClick() {
    var event = new Event();
    this.trigger('switch', event);
    if (!event.defaultPrevented) {
      this.setActivated(!this.activated);
    }
  }

  setLabel(label) {
    this.setProperty('label', label);
  }

  _renderLabel() {
    if (this.htmlEnabled) {
      this.$label.html(this.label);
    } else {
      this.$label.text(this.label);
    }
  }

  setActivated(activated) {
    this.setProperty('activated', activated);
  }

  _renderActivated() {
    this.$button.toggleClass('activated', this.activated);
  }
}
