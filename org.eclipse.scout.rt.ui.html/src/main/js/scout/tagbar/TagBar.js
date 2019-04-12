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

scout.TagBar = function() {
  scout.TagBar.parent.call(this);

  this.overflowEnabled = true;
  this.$overflowIcon = null;
  this.overflowVisible = false;
  this.overflow = null;
  this.tags = [];
  /**
   * Whether or not the tag elements are clickable (even when TagBar is disabled).
   * When the tag elements are clickable a click handler is registered and
   * a pointer cursor appears when hovering over the element.
   */
  this.clickable = false;
};
scout.inherits(scout.TagBar, scout.Widget);

scout.TagBar.prototype._render = function() {
  this.$container = this.$parent.appendDiv('tag-bar');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.TagBarLayout(this));
  this._installTooltipSupport();
};

scout.TagBar.prototype._renderProperties = function() {
  scout.TagBar.parent.prototype._renderProperties.call(this);
  this._renderTags();
  this._renderOverflowVisible();
};

scout.TagBar.prototype._remove = function() {
  this.$overflowIcon = null;
  this.overflowVisible = false;
  this.closeOverflowPopup();
  this._uninstallTooltipSupport();
  scout.TagBar.parent.prototype._remove.call(this);
};

scout.TagBar.prototype.setTags = function(tags) {
  this.setProperty('tags', tags);
};

scout.TagBar.prototype.updateTags = function() {
  if (this.rendered) {
    this._renderTags();
  }
};

/**
 * This function is also used by sub- and friend-classes like the TagOverflowPopup.
 */
scout.TagBar.prototype._renderTags = function() {
  var tags = scout.arrays.ensure(this.tags);
  var clickHandler = this.clickable ? this._onTagClick.bind(this) : null;
  var removeHandler = this._onTagRemoveClick.bind(this);
  scout.TagBar.renderTags(this.$container, tags, this.enabledComputed, clickHandler, removeHandler);
  this.invalidateLayoutTree();
};

scout.TagBar.prototype._onTagClick = function(event) {
  var tag = scout.TagBar.getTagData($(event.currentTarget));
  this._triggerTagClick(tag);
  return false;
};

scout.TagBar.prototype._triggerTagClick = function(tag) {
  this.trigger('tagClick', {
    tag: tag
  });
};

scout.TagBar.prototype._onTagRemoveClick = function(event) {
  if (this.enabledComputed) {
    this.removeTagByElement($(event.currentTarget));
  }
  return false;
};

scout.TagBar.prototype.removeTagByElement = function($tag) {
  var tag = scout.TagBar.getTagData($tag);
  if (tag) {
    this._triggerTagRemove(tag, $tag);
  }
};

scout.TagBar.prototype._triggerTagRemove = function(tag, $tag) {
  this.trigger('tagRemove', {
    tag: tag,
    $tag: $tag
  });
};

scout.TagBar.prototype._onOverflowIconMousedown = function(event) {
  this.openOverflowPopup();
  return false;
};

scout.TagBar.prototype.isOverflowIconFocused = function() {
  if (!this.$overflowIcon) {
    return false;
  }
  var ae = this.$container.activeElement();
  return this.$overflowIcon.is(ae);
};

scout.TagBar.prototype.openOverflowPopup = function() {
  if (this.overflow) {
    return;
  }
  this.overflow = this._createOverflowPopup();
  this.overflow.on('close', this._onOverflowPopupClose.bind(this));
  this.overflow.open();
};

scout.TagBar.prototype._createOverflowPopup = function() {
  return scout.create('TagBarOverflowPopup', {
    parent: this,
    closeOnAnchorMouseDown: false,
    focusableContainer: true,
    $anchor: this.$container,
    $headBlueprint: this.$overflowIcon,
    cssClass: this.cssClass
  });
};

scout.TagBar.prototype.closeOverflowPopup = function() {
  if (this.overflow && !this.overflow.destroying) {
    this.overflow.close();
  }
};

scout.TagBar.prototype._onOverflowPopupClose = function() {
  this.overflow = null;
};

scout.TagBar.prototype._installTooltipSupport = function() {
  scout.tooltips.install(this.$container, {
    parent: this,
    selector: '.tag-text',
    text: this._tagTooltipText.bind(this),
    arrowPosition: 50,
    arrowPositionUnit: '%',
    nativeTooltip: !scout.device.isCustomEllipsisTooltipPossible()
  });
};

scout.TagBar.prototype._uninstallTooltipSupport = function() {
  scout.tooltips.uninstall(this.$container);
};

scout.TagBar.prototype._tagTooltipText = function($tag) {
  return $tag.isContentTruncated() ? $tag.text() : null;
};

scout.TagBar.prototype._removeFocusFromTagElements = function() {
  scout.TagBar.findFocusableTagElements(this.$container)
    .removeClass('focused')
    .setTabbable(false);
};

scout.TagBar.prototype.focus = function() {
  this.$container.addClass('focused');
  this._removeFocusFromTagElements();
  this.closeOverflowPopup();
};

scout.TagBar.prototype.blur = function() {
  this.$container.removeClass('focused');

  // when overflow popup opens it sets focus to the first tag element, this means:
  // the input field loses focus. In that case we must prevent that the overflow popup is closed.
  var popupRequestsFocus = this.overflow && this.overflow.$container.has(event.relatedTarget);
  if (popupRequestsFocus) {
    return;
  }
  this.closeOverflowPopup();
};

scout.TagBar.prototype.setOverflowVisible = function(overflowVisible) {
  this.setProperty('overflowVisible', overflowVisible);
};

scout.TagBar.prototype._renderOverflowVisible = function() {
  if (this.overflowVisible) {
    if (!this.$overflowIcon) {
      this.$overflowIcon = this.$container
        .prependDiv('overflow-icon')
        .on('mousedown', this._onOverflowIconMousedown.bind(this));
    }
  } else {
    if (this.$overflowIcon) {
      this.$overflowIcon.remove();
      this.$overflowIcon = null;
    }
  }
};

scout.TagBar.prototype._updateErrorStatusClasses = function(statusClass, hasStatus) {
  scout.TagBar.parent.prototype._updateErrorStatusClasses.call(this, statusClass, hasStatus);
  this.$container.removeClass(scout.FormField.SEVERITY_CSS_CLASSES);
  this.$container.addClass(statusClass, hasStatus);
};

/**
 * Returns the tag-texts of the tag-elements currently visible in the UI (=not hidden).
 */
scout.TagBar.prototype.visibleTags = function() {
  if (!this.rendered) {
    return [];
  }
  var tags = [];
  this.$container
    .find('.tag-element:not(.hidden)')
    .each(function() {
      tags.push(scout.TagBar.getTagData($(this)));
    });
  return tags;
};

//--- static helpers ---

scout.TagBar.findFocusedTagElement = function($container) {
  return $container.find('.tag-element.focused');
};

scout.TagBar.findFocusableTagElements = function($container) {
  return $container.find('.tag-element:not(.hidden),.overflow-icon');
};

scout.TagBar.focusFirstTagElement = function($container) {
  this.focusTagElement(this.firstTagElement($container));
};

scout.TagBar.firstTagElement = function($container) {
  return $container.find('.tag-element').first();
};

scout.TagBar.focusTagElement = function($tagElement) {
  $tagElement
    .setTabbable(true)
    .addClass('focused')
    .focus();
};

scout.TagBar.unfocusTagElement = function($tagElement) {
  $tagElement
    .setTabbable(false)
    .removeClass('focused');
};

scout.TagBar.getTagData = function($tag) {
  var tagData = $tag.data('tag');
  if (tagData) {
    return tagData;
  }
  return $tag.parent().data('tag');
};

scout.TagBar.renderTags = function($parent, tags, enabled, clickHandler, removeHandler) {
  $parent.find('.tag-element').remove();
  tags.forEach(function(tagText) {
    scout.TagBar.renderTag($parent, tagText, enabled, clickHandler, removeHandler);
  }, this);
};

scout.TagBar.renderTag = function($parent, tagText, enabled, clickHandler, removeHandler) {
  var $element = $parent
    .appendDiv('tag-element')
    .data('tag', tagText);
  var $tagText = $element.appendSpan('tag-text', tagText);
  if (clickHandler) {
    $tagText
      .addClass('clickable')
      .on('mousedown', clickHandler);
  }
  if (enabled) {
    $element
      .appendSpan('tag-remove-icon')
      .on('click', removeHandler);
  } else {
    $element.addClass('disabled');
  }
  return $element;
};
