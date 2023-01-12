/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, EllipsisMenu, InitModelOf, Menu, MenuDestinations, scout} from '../index';
import $ from 'jquery';

export type MenuFilterOptions = {
  onlyVisible?: boolean;
  enableDisableKeyStrokes?: boolean;
  notAllowedTypes?: string | string[];
  defaultMenuTypes?: string | string[];
};

export const menus = {
  filterAccordingToSelection(prefix: string, selectionLength: number, menuArr: Menu[], destination: MenuDestinations, options?: MenuFilterOptions): Menu[] {
    let allowedTypes: string[] = [],
      {notAllowedTypes} = options || {};

    if (destination === MenuDestinations.MENU_BAR) {
      allowedTypes = [prefix + '.EmptySpace', prefix + '.SingleSelection', prefix + '.MultiSelection'];
    } else if (destination === MenuDestinations.CONTEXT_MENU) {
      allowedTypes = [prefix + '.SingleSelection', prefix + '.MultiSelection'];
    } else if (destination === MenuDestinations.HEADER) {
      allowedTypes = [prefix + '.Header'];
    }

    if (allowedTypes.indexOf(prefix + '.SingleSelection') > -1 && selectionLength !== 1) {
      arrays.remove(allowedTypes, prefix + '.SingleSelection');
    }
    if (allowedTypes.indexOf(prefix + '.MultiSelection') > -1 && selectionLength <= 1) {
      arrays.remove(allowedTypes, prefix + '.MultiSelection');
    }
    notAllowedTypes = arrays.ensure(notAllowedTypes);
    let fixedNotAllowedTypes = [];
    // ensure prefix
    prefix = prefix + '.';
    notAllowedTypes.forEach(type => {
      if (type.slice(0, prefix.length) !== prefix) {
        type = prefix + type;
      }
      fixedNotAllowedTypes.push(type);
    }, this);
    return menus.filter(menuArr, allowedTypes, $.extend({}, options, {notAllowedTypes: fixedNotAllowedTypes}));
  },

  /**
   * Filters menus that don't match the given types, or in other words: only menus with the given types are returned
   * from this method. The visible state is only checked if the parameter onlyVisible is set to true. Otherwise, invisible items are returned and added to the
   * menu-bar DOM (invisible, however). They may change their visible state later. If there are any types in notAllowedTypes each menu is checked also against
   * these types and if they are matching the menu is filtered.
   */
  filter(menuArr: Menu[], types?: string | string[], options?: MenuFilterOptions): Menu[] {
    if (!menuArr) {
      return;
    }
    types = arrays.ensure(types);
    let {onlyVisible, enableDisableKeyStrokes, notAllowedTypes, defaultMenuTypes} = options || {};
    notAllowedTypes = arrays.ensure(notAllowedTypes);
    defaultMenuTypes = arrays.ensure(defaultMenuTypes);

    let filteredMenus = [], separatorCount = 0;

    menuArr.forEach(menu => {
      let childMenus = menu.childActions;
      if (childMenus.length > 0) {
        childMenus = menus.filter(childMenus, types, options);
        if (childMenus.length === 0) {
          menus._enableDisableMenuKeyStroke(menu, enableDisableKeyStrokes, true);
          return;
        }
      } else if (!menus._checkType(menu, types as string[], defaultMenuTypes) || (notAllowedTypes.length !== 0 && menus._checkType(menu, notAllowedTypes as string[], defaultMenuTypes))) {
        // Don't check the menu type for a group
        menus._enableDisableMenuKeyStroke(menu, enableDisableKeyStrokes, true);
        return;
      }

      if (onlyVisible && !menu.visible) {
        menus._enableDisableMenuKeyStroke(menu, enableDisableKeyStrokes, true);
        return;
      }
      if (menu.separator) {
        separatorCount++;
      }
      menus._enableDisableMenuKeyStroke(menu, enableDisableKeyStrokes, false);
      filteredMenus.push(menu);
    });

    // Ignore menus with only separators
    if (separatorCount === filteredMenus.length) {
      return [];
    }
    return filteredMenus;
  },

  /**
   * Makes leading, trailing and duplicate separators invisible or reverts the visibility change if needed.
   */
  updateSeparatorVisibility(menuArr: Menu | Menu[]) {
    menuArr = arrays.ensure(menuArr);
    menuArr = menuArr.filter(menu => menu.visible || menu.separator);
    if (menuArr.length === 0) {
      return;
    }

    let hasMenuBefore = false;
    let hasMenuAfter = false;
    menuArr.forEach((menu: Menu & { visibleOrig: boolean }, i: number) => {
      if (menu.ellipsis) {
        return;
      }
      if (!menu.separator) {
        hasMenuBefore = true;
        return;
      }
      hasMenuAfter = menuArr[i + 1] && !menuArr[i + 1].separator && !menuArr[i + 1].ellipsis;

      // If the separator has a separator next to it, make it invisible
      if (!hasMenuBefore || !hasMenuAfter) {
        if (menu.visibleOrig === undefined) {
          menu.visibleOrig = menu.visible;
          menu.setVisible(false);
        }
      } else if (menu.visibleOrig !== undefined) {
        // Revert to original state
        menu.setVisible(menu.visibleOrig);
        menu.visibleOrig = undefined;
      }
    });
  },

  checkType(menu: Menu, types: string | string[], defaultMenuTypes: string | string[] = []): boolean {
    types = arrays.ensure(types);
    if (menu.childActions.length > 0) {
      let childMenus = menus.filter(menu.childActions, types);
      return childMenus.length > 0;
    }
    return menus._checkType(menu, types, defaultMenuTypes);
  },

  /** @internal */
  _enableDisableMenuKeyStroke(menu: Menu, activated: boolean, exclude: boolean) {
    if (activated) {
      menu.excludedByFilter = exclude;
    }
  },

  /**
   * Checks the type of menu. Don't use this for menu groups.
   * @internal
   */
  _checkType(menu: Menu, types: string[], defaultMenuTypes: string | string[] = []): boolean {
    if (!types || types.length === 0) {
      return false;
    }
    let menuTypes = arrays.ensure(menu.menuTypes);
    defaultMenuTypes = arrays.ensure(defaultMenuTypes);
    if (menuTypes.length === 0) {
      menuTypes = defaultMenuTypes;
    }
    for (let j = 0; j < types.length; j++) {
      if (menuTypes.indexOf(types[j]) > -1) {
        return true;
      }
    }
    return false;
  },

  createEllipsisMenu(options: InitModelOf<EllipsisMenu>): EllipsisMenu {
    return scout.create(EllipsisMenu, options);
  },

  moveMenuIntoEllipsis(menu: Menu, ellipsis: EllipsisMenu) {
    menu.remove();
    menu._setOverflown(true);
    menu.overflowMenu = ellipsis;

    let menusInEllipsis = ellipsis.childActions.slice();
    menusInEllipsis.unshift(menu); // add as first element
    ellipsis.setChildActions(menusInEllipsis);
  },

  removeMenuFromEllipsis(menu: Menu, $parent?: JQuery) {
    menu._setOverflown(false);
    menu.overflowMenu = null;
    if (!menu.rendered) {
      menu.render($parent);
    }
  }
};
