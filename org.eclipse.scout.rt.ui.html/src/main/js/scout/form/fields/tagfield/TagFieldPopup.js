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
};
scout.inherits(scout.TagFieldPopup, scout.PopupWithHead);

scout.TagFieldPopup.prototype._init = function(options) {
  options.focusableContainer = true; // In order to allow keyboard navigation, the popup must gain focus. Because menu-items are not focusable, make the container focusable instead.

  scout.TagFieldPopup.parent.prototype._init.call(this, options);
};

scout.TagFieldPopup.prototype._render = function() {
  scout.TagFieldPopup.parent.prototype._render.call(this);
  this._renderTags();
};

scout.TagFieldPopup.prototype._renderTags = function() {
  var tagField = this.parent;
  var tags = scout.arrays.ensure(tagField.value);
  tags.forEach(this._renderTagElement.bind(this));
};

scout.TagFieldPopup.prototype._renderTagElement = function(tag) {
  var $element = this.$body
    .appendDiv('tag-element')
    .text(tag);

  $element
    .appendSpan('tag-element-remove')
    .data('tag', tag)
    .on('click', this._onTagElementRemove.bind(this))
    .text('x');
};

scout.TagFieldPopup.prototype._onTagElementRemove = function(event) {
  this.parent._onTagElementRemove(event);
  this.close();
};

scout.TagFieldPopup.prototype._modifyBody = function() {
  this.$body.addClass('tagfield-popup');
};
