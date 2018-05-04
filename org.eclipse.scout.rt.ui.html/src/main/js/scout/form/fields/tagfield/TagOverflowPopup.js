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
scout.TagOverflowPopup = function() {
  scout.TagOverflowPopup.parent.call(this); // FIXME [awe] move to tagbar folder

  this._tagBarPropertyChangeListener = null;

  // We need not only to return which element receives the initial focus
  // but we must also prepare this element so it can receive the focus
  this.initialFocus = function() {
    return scout.TagBar.firstTagElement(this.$body)
      .setTabbable(true)
      .addClass('focused')[0];
  };
};
scout.inherits(scout.TagOverflowPopup, scout.PopupWithHead);

scout.TagOverflowPopup.prototype._init = function(options) {
  scout.TagOverflowPopup.parent.prototype._init.call(this, options);
  this._tagBarPropertyChangeListener = this._onTagBarPropertyChange.bind(this);
  this.parent.on('propertyChange', this._tagBarPropertyChangeListener);
};

scout.TagOverflowPopup.prototype._initKeyStrokeContext = function() {
  scout.TagOverflowPopup.parent.prototype._initKeyStrokeContext.call(this);
  this.keyStrokeContext.registerKeyStroke([
    new scout.TagFieldNavigationKeyStroke(this), // FIXME [awe] check key strokes
    new scout.TagFieldDeleteKeyStroke(this)
  ]);
};

scout.TagOverflowPopup.prototype._destroy = function() {
  this.parent.off('propertyChange', this._tagBarPropertyChangeListener);
  scout.TagOverflowPopup.parent.prototype._destroy.call(this);
};

scout.TagOverflowPopup.prototype._render = function() {
  scout.TagOverflowPopup.parent.prototype._render.call(this);

  this.$body.addClass('tag-overflow-popup');
  this._renderTags();
};

scout.TagOverflowPopup.prototype._renderTags = function() {
  this.$body.empty();

  var tagBar = this.parent;
  var visibleTags = tagBar.visibleTags();
  var allTags = scout.arrays.ensure(tagBar.tags);
  allTags.forEach(function(tagText) {
    // only add tags that are currently in "overflow" and thus are not visible
    if (visibleTags.indexOf(tagText) === -1) {
      tagBar.appendTagElement(this.$body, tagText, this._onTagRemoveClick.bind(this));
    }
  }.bind(this));

  if (!this.rendering) {
    this.revalidateLayout();
  }
};

scout.TagOverflowPopup.prototype._renderHead = function() {
  scout.TagOverflowPopup.parent.prototype._renderHead.call(this);

  // FIXME [awe] ask c.gu why the border around the head looks a bit broken (1 pixel issue)
  this._copyCssClassToHead('overflow-icon');
  this.$head
    .removeClass('popup-head menu-item')
    .addClass('tag-overflow-popup-head');
};

scout.TagOverflowPopup.prototype._focusFirstTagElement = function() {
  scout.TagBar.focusFirstTagElement(this.$body);
};

scout.TagOverflowPopup.prototype._onTagRemoveClick = function(event) {
  this.parent._onTagRemoveClick(event);
};

scout.TagOverflowPopup.prototype._onTagBarPropertyChange = function(event) {
  if (event.propertyName === 'tags') {
    var allTags = scout.arrays.ensure(this.parent.tags);
    var visibleTags = this.parent.visibleTags();
    var numTags = allTags.length;
    // close popup when no more tags left or all tags are visible (=no overflow icon)
    if (numTags === 0 || numTags === visibleTags.length) {
      this.close();
    } else {
      this._renderTags();
      this._focusFirstTagElement();
    }
  }
};
