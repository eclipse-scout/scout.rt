/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, Device, FormField, HtmlComponent, scout, TagBarLayout, tooltips, Widget} from '../index';
import $ from 'jquery';

export default class TagBar extends Widget {

  constructor() {
    super();

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
  }

  _render() {
    this.$container = this.$parent.appendDiv('tag-bar');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new TagBarLayout(this));
    this._installTooltipSupport();
  }

  _renderProperties() {
    super._renderProperties();
    this._renderTags();
    this._renderOverflowVisible();
  }

  _remove() {
    this.$overflowIcon = null;
    this.overflowVisible = false;
    this.closeOverflowPopup();
    this._uninstallTooltipSupport();
    super._remove();
  }

  setTags(tags) {
    this.setProperty('tags', tags);
  }

  updateTags() {
    if (this.rendered) {
      this._renderTags();
    }
  }

  /**
   * This function is also used by sub- and friend-classes like the TagOverflowPopup.
   */
  _renderTags() {
    let tags = arrays.ensure(this.tags);
    let clickHandler = this.clickable ? this._onTagClick.bind(this) : null;
    let removeHandler = this._onTagRemoveClick.bind(this);
    TagBar.renderTags(this.$container, tags, this.enabledComputed, clickHandler, removeHandler);
    this.invalidateLayoutTree();
  }

  _onTagClick(event) {
    let tag = TagBar.getTagData($(event.currentTarget));
    this._triggerTagClick(tag);
    return false;
  }

  _triggerTagClick(tag) {
    this.trigger('tagClick', {
      tag: tag
    });
  }

  _onTagRemoveClick(event) {
    if (this.enabledComputed) {
      this.removeTagByElement($(event.currentTarget));
    }
    return false;
  }

  removeTagByElement($tag) {
    let tag = TagBar.getTagData($tag);
    if (tag) {
      this._triggerTagRemove(tag, $tag);
    }
  }

  _triggerTagRemove(tag, $tag) {
    this.trigger('tagRemove', {
      tag: tag,
      $tag: $tag
    });
  }

  _onOverflowIconMousedown(event) {
    this.openOverflowPopup();
    return false;
  }

  isOverflowIconFocused() {
    if (!this.$overflowIcon) {
      return false;
    }
    let ae = this.$container.activeElement();
    return this.$overflowIcon.is(ae);
  }

  openOverflowPopup() {
    if (this.overflow) {
      return;
    }
    this.overflow = this._createOverflowPopup();
    this.overflow.on('close', this._onOverflowPopupClose.bind(this));
    this.overflow.open();
  }

  _createOverflowPopup() {
    return scout.create('TagBarOverflowPopup', {
      parent: this,
      closeOnAnchorMouseDown: false,
      focusableContainer: true,
      $anchor: this.$container,
      $headBlueprint: this.$overflowIcon,
      cssClass: this.cssClass
    });
  }

  closeOverflowPopup() {
    if (this.overflow && !this.overflow.destroying) {
      this.overflow.close();
    }
  }

  _onOverflowPopupClose() {
    this.overflow = null;
  }

  _installTooltipSupport() {
    tooltips.install(this.$container, {
      parent: this,
      selector: '.tag-text',
      text: this._tagTooltipText.bind(this),
      arrowPosition: 50,
      arrowPositionUnit: '%',
      nativeTooltip: !Device.get().isCustomEllipsisTooltipPossible()
    });
  }

  _uninstallTooltipSupport() {
    tooltips.uninstall(this.$container);
  }

  _tagTooltipText($tag) {
    return $tag.isContentTruncated() ? $tag.text() : null;
  }

  _removeFocusFromTagElements() {
    TagBar.findFocusableTagElements(this.$container)
      .removeClass('focused')
      .setTabbable(false);
  }

  focus() {
    this.$container.addClass('focused');
    this._removeFocusFromTagElements();
    this.closeOverflowPopup();
  }

  blur() {
    this.$container.removeClass('focused');

    // when overflow popup opens it sets focus to the first tag element, this means:
    // the input field loses focus. In that case we must prevent that the overflow popup is closed.
    let popupRequestsFocus = this.overflow && this.overflow.$container.has(event.relatedTarget);
    if (popupRequestsFocus) {
      return;
    }
    this.closeOverflowPopup();
  }

  setOverflowVisible(overflowVisible) {
    this.setProperty('overflowVisible', overflowVisible);
  }

  _renderOverflowVisible() {
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
  }

  _updateErrorStatusClasses(statusClass, hasStatus) {
    super._updateErrorStatusClasses(statusClass, hasStatus);
    this.$container.removeClass(FormField.SEVERITY_CSS_CLASSES);
    this.$container.addClass(statusClass, hasStatus);
  }

  /**
   * Returns the tag-texts of the tag-elements currently visible in the UI (=not hidden).
   */
  visibleTags() {
    if (!this.rendered) {
      return [];
    }
    let tags = [];
    this.$container
      .find('.tag-element:not(.hidden)')
      .each(function() {
        tags.push(TagBar.getTagData($(this)));
      });
    return tags;
  }

  // --- static helpers ---

  static findFocusedTagElement($container) {
    return $container.find('.tag-element.focused');
  }

  static findFocusableTagElements($container) {
    return $container.find('.tag-element:not(.hidden),.overflow-icon');
  }

  static focusFirstTagElement($container) {
    this.focusTagElement(this.firstTagElement($container));
  }

  static firstTagElement($container) {
    return $container.find('.tag-element').first();
  }

  static focusTagElement($tagElement) {
    $tagElement
      .setTabbable(true)
      .addClass('focused')
      .focus();
  }

  static unfocusTagElement($tagElement) {
    $tagElement
      .setTabbable(false)
      .removeClass('focused');
  }

  static getTagData($tag) {
    let tagData = $tag.data('tag');
    if (tagData) {
      return tagData;
    }
    return $tag.parent().data('tag');
  }

  static renderTags($parent, tags, enabled, clickHandler, removeHandler) {
    $parent.find('.tag-element').remove();
    tags.forEach(tagText => {
      TagBar.renderTag($parent, tagText, enabled, clickHandler, removeHandler);
    }, this);
  }

  static renderTag($parent, tagText, enabled, clickHandler, removeHandler) {
    let $element = $parent
      .appendDiv('tag-element')
      .data('tag', tagText);
    let $tagText = $element.appendSpan('tag-text', tagText);
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
  }
}
