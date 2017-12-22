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

  this._tagFieldPropertyChangeListener = null;

  // We need not only to return which element receives the initial focus
  // but we must also prepare this element so it can receive the focus
  this.initialFocus = function() {
    return scout.TagField.firstTagElement(this.$body)
      .setTabbable(true)
      .addClass('focused')[0];
  };
};
scout.inherits(scout.TagFieldPopup, scout.PopupWithHead);

scout.TagFieldPopup.prototype._init = function(options) {
  scout.TagFieldPopup.parent.prototype._init.call(this, options);
  this._tagFieldPropertyChangeListener = this._onTagFieldPropertyChange.bind(this);
  this.parent.on('propertyChange', this._tagFieldPropertyChangeListener);
};

scout.TagFieldPopup.prototype._initKeyStrokeContext = function() {
  scout.TagFieldPopup.parent.prototype._initKeyStrokeContext.call(this);
  this.keyStrokeContext.registerKeyStroke([
    new scout.TagFieldNavigationKeyStroke(this),
    new scout.TagFieldDeleteKeyStroke(this)
  ]);
};

scout.TagFieldPopup.prototype._destroy = function() {
  this.parent.off('propertyChange', this._tagFieldPropertyChangeListener);
  scout.TagFieldPopup.parent.prototype._destroy.call(this);
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
  var visibleTags = tagField.visibleTags();
  var allTags = scout.arrays.ensure(tagField.value);
  allTags.forEach(function(tagText) {
    // only add tags that are currently in "overflow" and thus are not visible
    if (visibleTags.indexOf(tagText) === -1) {
      tagField._makeTagElement(this.$body, tagText, this._onTagRemoveClick.bind(this))
        .appendTo(this.$body);
    }
  }.bind(this));

  if (!this.rendering) {
    this.revalidateLayout();
  }
};

scout.TagFieldPopup.prototype._renderHead = function() {
  scout.TagFieldPopup.parent.prototype._renderHead.call(this);

  // FIXME [awe] ask c.gu why the border around the head looks a bit broken (1 pixel issue)
  this._copyCssClassToHead('overflow-icon');
  this.$head
    .removeClass('popup-head menu-item')
    .addClass('tagfield-popup-head');
};

scout.TagFieldPopup.prototype._focusFirstTagElement = function() {
  scout.TagField.focusFirstTagElement(this.$body);
};

scout.TagFieldPopup.prototype._onTagRemoveClick = function(event) {
  this.parent._onTagRemoveClick(event);
};

scout.TagFieldPopup.prototype._onTagFieldPropertyChange = function(event) {
  if (event.propertyName === 'value') {
    var allTags = scout.arrays.ensure(this.parent.value);
    var visibleTags = this.parent.visibleTags();
    var numTags = allTags.length;
    // close popup when no more tags left or all tags are visible (=no overflow icon)
    if (numTags === 0 || numTags === visibleTags.length) {
      this.close();
    } else {
      this._renderValue();
      this._focusFirstTagElement();
    }
  }
};
