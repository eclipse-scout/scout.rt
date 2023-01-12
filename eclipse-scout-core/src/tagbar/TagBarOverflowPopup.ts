/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, arrays, EventHandler, InitModelOf, Popup, PropertyChangeEvent, TagBar, TagBarOverflowPopupLayout, TagField, TagFieldDeleteKeyStroke, TagFieldKeyStrokeAdapter, TagFieldNavigationKeyStroke} from '../index';

export class TagBarOverflowPopup extends Popup {
  declare parent: TagBar;

  $body: JQuery;

  protected _tagBarPropertyChangeListener: EventHandler;

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

  protected override _init(options: InitModelOf<this>) {
    super._init(options);
    this._tagBarPropertyChangeListener = this._onTagBarPropertyChange.bind(this);
    this.parent.on('propertyChange', this._tagBarPropertyChangeListener);
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    this.keyStrokeContext.registerKeyStrokes([
      new TagFieldNavigationKeyStroke(this._createFieldAdapter()),
      new TagFieldDeleteKeyStroke(this._createFieldAdapter())
    ]);
  }

  protected override _destroy() {
    this.parent.off('propertyChange', this._tagBarPropertyChangeListener);
    super._destroy();
  }

  protected override _render() {
    super._render();

    this.$container.addClass('tag-overflow-popup');
    this.$body = this.$container.appendDiv('popup-body');
  }

  protected override _createLayout(): AbstractLayout {
    return new TagBarOverflowPopupLayout(this);
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderTags();
  }

  protected _renderTags() {
    let tagBar = this.parent;
    let visibleTags = tagBar.visibleTags();
    let allTags = arrays.ensure(tagBar.tags);
    let overflowTags = allTags.filter(tagText => visibleTags.indexOf(tagText) === -1);
    tagBar.renderTags(this.$body, overflowTags);

    if (!this.rendering) {
      this.invalidateLayoutTree();
    }
  }

  protected _focusFirstTagElement() {
    TagBar.focusFirstTagElement(this.$body);
  }

  protected _onTagBarPropertyChange(event: PropertyChangeEvent) {
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

  protected _createFieldAdapter(): TagFieldKeyStrokeAdapter {
    return TagBarOverflowPopup.createFieldAdapter(this);
  }

  static createFieldAdapter(field: TagBarOverflowPopup): TagFieldKeyStrokeAdapter {
    return {
      $container: () => field.$body,
      enabled: () => true,
      focus: () => {
        // nop
      },
      removeTag: tag => {
        let tagField = field.parent.parent as TagField;
        tagField.removeTag(tag);
      }
    };
  }
}
