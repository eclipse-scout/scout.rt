/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
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
  this.autoFit = false;
  this.scrollBarEnabled = false;
};
scout.inherits(scout.ImageField, scout.FormField);

scout.ImageField.prototype._init = function(model) {
  scout.ImageField.parent.prototype._init.call(this, model);

  this.resolveIconIds(['imageUrl']);
  this.icon = scout.create('Icon', {
    parent: this,
    iconDesc: this.imageUrl,
    autoFit: this.autoFit,
    prepend: true
  });
  this.icon.on('load', this._onImageLoad.bind(this));
  this.icon.on('error', this._onImageError.bind(this));
};

scout.ImageField.prototype._render = function() {
  this.addContainer(this.$parent, 'image-field', new scout.ImageFieldLayout(this));
  this.addFieldContainer(this.$parent.makeDiv());

  // Complete the layout hierarchy between the image field and the image
  var htmlComp = scout.HtmlComponent.install(this.$fieldContainer, this.session);
  htmlComp.setLayout(new scout.SingleLayout());
  this.icon.render(this.$fieldContainer);

  this.addLabel();
  this.addField(this.icon.$container);
  this.addStatus();
};

scout.ImageField.prototype._renderProperties = function() {
  scout.ImageField.parent.prototype._renderProperties.call(this);
  this._renderScrollBarEnabled();
  this._renderDropType();
  this._renderImageUrl();
};

scout.ImageField.prototype._installDragAndDropHandler = function(event) {
  if (this.dragAndDropHandler) {
    return;
  }
  // add drag and drop event listeners to field container, img field might be hidden (e.g. if no image has been set)
  this.dragAndDropHandler = this._createDragAndDropHandler();
  this.dragAndDropHandler.install(this.$fieldContainer);
};

scout.ImageField.prototype.setImageUrl = function(imageUrl) {
  this.setProperty('imageUrl', imageUrl);
};

scout.ImageField.prototype._setImageUrl = function(imageUrl) {
  this._setProperty('imageUrl', imageUrl);
  this.icon.setIconDesc(imageUrl);
};

scout.ImageField.prototype._renderImageUrl = function() {
  var hasImageUrl = !!this.imageUrl;
  this.$fieldContainer.toggleClass('has-image', hasImageUrl);
  this.$container.toggleClass('has-image', hasImageUrl);
};

scout.ImageField.prototype.setAutoFit = function(autoFit) {
  this.setProperty('autoFit', autoFit);
};

scout.ImageField.prototype._setAutoFit = function(autoFit) {
  this._setProperty('autoFit', autoFit);
  this.icon.setAutoFit(autoFit);
};

scout.ImageField.prototype._renderAutoFit = function() {
  scout.scrollbars.update(this.$fieldContainer);
};

scout.ImageField.prototype._renderScrollBarEnabled = function() {
  // Note: Inner alignment has to be updated _before_ installing the scrollbar, because the inner
  // alignment uses absolute positioning, which confuses the scrollbar calculations.
  this._updateInnerAlignment();

  if (this.scrollBarEnabled) {
    this._installScrollbars();
  } else {
    this._uninstallScrollbars();
  }
};

/**
 * @override
 */
scout.ImageField.prototype.get$Scrollable = function() {
  return this.$fieldContainer;
};

scout.ImageField.prototype._renderGridData = function() {
  scout.ImageField.parent.prototype._renderGridData.call(this);
  this._updateInnerAlignment();
};

scout.ImageField.prototype._renderGridDataHints = function() {
  scout.ImageField.parent.prototype._renderGridDataHints.call(this);
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
  scout.scrollbars.update(this.$fieldContainer);
};

scout.ImageField.prototype._onImageError = function(event) {
  scout.scrollbars.update(this.$fieldContainer);
};
