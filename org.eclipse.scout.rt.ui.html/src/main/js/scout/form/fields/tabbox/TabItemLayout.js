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
import {GroupBoxLayout} from '../../../index';
import {MenuBarLayout} from '../../../index';

export default class TabItemLayout extends GroupBoxLayout {

constructor(tabItem) {
  super( tabItem);
}


_layoutStatus() {
  // NOP: $status width is set in TabItem.addStatus()
}

_menuBarSize(htmlMenuBar, containerSize, statusWidth) {
  var menuBarSize = MenuBarLayout.size(htmlMenuBar, containerSize);
  var tabBox = this.groupBox.parent;
  menuBarSize.width = tabBox.header.$container.outerWidth();
  return menuBarSize;
}

_titleHeight() {
  return 0;
}
}
