/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Device, HtmlComponent, scout, TagBarEventMap, TagBarLayout, TagBarModel, TagBarOverflowPopup, tooltips, Widget} from '../index';
import $ from 'jquery';

export class TagBar extends Widget implements TagBarModel {
  declare model: TagBarModel;
  declare eventMap: TagBarEventMap;
  declare self: TagBar;

  overflowEnabled: boolean;
  overflowVisible: boolean;
  tags: string[];
  clickable: boolean;
  overflow: TagBarOverflowPopup;
  $overflowIcon: JQuery;

  constructor() {
    super();

    this.overflowEnabled = true;
    this.$overflowIcon = null;
    this.overflowVisible = false;
    this.overflow = null;
    this.tags = [];
    this.clickable = false;
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('tag-bar');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new TagBarLayout(this));
    this._installTooltipSupport();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderTags();
    this._renderOverflowVisible();
  }

  protected override _remove() {
    this.$overflowIcon = null;
    this.overflowVisible = false;
    this.closeOverflowPopup();
    this._uninstallTooltipSupport();
    super._remove();
  }

  setTags(tags: string[]) {
    this.setProperty('tags', tags);
  }

  updateTags() {
    if (this.rendered) {
      this._renderTags();
    }
  }

  protected _renderTags() {
    let tags = arrays.ensure(this.tags);
    this.renderTags(this.$container, tags);
    this.invalidateLayoutTree();
  }

  setClickable(clickable: boolean) {
    this.setProperty('clickable', clickable);
  }

  protected _renderClickable() {
    this._renderTagsClickable(this.$container.children('.tag-element'));
    this.invalidateLayoutTree();
  }

  protected _onTagClick(event: JQuery.MouseDownEvent): boolean {
    let tag = TagBar.getTagData($(event.currentTarget));
    this._triggerTagClick(tag);
    return false;
  }

  protected _triggerTagClick(tag: string) {
    this.trigger('tagClick', {
      tag: tag
    });
  }

  protected override _renderEnabled() {
    super._renderEnabled();
    this._renderTagsRemovable(this.$container.children('.tag-element'));
    this.invalidateLayoutTree();
  }

  protected _onTagRemoveClick(event: JQuery.ClickEvent): boolean {
    if (this.enabledComputed) {
      this.removeTagByElement($(event.currentTarget));
    }
    return false;
  }

  removeTagByElement($tag: JQuery) {
    let tag = TagBar.getTagData($tag);
    if (tag) {
      this._triggerTagRemove(tag, $tag);
    }
  }

  protected _triggerTagRemove(tag: string, $tag: JQuery) {
    this.trigger('tagRemove', {
      tag: tag,
      $tag: $tag
    });
  }

  protected _onOverflowIconMousedown(event: JQuery.MouseDownEvent): boolean {
    this.toggleOverflowPopup();
    return false;
  }

  isOverflowIconFocused(): boolean {
    if (!this.$overflowIcon) {
      return false;
    }
    let $ae = this.$container.activeElement();
    return this.$overflowIcon.is($ae);
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

  protected _createOverflowPopup(): TagBarOverflowPopup {
    return scout.create(TagBarOverflowPopup, {
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

  protected _installTooltipSupport() {
    tooltips.install(this.$container, {
      parent: this,
      selector: '.tag-text',
      text: this._tagTooltipText.bind(this),
      arrowPosition: 50,
      arrowPositionUnit: '%',
      nativeTooltip: !Device.get().isCustomEllipsisTooltipPossible()
    });
  }

  protected _uninstallTooltipSupport() {
    tooltips.uninstall(this.$container);
  }

  protected _tagTooltipText($tag: JQuery): string {
    return $tag.isContentTruncated() ? $tag.text() : null;
  }

  protected _removeFocusFromTagElements() {
    TagBar.findFocusableTagElements(this.$container)
      .removeClass('focused')
      .setTabbable(false);
  }

  override focus(options?: { preventScroll?: boolean }): boolean {
    this.$container.addClass('focused');
    this._removeFocusFromTagElements();
    this.closeOverflowPopup();
    return false;
  }

  blur() {
    this.$container.removeClass('focused');

    // when overflow popup opens it sets focus to the first tag element, this means:
    // the input field loses focus. In that case we must prevent that the overflow popup is closed.
    let relatedTargetOfCurrentEvent = (event as FocusEvent).relatedTarget as Element;
    let popupRequestsFocus = this.overflow && this.overflow.$container.has(relatedTargetOfCurrentEvent);
    if (popupRequestsFocus) {
      return;
    }
    this.closeOverflowPopup();
  }

  setOverflowVisible(overflowVisible: boolean) {
    this.setProperty('overflowVisible', overflowVisible);
  }

  protected _renderOverflowVisible() {
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

  /**
   * Returns the tag-texts of the tag-elements currently visible in the UI (=not hidden).
   */
  visibleTags(): string[] {
    if (!this.rendered) {
      return [];
    }
    let tags: string[] = [];
    this.$container
      .find('.tag-element:not(.hidden)')
      .each(function() {
        tags.push(TagBar.getTagData($(this)));
      });
    return tags;
  }

  renderTags($parent: JQuery, tags: string[]) {
    $parent.find('.tag-element').remove();
    tags.forEach(tagText => this.renderTag($parent, tagText));
    let $tags = $parent.children('.tag-element');
    this._renderTagsClickable($tags);
    this._renderTagsRemovable($tags);
  }

  renderTag($parent: JQuery, tagText: string): JQuery {
    let $tag = $parent
      .appendDiv('tag-element')
      .data('tag', tagText);
    $tag.appendSpan('tag-text', tagText);
    return $tag;
  }

  protected _renderTagsClickable($tags: JQuery) {
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

  protected _renderTagsRemovable($tags: JQuery) {
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

  static findFocusedTagElement($container: JQuery): JQuery {
    return $container.find('.tag-element.focused');
  }

  static findFocusableTagElements($container: JQuery): JQuery {
    return $container.find('.tag-element:not(.hidden),.overflow-icon');
  }

  static focusFirstTagElement($container: JQuery) {
    this.focusTagElement(this.firstTagElement($container));
  }

  static firstTagElement($container: JQuery): JQuery {
    return $container.find('.tag-element').first();
  }

  static focusTagElement($tagElement: JQuery) {
    $tagElement
      .setTabbable(true)
      .addClass('focused')
      .focus();
  }

  static unfocusTagElement($tagElement: JQuery) {
    $tagElement
      .setTabbable(false)
      .removeClass('focused');
  }

  static getTagData($tag: JQuery): string {
    let tagData = $tag.data('tag') as string;
    if (tagData) {
      return tagData;
    }
    return $tag.parent().data('tag');
  }
}
