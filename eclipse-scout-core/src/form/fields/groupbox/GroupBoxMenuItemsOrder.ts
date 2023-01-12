/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Menu, MenuOrder, OrderedMenuItems} from '../../../index';

export class GroupBoxMenuItemsOrder implements MenuOrder {

  /**
   * Sorts the given menus item by horizontal alignment and divides the items in two groups for each alignment.
   * The result looks as follows:
   *
   * <em>
   * [horizontalAlignment=-1|0]    [horizontalAlignment=1]
   * [  buttons  ]  [  menus  ]    [ menus ]   [ buttons ]
   * </em>
   *
   * The buttons are always on the outer side of the group-box, the menus are on the inner side.
   */
  order(items: Menu[]): OrderedMenuItems {
    let leftButtons: Menu[] = [],
      leftMenus: Menu[] = [],
      rightButtons: Menu[] = [],
      rightMenus: Menu[] = [];

    items.forEach(item => {
      if (item.isButton()) {
        let horizontalAlignment = item.horizontalAlignment;
        if (horizontalAlignment === 1) {
          rightButtons.push(item);
        } else { // also 0
          leftButtons.push(item);
        }
      } else {
        if (item.horizontalAlignment === 1) {
          rightMenus.push(item);
        } else { // also 0
          leftMenus.push(item);
        }
      }
    });

    return {
      left: leftButtons.concat(leftMenus),
      right: rightMenus.concat(rightButtons),
      all: leftButtons.concat(leftMenus).concat(rightMenus).concat(rightButtons)
    };
  }
}
