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
scout.ApplicationLogo = function() {
  scout.ApplicationLogo.parent.call(this);
};
scout.inherits(scout.ApplicationLogo, scout.Widget);

scout.ApplicationLogo.prototype._init = function(model) {
  scout.ApplicationLogo.parent.prototype._init.call(this, model);
  this.url = model.url;
};

scout.ApplicationLogo.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('application-logo');

  // in memory of the first one...
  this.$container.dblclick(function(event) {
    if (event.altKey && event.ctrlKey) {
      $(event.target).css('background', 'none');
      $(event.target).css('font-size', '9px');
      $(event.target).text('make software not war');
    }
  });
};

scout.ApplicationLogo.prototype._renderProperties = function() {
  this._renderUrl();
};

scout.ApplicationLogo.prototype._renderUrl = function() {
  this.$container.css('backgroundImage', 'url(' + this.url + ')');
};

scout.ApplicationLogo.prototype.setUrl = function(url) {
  this.url = url;
  if (this.rendered) {
    this._renderUrl();
  }
};
