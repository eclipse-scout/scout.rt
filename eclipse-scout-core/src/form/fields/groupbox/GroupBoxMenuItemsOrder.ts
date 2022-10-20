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
export default class GroupBoxMenuItemsOrder {

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
  order(items) {
    let leftButtons = [],
      leftMenus = [],
      rightButtons = [],
      rightMenus = [];

    items.forEach(item => {
      if (item.isButton()) {
        let horizontalAlignment = item.horizontalAlignment;
        if (horizontalAlignment === undefined) {
          // Real buttons have no property 'horizontalAlignment' but a corresponding field on the gridData
          horizontalAlignment = (item.gridData && item.gridData.horizontalAlignment);
        }
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
