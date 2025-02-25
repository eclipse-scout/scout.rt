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

    // FIXME CGU [js-bookmark] parentNode is null for invisible root page -> add outline menus on . why does only OutlineAdapter.linkWithRow call updateMenus? BookmarkMenu is not inherited because inheritMenusFromParentTablePage is false for PageWithTable
    const table = this.page.parentNode.detailTable;
    const row = this.page.row;

    if (!table || !row || table !== row.getTable()) {
      return [];
    }

    return this._filterAndCloneParentTablePageMenus(table.menus, newParent);
  }

  protected _filterAndCloneParentTablePageMenus(tablePageMenus: Menu[], newParent: Widget): Menu[] {
    return this._filterParentTablePageMenus(tablePageMenus)
      .filter(menu => this.page.isMenuInheritedFromParentTablePage(menu))
      .map(menu => this._cloneParentTablePageMenu(menu, newParent));
  }

  protected _filterParentTablePageMenus(tablePageMenus: Menu[]): Menu[] {
    return menus.filter(tablePageMenus, Table.MenuType.SingleSelection);
  }

  protected _cloneParentTablePageMenu(menu: Menu, newParent: Widget): Menu {
    if (!menu) {
      return null;
    }

    const clone = menu.clone(
      {
        parent: newParent,
        menuTypes: []
      },
      {
        delegateEventsToOriginal: ['action'],
        delegateAllPropertiesToClone: true,
        excludePropertiesToClone: ['menuTypes', 'childActions']
      });

    if (menu.childActions && menu.childActions.length) {
      clone.setChildActions(this._filterAndCloneParentTablePageMenus(menu.childActions, clone));
    }

    return clone;
  }
}

