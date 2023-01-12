/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Menu, MenuBar, menus, ObjectWithType, scout, Session} from '../index';

export interface MenuOrder {
  order(items: Menu[]): OrderedMenuItems;
}

export type OrderedMenuItems = { left: Menu[]; right: Menu[]; all: Menu[] };

export class MenuItemsOrder implements MenuOrder, ObjectWithType {
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

  protected _menuTypes(types?: string[]): string[] {
    let menuTypes: string[] = [];
    types = types || [];
    for (let i = 0; i < types.length; i++) {
      menuTypes.push(this.objectType + '.' + types[i]);
    }
    return menuTypes;
  }

  /**
   * The separator here does not exist in the model delivered by the server-side client.
   * The createdBy property is added to the model to find and destroy items added by the UI later.
   */
  protected _createSeparator(): Menu {
    let separator = scout.create(Menu, {
      parent: this.menuBar,
      separator: true
    });
    separator.createdBy = this;
    return separator;
  }
}
