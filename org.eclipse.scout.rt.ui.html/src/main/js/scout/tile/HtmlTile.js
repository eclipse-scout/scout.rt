/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.HtmlTile = function() {
  scout.HtmlTile.parent.call(this);
  this.content = null;
};
scout.inherits(scout.HtmlTile, scout.Tile);

scout.HtmlTile.prototype._render = function() {
  this.$container = this.$parent.appendDiv('html-tile');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
};

scout.HtmlTile.prototype._renderProperties = function() {
  scout.HtmlTile.parent.prototype._renderProperties.call(this);
  this._renderContent();
};

scout.HtmlTile.prototype.setContent = function(content) {
  this.setProperty('content', content);
};

scout.HtmlTile.prototype._renderContent = function() {
  if (!this.content) {
    this.$container.empty();
    return;
  }
  this.$container.html(this.content);

  // Add listener to images to update the layout when the images are loaded
  this.$container.find('img')
    .on('load', this._onImageLoad.bind(this))
    .on('error', this._onImageError.bind(this));

  this.invalidateLayoutTree();
};

scout.HtmlTile.prototype._onImageLoad = function(event) {
  this.invalidateLayoutTree();
};

scout.HtmlTile.prototype._onImageError = function(event) {
  this.invalidateLayoutTree();
};
