/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
