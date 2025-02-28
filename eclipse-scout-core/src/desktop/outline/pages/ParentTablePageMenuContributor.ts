/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Menu, MenuOwner, menus, PageDetailMenuContributor, Table, Widget} from '../../../index';

export class ParentTablePageMenuContributor extends PageDetailMenuContributor {

  contribute(originalMenus: Menu[], detailContent: MenuOwner) {
    const parentTablePageMenus = this._computeParentTablePageMenus(detailContent);
    return [...parentTablePageMenus, ...originalMenus];
  }

  protected _computeParentTablePageMenus(newParent: Widget): Menu[] {
    if (!this.page.parentNode) {
      return [];
    }

    const table = this.page.parentNode.detailTable;
    const row = this.page.row;

    if (!table || !row || table !== row.getTable()) {
      return [];
    }

    return this._cloneMenus(table.menus, newParent);
  }

  protected override _cloneMenus(tablePageMenus: Menu[], newParent: Widget): Menu[] {
    return this._filterParentTablePageMenus(tablePageMenus)
      .filter(menu => this.page.isMenuInheritedFromParentTablePage(menu))
      .map(menu => this._cloneMenu(menu, newParent));
  }

  protected _filterParentTablePageMenus(tablePageMenus: Menu[]): Menu[] {
    return menus.filter(tablePageMenus, Table.MenuType.SingleSelection);
  }
}

