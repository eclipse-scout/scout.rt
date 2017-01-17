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
scout.DesktopLogo = function() {
  scout.DesktopLogo.parent.call(this);
};
scout.inherits(scout.DesktopLogo, scout.Widget);

scout.DesktopLogo.prototype._init = function(model) {
  scout.DesktopLogo.parent.prototype._init.call(this, model);
  this.url = model.url;
};

scout.DesktopLogo.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('desktop-logo');
};

scout.DesktopLogo.prototype._renderProperties = function() {
  scout.DesktopLogo.parent.prototype._renderProperties.call(this);
  this._renderUrl();
};

scout.DesktopLogo.prototype._renderUrl = function() {
  this.$container.css('backgroundImage', 'url(' + this.url + ')');
};

scout.DesktopLogo.prototype.setUrl = function(url) {
  this.setProperty('url', url);
};
