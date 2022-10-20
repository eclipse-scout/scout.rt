/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {GroupBox, TabItemLayout} from '../../../index';

export default class TabItem extends GroupBox {

  constructor() {
    super();
    this.marked = false;
  }

  _createLayout() {
    return new TabItemLayout(this);
  }

  /**
   * @override GroupBox.js
   *
   * handled by Tab.js
   */
  _computeTitleVisible(labelVisible) {
    return false;
  }

  /**
   * @override GroupBox.js
   *
   * handled by Tab.js
   */
  addStatus() {
    // void
  }

  /**
   * @override GroupBox.js
   *
   * handled by Tab.js
   */
  _computeStatusVisible() {
    return false;
  }

  setMarked(marked) {
    this.setProperty('marked', marked);
  }

  getContextMenuItems(onlyVisible = true) {
    return [];
  }

  /**
   * @override FormField.js
   */
  focus() {
    this.select();
    // ensure the focus is on the tab
    this.parent.focusTabItem(this);
  }

  isFocused() {
    return this.parent.isTabItemFocused(this);
  }

  select() {
    if (this.parent.selectedTab !== this) {
      this.parent.setSelectedTab(this);
    }
  }

  getTab() {
    return this.parent.getTabForItem(this);
  }
}
