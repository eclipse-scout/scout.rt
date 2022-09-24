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
import {arrays, Popup, TagBar, TagBarOverflowPopupLayout, TagFieldDeleteKeyStroke, TagFieldNavigationKeyStroke} from '../index';

export default class TagBarOverflowPopup extends Popup {

  constructor() {
    super();

    this.windowPaddingX = 0;
    this.windowPaddingY = 0;
    this._tagBarPropertyChangeListener = null;

    // We need not only to return which element receives the initial focus
    // but we must also prepare this element so it can receive the focus
    this.initialFocus = function() {
      return TagBar.firstTagElement(this.$body)
        .setTabbable(true)
        .addClass('focused')[0];
    };
  }

  _init(options) {
    super._init(options);
    this._tagBarPropertyChangeListener = this._onTagBarPropertyChange.bind(this);
    this.parent.on('propertyChange', this._tagBarPropertyChangeListener);
  }

  _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    this.keyStrokeContext.registerKeyStrokes([
      new TagFieldNavigationKeyStroke(this._createFieldAdapter()),
      new TagFieldDeleteKeyStroke(this._createFieldAdapter())
    ]);
  }

  _destroy() {
    this.parent.off('propertyChange', this._tagBarPropertyChangeListener);
    super._destroy();
  }

  _render() {
    super._render();

    this.$container.addClass('tag-overflow-popup');
    this.$body = this.$container.appendDiv('popup-body');
  }

  _createLayout() {
    return new TagBarOverflowPopupLayout(this);
  }

  _renderProperties() {
    super._renderProperties();
    this._renderTags();
  }

  _renderTags() {
    let tagBar = this.parent;
    let visibleTags = tagBar.visibleTags();
    let allTags = arrays.ensure(tagBar.tags);
    let overflowTags = allTags.filter(tagText => visibleTags.indexOf(tagText) === -1);
    tagBar.renderTags(this.$body, overflowTags);

    if (!this.rendering) {
      this.invalidateLayoutTree();
    }
  }

  _focusFirstTagElement() {
    TagBar.focusFirstTagElement(this.$body);
  }

  _onTagRemoveClick(event) {
    this.parent._onTagRemoveClick(event);
  }

  _onTagBarPropertyChange(event) {
    if (event.propertyName === 'tags') {
      let allTags = arrays.ensure(this.parent.tags);
      let visibleTags = this.parent.visibleTags();
      let numTags = allTags.length;
      // close popup when no more tags left or all tags are visible (=no overflow icon)
      if (numTags === 0 || numTags === visibleTags.length) {
        this.close();
      } else {
        this._renderTags();
        this._focusFirstTagElement();
      }
    }
  }

  _createFieldAdapter() {
    return TagBarOverflowPopup.createFieldAdapter(this);
  }

  static createFieldAdapter(field) {
    return {
      $container: () => field.$body,

      enabled: () => true,

      focus: () => {
      },

      one: (p1, p2) => {
        field.one(p1, p2);
      },

      off: (p1, p2) => {
        field.off(p1, p2);
      },

      removeTag: tag => {
        field.parent.parent.removeTag(tag);
      }
    };
  }
}
