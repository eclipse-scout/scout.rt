/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {GroupBox, GroupBoxEventMap, GroupBoxModel, Menu, PropertyChangeEvent, Tab, TabBox, TabItemLayout} from '../../../index';

export interface TabItemModel extends GroupBoxModel {
  marked?: boolean;
}

export interface TabItemEventMap extends GroupBoxEventMap {
  'propertyChange:marked': PropertyChangeEvent<boolean>;
}

export default class TabItem extends GroupBox implements TabItemModel {
  declare model: TabItemModel;
  declare eventMap: TabItemEventMap;
  declare self: TabItem;
  declare parent: TabBox;

  marked: boolean;

  constructor() {
    super();
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
