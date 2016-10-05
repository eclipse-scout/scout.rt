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
scout.ImageField = function() {
  scout.ImageField.parent.call(this);
  this.scrollBarEnabled = false;
};
scout.inherits(scout.ImageField, scout.FormField);

scout.ImageField.prototype._render = function($parent) {
  this.addContainer($parent, 'image-field', new scout.ImageFieldLayout(this));
  this.addFieldContainer($parent.makeDiv());

  var $field = this.$fieldContainer.appendElement('<img>', 'image')
    .on('load', this._onImageLoad.bind(this))
    .on('error', this._onImageError.bind(this));

  this.addLabel();
  this.addField($field);
  this.addStatus();
};

scout.ImageField.prototype._renderProperties = function() {
  scout.ImageField.parent.prototype._renderProperties.call(this);
  this._renderImageUrl();
  this._renderAutoFit();
  this._renderScrollBarEnabled();
  this._renderDropType();
};

scout.ImageField.prototype._remove = function() {
  scout.scrollbars.uninstall(this.$fieldContainer, this.session);
  scout.ImageField.parent.prototype._remove.call(this);
};

scout.ImageField.prototype._installDragAndDropHandler = function(event) {
  if (this.dragAndDropHandler) {
    return;
  }
  // add drag and drop event listeners to field container, img field might be hidden (e.g. if no image has been set)
  this.dragAndDropHandler = this._createDragAndDropHandler();
  this.dragAndDropHandler.install(this.$fieldContainer);
};

scout.ImageField.prototype._renderImageUrl = function() {
  this.$field.attr('src', this.imageUrl);
  // Hide <img> when it has no content to suppress the browser's 'broken image' icon
  this.$field.toggleClass('empty', !this.imageUrl);
};

scout.ImageField.prototype._renderAutoFit = function() {
  this.$field.toggleClass('autofit', this.autoFit);
  scout.scrollbars.update(this.$fieldContainer);
};

scout.ImageField.prototype._renderScrollBarEnabled = function() {
  // Note: Inner alignment has to be updated _before_ installing the scrollbar, because the inner
  // alignment uses absolute positioning, which confuses the scrollbar calculations.
  this._updateInnerAlignment();

  if (this.scrollBarEnabled) {
    scout.scrollbars.install(this.$fieldContainer, {
      parent: this
    });
  } else {
    scout.scrollbars.uninstall(this.$fieldContainer, this.session);
  }
};

scout.ImageField.prototype._renderGridData = function() {
  this._updateInnerAlignment();
};

scout.ImageField.prototype._updateInnerAlignment = function() {
  // Enable inner alignment only when scrollbars are disabled
  this.updateInnerAlignment({
    useHorizontalAlignment: (!this.scrollBarEnabled),
    useVerticalAlignment: (!this.scrollBarEnabled)
  });
};

scout.ImageField.prototype._onImageLoad = function(event) {
  this.$field.removeClass('broken');
  scout.scrollbars.update(this.$fieldContainer);
  this.invalidateLayoutTree();
};

scout.ImageField.prototype._onImageError = function(event) {
  this.$field.addClass('empty broken');
  scout.scrollbars.update(this.$fieldContainer);
  this.invalidateLayoutTree();
};

scout.ImageField.prototype.setImageUrl = function(imageUrl) {
  this.setProperty('imageUrl', imageUrl);
};
