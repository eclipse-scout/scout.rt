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
import {Menu, MenuBar, menus, scout, Session} from '../index';

export type MenuOrder = {
  order(items: Menu[]): OrderedMenuItems;
  menuBar: MenuBar;
};

export type OrderedMenuItems = { left: Menu[]; right: Menu[]; all: Menu[] };

export default class MenuItemsOrder {
  session: Session;
  objectType: string;
  emptySpaceTypes: string[];
  selectionTypes: string[];
  menuBar: MenuBar;

  constructor(session: Session, objectType: string) {
    this.session = session;
    this.objectType = objectType;
    this.emptySpaceTypes = ['EmptySpace'];
    this.selectionTypes = ['SingleSelection', 'MultiSelection'];
  }

  order(items: Menu[]): OrderedMenuItems {
    let buttons: Menu[] = [], emptySpaceItems: Menu[] = [], selectionItems: Menu[] = [], rightItems: Menu[] = [];
    let isEmptySpaceMenuVisible = false, isSelectionMenuVisible = false;

    items.forEach(item => {
      // skip separators added dynamically by this class
      if (item.createdBy === this) {
        return;
      }
      if (item.isButton()) {
        buttons.push(item);
      } else if (item.horizontalAlignment === 1) {
        rightItems.push(item);
      } else if (menus.checkType(item, this._menuTypes(this.emptySpaceTypes))) {
        if (item.visible) {
          isEmptySpaceMenuVisible = true;
        }
        emptySpaceItems.push(item);
      } else if (menus.checkType(item, this._menuTypes(this.selectionTypes))) {
        if (item.visible) {
          isSelectionMenuVisible = true;
        }
        selectionItems.push(item);
      }
    });

    // add fixed separator between emptySpace and selection
    if (isEmptySpaceMenuVisible && isSelectionMenuVisible) {
      emptySpaceItems.push(this._createSeparator());
    }

    return {
      left: buttons.concat(emptySpaceItems, selectionItems),
      right: rightItems,
      all: buttons.concat(emptySpaceItems, selectionItems).concat(rightItems)
    };
  }

  protected _menuTypes(types: string[]): string[] {
    let i, menuTypes = [];
    types = types || [];
    for (i = 0; i < types.length; i++) {
      menuTypes.push(this.objectType + '.' + types[i]);
    }
    return menuTypes;
  }

  /**
   * The separator here does not exist in the model delivered by the server-side client.
   * The createdBy property is added to the model to find and destroy items added by the UI later.
   */
  protected _createSeparator(): Menu {
    return scout.create(Menu, {
      parent: this.menuBar,
      createdBy: this,
      separator: true
    });
  }
}
