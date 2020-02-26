/*
 * Copyright (c) 2014-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Widget} from '../index';

export default class DesktopLogo extends Widget {

  constructor() {
    super();
    this.desktop = null;
    this.clickable = false;
    this._desktopPropertyChangeHandler = this._onDesktopPropertyChange.bind(this);
    this._clickHandler = this._onClick.bind(this);
  }

  _init(model) {
    super._init(model);
    this.desktop = this.session.desktop;
    this.clickable = this.desktop.logoActionEnabled;
    this.url = model.url;
  }

  _render() {
    this.$container = this.$parent.appendDiv('desktop-logo');
    this.desktop.on('propertyChange', this._desktopPropertyChangeHandler);
  }

  _renderProperties() {
    super._renderProperties();
    this._renderUrl();
    this._renderClickable();
  }

  _remove() {
    this.desktop.off('propertyChange', this._desktopPropertyChangeHandler);
    super._remove();
  }

  _renderUrl() {
    this.$container.css('backgroundImage', 'url(' + this.url + ')');
  }

  _renderClickable() {
    this.$container.toggleClass('clickable', this.clickable);
    if (this.clickable) {
      this.$container.on('click', this._clickHandler);
    } else {
      this.$container.off('click', this._clickHandler);
    }
  }

  setUrl(url) {
    this.setProperty('url', url);
  }

  setClickable(clickable) {
    this.setProperty('clickable', clickable);
  }

  _onDesktopPropertyChange(event) {
    if (event.propertyName === 'logoActionEnabled') {
      this.setClickable(event.newValue);
    }
  }

  _onClick(event) {
    this.desktop.logoAction();
  }
}
