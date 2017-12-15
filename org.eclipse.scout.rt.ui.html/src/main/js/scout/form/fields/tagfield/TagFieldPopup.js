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
scout.TagFieldPopup = function() {
  scout.TagFieldPopup.parent.call(this);

  // Make sure head won't be rendered, there is a css selector which is applied only if there is a head
  this._headVisible = false;
  this._tagFieldPropertyChangeListener = null;
};
scout.inherits(scout.TagFieldPopup, scout.PopupWithHead);

scout.TagFieldPopup.prototype._init = function(options) {
  options.focusableContainer = true; // In order to allow keyboard navigation, the popup must gain focus. Because menu-items are not focusable, make the container focusable instead.

  scout.TagFieldPopup.parent.prototype._init.call(this, options);

  this._tagFieldPropertyChangeListener = this._onTagFieldPropertyChange.bind(this);
  this.parent.on('propertyChange', this._tagFieldPropertyChangeListener);
};

scout.TagFieldPopup.prototype._destroy = function() {
  scout.TagFieldPopup.parent.prototype._destroy.call(this);
  this.parent.off('propertyChange', this._tagFieldPropertyChangeListener);
};

scout.TagFieldPopup.prototype._modifyBody = function() {
  this.$body.addClass('tagfield-popup');
};

scout.TagFieldPopup.prototype._render = function() {
  scout.TagFieldPopup.parent.prototype._render.call(this);
  this._renderValue();
};

scout.TagFieldPopup.prototype._renderValue = function() {
  this.$body.empty();
  var tagField = this.parent;
  var tags = scout.arrays.ensure(tagField.value);
  tags.forEach(function(tagText) {
    tagField._makeTagElement(this.$body, tagText, this._onTagRemoveClick.bind(this))
      .appendTo(this.$body);
  }.bind(this));

  if (!this.rendering) {
    this.revalidateLayout();
  }
};

scout.TagFieldPopup.prototype._onTagRemoveClick = function(event) {
  this.parent._onTagRemoveClick(event);
};

scout.TagFieldPopup.prototype._onTagFieldPropertyChange = function(event) {
  if (event.propertyName === 'value') {
    if (scout.arrays.empty(this.parent.value)) {
      this.close();
    } else {
      this._renderValue();
    }
  }
};
