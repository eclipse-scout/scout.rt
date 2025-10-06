/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ContextMenuPopup, Menu, MenuModel, Popup, scout, SimpleTabArea} from "..";

export class SimpleTabOverflowMenu extends Menu implements SimpleTabOverflowMenuModel {
  declare parent: SimpleTabArea;
  overflowTabIndices: number[];

  constructor() {
    super();
    this.toggleAction = true;
  }

  get tabArea(): SimpleTabArea {
    return this.parent;
  }

  protected override _render() {
    super._render();
    this.$container
      .addClass('simple-overflow-tab-item')
      .removeClass('menu-item')
      .appendDiv('num-tabs').text(this.overflowTabIndices.length);
  }

  protected override _doActionTogglesPopup(): boolean {
    return true;
  }

  protected override _createPopup(): Popup {
    let overflowMenus = [];
    for (const i of this.overflowTabIndices) {
      let tab = this.tabArea.getTabs()[i];
      let menu = scout.create(Menu, {
        parent: this.tabArea,
        text: tab.getMenuText()
      });
      menu.on('action', () => {
        this.tabArea.selectTab(tab);
      });
      overflowMenus.push(menu);
    }

    return scout.create(ContextMenuPopup, {
      parent: this.tabArea,
      menuItems: overflowMenus,
      cloneMenuItems: false,
      $anchor: this.$container,
      closeOnAnchorMouseDown: false
    });
  }
}

export interface SimpleTabOverflowMenuModel extends MenuModel {
  parent?: SimpleTabArea;
  tabArea?: SimpleTabArea;
  overflowTabIndices?: number[];
}
