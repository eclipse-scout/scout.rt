/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, AriaLabelledByInsertPosition, InitModelOf, scout, strings, TabbableCoordinator, TableHeaderMenuButton, TableHeaderMenuGroupEventMap, TableHeaderMenuGroupModel, Widget, widgets} from '../index';

export class TableHeaderMenuGroup extends Widget implements TableHeaderMenuGroupModel {
  declare model: TableHeaderMenuGroupModel;
  declare eventMap: TableHeaderMenuGroupEventMap;
  declare self: TableHeaderMenuGroup;

  text: string;
  $text: JQuery;
  /**
   * Specifies which of the "items" (buttons) is currently active and should be shown in the group title.
   * If null, the initial text is shown instead. This property is automatically set by _updateCurrentGroupItem().
   */
  currentGroupItem: TableHeaderMenuGroupItem;
  tabbableCoordinator: TabbableCoordinator;

  // Internal properties used by _updateCurrentGroupItem() to compute the actual "current item".
  // - "hovered" = set when the user hovers an item with the mouse
  // - "focused" = set when an item is focused with the keyboard
  // - "active" = can be set when an item is neither hovered nor focused but should still be marked as the current item (e.g. while a context menu popup is open)
  protected _hoveredGroupItem: TableHeaderMenuGroupItem;
  protected _focusedGroupItem: TableHeaderMenuGroupItem;
  protected _activeGroupItem: TableHeaderMenuGroupItem;

  constructor() {
    super();
    this.tabbableCoordinator = scout.create(TabbableCoordinator, {parent: this});
  }

  protected override _init(options: InitModelOf<this>) {
    super._init(options);
    this.resolveTextKeys(['text']);
  }

  protected override _addChild(child: Widget) {
    super._addChild(child);
    this.tabbableCoordinator.setItems(this.children.filter(child => child instanceof TableHeaderMenuButton) as TableHeaderMenuButton[]);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('table-header-menu-group buttons');
    this.$text = this.$container.appendDiv('table-header-menu-group-text');

    this.children.forEach(child => {
      child.render();
      if (isGroupItem(child)) {
        this._installGroupItem(child);
      }
    });
    widgets.updateFirstLastMarker(this.children);
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderCurrentGroupItem();
  }

  setText(text: string) {
    this.text = text;
    if (this.rendered) {
      this._renderComputedText();
    }
  }

  protected _renderComputedText() {
    let computedText = this._computeText();
    this.$text.text(computedText);
  }

  protected _computeText(): string {
    if (this.currentGroupItem) {
      return strings.join(' ', this.text, this.currentGroupItem.computeGroupSuffix());
    }
    return this.text;
  }

  setCurrentGroupItem(currentGroupItem: TableHeaderMenuGroupItem) {
    if (!currentGroupItem) {
      this._hoveredGroupItem = null;
      this._activeGroupItem = null;
      this._focusedGroupItem = null;
    }
    this.setProperty('currentGroupItem', currentGroupItem);
  }

  protected _renderCurrentGroupItem() {
    this._renderComputedText();
  }

  /**
   * Installs hover and mouse over handlers to update the text of the group.
   */
  protected _installGroupItem(item: TableHeaderMenuGroupItem) {
    // Remove aria-label because aria-labelledby points to a more sophisticated text
    aria.label(item.$container, null);
    // link item with the group header, the header is updated with the text of the action
    aria.linkElementWithLabel(item.get$Focusable(), this.$text, AriaLabelledByInsertPosition.FRONT, true);

    item.$container
      .on('focusin', () => this.setFocusedGroupItem(item))
      .on('focusout', () => this.setFocusedGroupItem(null))
      .on('mouseenter', () => this.setHoveredGroupItem(item))
      .on('mouseleave', () => this.setHoveredGroupItem(null));
  }

  /** @internal */
  setHoveredGroupItem(hoveredGroupItem: TableHeaderMenuGroupItem) {
    this._hoveredGroupItem = hoveredGroupItem;
    this._updateCurrentGroupItem();
  }

  /** @internal */
  setFocusedGroupItem(focusedGroupItem: TableHeaderMenuGroupItem) {
    this._focusedGroupItem = focusedGroupItem;
    this._updateCurrentGroupItem();
  }

  /** @internal */
  setActiveGroupItem(activeGroupItem: TableHeaderMenuGroupItem) {
    this._activeGroupItem = activeGroupItem;
    this._updateCurrentGroupItem();
  }

  protected _updateCurrentGroupItem() {
    this.setCurrentGroupItem(this._hoveredGroupItem || this._focusedGroupItem || this._activeGroupItem);
  }
}

export interface TableHeaderMenuGroupItem extends Widget {
  /**
   * @returns the suffix to append to the text of a {@link TableHeaderMenuGroup}.
   */
  computeGroupSuffix(): string;
}

function isGroupItem(item: Widget): item is TableHeaderMenuGroupItem {
  return Reflect.has(item, 'computeGroupSuffix');
}
