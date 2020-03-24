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
import {GroupBox, TabItemLayout} from '../../../index';

export default class TabItem extends GroupBox {

  constructor() {
    super();
    this.marked = false;
  }

  _init(model) {
    super._init(model);
    this._setMenusVisible(this.menusVisible);
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

  _setMenusVisible() {
    // Always invisible because menus are displayed in menu bar and not with status icon
    // Actually not needed at the moment because only value fields have menus (at least at the java model).
    // But actually we should change this so that menus are possible for every form field
    // TODO [7.0] cgu: remove this comment if java model supports form field menus
    this._setProperty('menusVisible', false);
  }

  /**
   * @override FormField.js
   */
  focus() {
    if (this.parent.selectedTab !== this) {
      this.parent.setSelectedTab(this);
    }
    // ensure the focus is on the tab
    this.parent.focusTabItem(this);
  }

  isFocused() {
    return this.parent.isTabItemFocused(this);
  }
}
