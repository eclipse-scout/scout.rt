/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {GroupBox, GroupBoxEventMap, GroupBoxModel, Menu, ObjectUuidProvider, PropertyChangeEvent, Tab, TabBox, TabItemLayout} from '../../../index';

export interface TabItemModel extends GroupBoxModel {
  /**
   * Configures the keystroke to select this tab item.
   *
   * @see {@link Action.keyStroke} for the format and examples.
   */
  selectionKeystroke?: string;
  marked?: boolean;
}

export interface TabItemEventMap extends GroupBoxEventMap {
  'propertyChange:marked': PropertyChangeEvent<boolean>;
}

export class TabItem extends GroupBox implements TabItemModel {
  declare model: TabItemModel;
  declare eventMap: TabItemEventMap;
  declare self: TabItem;
  declare parent: TabBox;

  selectionKeystroke: string;
  marked: boolean;

  constructor() {
    super();
    this.selectionKeystroke = null;
    this.marked = false;
  }

  protected override _createLayout(): TabItemLayout {
    return new TabItemLayout(this);
  }

  /**
   * Handled by {@link Tab}
   */
  protected override _computeTitleVisible(labelVisible: boolean): boolean {
    return false;
  }

  /**
   * Handled by {@link Tab}
   */
  override addStatus() {
    // void
  }

  /**
   * Handled by {@link Tab}
   */
  protected override _computeStatusVisible(): boolean {
    return false;
  }

  setMarked(marked: boolean) {
    this.setProperty('marked', marked);
  }

  override getContextMenuItems(onlyVisible = true): Menu[] {
    return [];
  }

  /**
   * Selects and focuses the corresponding {@link Tab}.
   * @returns true if the tab could be focused, false if not
   */
  override focus(): boolean {
    this.select();
    // ensure the focus is on the tab
    return this.parent.focusTabItem(this);
  }

  override isFocused(): boolean {
    return this.parent.isTabItemFocused(this);
  }

  select() {
    if (this.parent.selectedTab !== this) {
      this.parent.setSelectedTab(this);
    }
  }

  getTab(): Tab {
    return this.parent.getTabForItem(this);
  }
}

ObjectUuidProvider.UuidPathSkipWidgets.add(TabItem);
