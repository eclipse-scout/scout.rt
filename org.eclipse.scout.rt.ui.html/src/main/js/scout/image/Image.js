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
scout.Image = function() {
  scout.Image.parent.call(this);
  this.autoFit = false;
  this.imageUrl = null;
};
scout.inherits(scout.Image, scout.Widget);

scout.Image.prototype._render = function($parent) {
  this.$container = $parent.makeElement('<img>', 'image')
    .on('load', this._onImageLoad.bind(this))
    .on('error', this._onImageError.bind(this));

  if (scout.nvl(this.prepend, true)) {
    this.$container.prependTo($parent);
  } else {
    this.$container.appendTo($parent);
  }

  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.ImageLayout(this));
  this.htmlComp.pixelBasedSizing = false;
};

scout.Image.prototype._renderProperties = function() {
  scout.Image.parent.prototype._renderProperties.call(this);
  this._renderImageUrl();
  this._renderAutoFit();
};

scout.Image.prototype._remove = function() {
  scout.Image.parent.prototype._remove.call(this);
  this.htmlComp = null;
};

scout.Image.prototype.setImageUrl = function(imageUrl) {
  this.setProperty('imageUrl', imageUrl);
};

scout.Image.prototype._renderImageUrl = function() {
  this.$container.attr('src', this.imageUrl);

  // Hide <img> when it has no content (event 'load' will not fire)
  if (!this.imageUrl) {
    this.$container.addClass('empty').removeClass('broken');
  }
};

scout.Image.prototype.setAutoFit = function(autoFit) {
  this.setProperty('autoFit', autoFit);
};

scout.Image.prototype._renderAutoFit = function() {
  this.$container.toggleClass('autofit', this.autoFit);
};

scout.Image.prototype._onImageLoad = function(event) {
  if (!this.rendered) { // check needed, because this is an async callback
    return;
  }
  this.$container.removeClass('empty broken');
  this.invalidateLayoutTree();
  this.trigger('load');
};

scout.Image.prototype._onImageError = function(event) {
  if (!this.rendered) { // check needed, because this is an async callback
    return;
  }
  this.$container.addClass('empty broken');
  this.invalidateLayoutTree();
  this.trigger('error');
};
