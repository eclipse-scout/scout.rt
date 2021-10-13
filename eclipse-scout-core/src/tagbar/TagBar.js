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

  _renderTags() {
    let tags = arrays.ensure(this.tags);
    this.renderTags(this.$container, tags, this.enabledComputed);
    this.invalidateLayoutTree();
  }

  setClickable(clickable) {
    this.setProperty('clickable', clickable);
  }

  _renderClickable() {
    this._renderTagsClickable(this.$container.children('.tag-element'));
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

  _renderEnabled() {
    super._renderEnabled();
    this._renderTagsRemovable(this.$container.children('.tag-element'));
    this.invalidateLayoutTree();
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
    this.toggleOverflowPopup();
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
    this.$overflowIcon.addClass('selected');
    this.overflow = this._createOverflowPopup();
    this.overflow.open();
    this.overflow.one('destroy', event => {
      if (this.$overflowIcon) {
        this.$overflowIcon.removeClass('selected');
      }
      this.overflow = null;
    });
  }

  _createOverflowPopup() {
    return scout.create('TagBarOverflowPopup', {
      parent: this,
      closeOnAnchorMouseDown: false,
      focusableContainer: true,
      $anchor: this.$overflowIcon,
      cssClass: this.cssClass
    });
  }

  closeOverflowPopup() {
    if (this.overflow) {
      this.overflow.close();
    }
  }

  toggleOverflowPopup() {
    if (this.overflow) {
      this.closeOverflowPopup();
    } else {
      this.openOverflowPopup();
    }
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
    this.$container.addClass(statusClass);
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

  renderTags($parent, tags) {
    $parent.find('.tag-element').remove();
    tags.forEach(tagText => this.renderTag($parent, tagText));
    let $tags = $parent.children('.tag-element');
    this._renderTagsClickable($tags);
    this._renderTagsRemovable($tags);
  }

  renderTag($parent, tagText) {
    let $tag = $parent
      .appendDiv('tag-element')
      .data('tag', tagText);
    $tag.appendSpan('tag-text', tagText);
    return $tag;
  }

  _renderTagsClickable($tags) {
    $tags.each((i, tag) => {
      let $tag = $(tag);
      let $tagText = $tag.children('.tag-text');
      let clickHandler = $tag.data('click-handler');
      if (this.clickable) {
        if (!clickHandler) {
          clickHandler = this._onTagClick.bind(this);
          $tag.data('click-handler', clickHandler);
          $tagText.on('mousedown', clickHandler);
        }
      } else {
        $tagText.off('mousedown', clickHandler);
        $tag.removeData('click-handler');
      }
      $tag.toggleClass('clickable', this.clickable);
    });
  }

  _renderTagsRemovable($tags) {
    $tags.each((i, tag) => {
      let $tag = $(tag);
      let $tagRemove = $tag.children('.tag-remove-icon');
      let removeHandler = $tag.data('remove-handler');
      if (this.enabledComputed) {
        if (!removeHandler) {
          removeHandler = this._onTagRemoveClick.bind(this);
          $tag.data('remove-handler', removeHandler);
          $tag
            .appendSpan('tag-remove-icon')
            .on('click', removeHandler);
        }
      } else {
        $tagRemove.off('click', removeHandler);
        $tagRemove.remove();
        $tag.removeData('remove-handler');
      }
      $tag.toggleClass('removable', this.enabledComputed);
    });
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
}
