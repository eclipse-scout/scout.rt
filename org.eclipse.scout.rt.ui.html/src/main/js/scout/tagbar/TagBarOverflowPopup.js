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
scout.TagBarOverflowPopup = function() {
  scout.TagBarOverflowPopup.parent.call(this);

  this._tagBarPropertyChangeListener = null;

  // We need not only to return which element receives the initial focus
  // but we must also prepare this element so it can receive the focus
  this.initialFocus = function() {
    return scout.TagBar.firstTagElement(this.$body)
      .setTabbable(true)
      .addClass('focused')[0];
  };
};
scout.inherits(scout.TagBarOverflowPopup, scout.PopupWithHead);

scout.TagBarOverflowPopup.prototype._init = function(options) {
  scout.TagBarOverflowPopup.parent.prototype._init.call(this, options);
  this._tagBarPropertyChangeListener = this._onTagBarPropertyChange.bind(this);
  this.parent.on('propertyChange', this._tagBarPropertyChangeListener);
};

scout.TagBarOverflowPopup.prototype._initKeyStrokeContext = function() {
  scout.TagBarOverflowPopup.parent.prototype._initKeyStrokeContext.call(this);
  this.keyStrokeContext.registerKeyStroke([
    new scout.TagFieldNavigationKeyStroke(this), // FIXME [awe] check key strokes
    new scout.TagFieldDeleteKeyStroke(this)
  ]);
};

scout.TagBarOverflowPopup.prototype._destroy = function() {
  this.parent.off('propertyChange', this._tagBarPropertyChangeListener);
  scout.TagBarOverflowPopup.parent.prototype._destroy.call(this);
};

scout.TagBarOverflowPopup.prototype._render = function() {
  scout.TagBarOverflowPopup.parent.prototype._render.call(this);

  this.$body.addClass('tag-overflow-popup');
  this._renderTags();
};

scout.TagBarOverflowPopup.prototype._renderTags = function() {
  var tagBar = this.parent;
  var visibleTags = tagBar.visibleTags();
  var allTags = scout.arrays.ensure(tagBar.tags);
  var overflowTags = allTags.filter(function(tagText) {
    return visibleTags.indexOf(tagText) === -1;
  });

  var clickHandler = tagBar._onTagClick.bind(tagBar);
  var removeHandler = tagBar._onTagRemoveClick.bind(tagBar);
  scout.TagBar.renderTags(this.$body, overflowTags, tagBar.enabledComputed, clickHandler, removeHandler);
  // FIXME [awe] flag machen, um die eigenschaften vom original zu übernehmen? popup schliessen nach klick

  if (!this.rendering) {
    this.revalidateLayout();
  }

};

scout.TagBarOverflowPopup.prototype._renderHead = function() {
  scout.TagBarOverflowPopup.parent.prototype._renderHead.call(this);

  this._copyCssClassToHead('overflow-icon');
  this.$head
    .removeClass('popup-head menu-item')
    .addClass('tag-overflow-popup-head');
};

scout.TagBarOverflowPopup.prototype._focusFirstTagElement = function() {
  scout.TagBar.focusFirstTagElement(this.$body);
};

scout.TagBarOverflowPopup.prototype._onTagRemoveClick = function(event) {
  this.parent._onTagRemoveClick(event);
};

scout.TagBarOverflowPopup.prototype._onTagBarPropertyChange = function(event) {
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
