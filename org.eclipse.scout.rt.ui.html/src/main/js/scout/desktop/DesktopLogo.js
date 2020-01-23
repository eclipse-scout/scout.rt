/*******************************************************************************
 * Copyright (c) 2014-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.DesktopLogo = function() {
  scout.DesktopLogo.parent.call(this);
  this.desktop = null;
  this.clickable = false;
  this._desktopPropertyChangeHandler = this._onDesktopPropertyChange.bind(this);
  this._clickHandler = this._onClick.bind(this);
};
scout.inherits(scout.DesktopLogo, scout.Widget);

scout.DesktopLogo.prototype._init = function(model) {
  scout.DesktopLogo.parent.prototype._init.call(this, model);
  if (this.desktop) {
    this.clickable = this.desktop.logoActionEnabled;
  }
  this.url = model.url;
};

scout.DesktopLogo.prototype._render = function() {
  this.$container = this.$parent.appendDiv('desktop-logo');
  if (this.desktop) {
    this.desktop.on('propertyChange', this._desktopPropertyChangeHandler);
  }
};

scout.DesktopLogo.prototype._renderProperties = function() {
  scout.DesktopLogo.parent.prototype._renderProperties.call(this);
  this._renderUrl();
  this._renderClickable();
};

scout.DesktopLogo.prototype._remove = function() {
  this.desktop.off('propertyChange', this._desktopPropertyChangeHandler);
  scout.DesktopLogo.parent.prototype._remove.call(this);
};

scout.DesktopLogo.prototype._renderUrl = function() {
  this.$container.css('backgroundImage', 'url(' + this.url + ')');
};

scout.DesktopLogo.prototype._renderClickable = function() {
  if (this.desktop) {
    this.$container.toggleClass('clickable', this.clickable);
    if (this.clickable) {
      this.$container.on('click', this._clickHandler);
    }
    else {
      this.$container.off('click', this._clickHandler);
    }
  }
};

scout.DesktopLogo.prototype.setUrl = function(url) {
  this.setProperty('url', url);
};

scout.DesktopLogo.prototype.setClickable = function(clickable) {
  this.setProperty('clickable', clickable);
};

scout.DesktopLogo.prototype._onDesktopPropertyChange = function(event) {
  if (event.propertyName === 'logoActionEnabled') {
    this.setClickable(event.newValue);
  }
};

scout.DesktopLogo.prototype._onClick = function(event) {
  if (this.desktop) {
    this.desktop.logoAction();
  }
};
