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
import {Dimension, GroupBoxLayout, HtmlComponent, MenuBarLayout, TabItem} from '../../../index';

export class TabItemLayout extends GroupBoxLayout {
  declare groupBox: TabItem;

  constructor(tabItem: TabItem) {
    super(tabItem);
  }

  protected override _$status(): JQuery {
    // Use status area from tab box for alignment purposes (e.g. to align notification with title border)
    return this.groupBox.parent.$status;
  }

  protected override _layoutStatus() {
    // Nothing to layout here because the status of the tab item is displayed in the tab (see TabItem.addStatus, Tab._updateStatus)
  }

  protected override _menuBarSize(htmlMenuBar: HtmlComponent, containerSize: Dimension, statusWidth: number): Dimension {
    let menuBarSize = MenuBarLayout.size(htmlMenuBar, containerSize);
    let tabBox = this.groupBox.parent;
    menuBarSize.width = tabBox.header.$container.outerWidth();
    return menuBarSize;
  }

  protected override _headerHeight(): number {
    return 0;
  }
}
