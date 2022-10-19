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
import {GroupBoxLayout, MenuBarLayout} from '../../../index';

export default class TabItemLayout extends GroupBoxLayout {

  constructor(tabItem) {
    super(tabItem);
  }

  _$status() {
    // Use status area from tab box for alignment purposes (e.g. to align notification with title border)
    return this.groupBox.parent.$status;
  }

  _layoutStatus() {
    // Nothing to layout here because the status of the tab item is displayed in the tab (see TabItem.addStatus, Tab._updateStatus)
  }

  _menuBarSize(htmlMenuBar, containerSize, statusWidth) {
    let menuBarSize = MenuBarLayout.size(htmlMenuBar, containerSize);
    let tabBox = this.groupBox.parent;
    menuBarSize.width = tabBox.header.$container.outerWidth();
    return menuBarSize;
  }

  _headerHeight() {
    return 0;
  }
}
