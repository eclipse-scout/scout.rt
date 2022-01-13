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
import {arrays, MenuDestinations, scout} from '../index';

export function filterAccordingToSelection(prefix, selectionLength, menus, destination, onlyVisible, enableDisableKeyStroke, notAllowedTypes) {
  let allowedTypes = [];

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
  return filter(menus, allowedTypes, onlyVisible, enableDisableKeyStroke, fixedNotAllowedTypes);
}

/**
 * Filters menus that don't match the given types, or in other words: only menus with the given types are returned
 * from this method. The visible state is only checked if the parameter onlyVisible is set to true. Otherwise invisible items are returned and added to the
 * menu-bar DOM (invisible, however). They may change their visible state later. If there are any types in notAllowedTypes each menu is checked also against
 * these types and if they are matching the menu is filtered.
 */
export function filter(menus, types, onlyVisible, enableDisableKeyStrokes, notAllowedTypes) {
  if (!menus) {
    return;
  }
  types = arrays.ensure(types);
  notAllowedTypes = arrays.ensure(notAllowedTypes);

  let filteredMenus = [],
    separatorCount = 0;

  menus.forEach(menu => {
    let childMenus = menu.childActions;
    if (childMenus.length > 0) {
      childMenus = filter(childMenus, types, onlyVisible, enableDisableKeyStrokes, notAllowedTypes);
      if (childMenus.length === 0) {
        _enableDisableMenuKeyStroke(menu, enableDisableKeyStrokes, true);
        return;
      }
    } else if (!_checkType(menu, types) || (notAllowedTypes.length !== 0 && _checkType(menu, notAllowedTypes))) {
      // Don't check the menu type for a group
      _enableDisableMenuKeyStroke(menu, enableDisableKeyStrokes, true);
      return;
    }

    if (onlyVisible && !menu.visible) {
      _enableDisableMenuKeyStroke(menu, enableDisableKeyStrokes, true);
      return;
    }
    if (menu.separator) {
      separatorCount++;
    }
    _enableDisableMenuKeyStroke(menu, enableDisableKeyStrokes, false);
    filteredMenus.push(menu);
  });

  // Ignore menus with only separators
  if (separatorCount === filteredMenus.length) {
    return [];
  }
  return filteredMenus;
}

/**
 * Makes leading, trailing and duplicate separators invisible or reverts the visibility change if needed.
 */
export function updateSeparatorVisibility(menus) {
  menus = arrays.ensure(menus);

  menus = menus.filter(menu => {
    return menu.visible || menu.separator;
  });

  if (menus.length === 0) {
    return;
  }

  let hasMenuBefore = false;
  let hasMenuAfter = false;
  menus.forEach((menu, i) => {
    if (menu.ellipsis) {
      return;
    }
    if (!menu.separator) {
      hasMenuBefore = true;
      return;
    }
    hasMenuAfter = menus[i + 1] && !menus[i + 1].separator && !menus[i + 1].ellipsis;

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
}

export function checkType(menu, types) {
  types = arrays.ensure(types);
  if (menu.childActions.length > 0) {
    let childMenus = filter(menu.childActions, types);
    return (childMenus.length > 0);
  }
  return _checkType(menu, types);
}

export function _enableDisableMenuKeyStroke(menu, activated, exclude) {
  if (activated) {
    menu.excludedByFilter = exclude;
  }
}

/**
 * Checks the type of a menu. Don't use this for menu groups.
 */

export function _checkType(menu, types) {
  if (!types || types.length === 0) {
    return false;
  }
  if (!menu.menuTypes) {
    return false;
  }
  for (let j = 0; j < types.length; j++) {
    if (menu.menuTypes.indexOf(types[j]) > -1) {
      return true;
    }
  }
}

export function createEllipsisMenu(options) {
  return scout.create('EllipsisMenu', options);
}

export function moveMenuIntoEllipsis(menu, ellipsis) {
  menu.remove();
  menu._setOverflown(true);
  menu.overflowMenu = ellipsis;

  let menusInEllipsis = ellipsis.childActions.slice();
  menusInEllipsis.unshift(menu); // add as first element
  ellipsis.setChildActions(menusInEllipsis);
}

export function removeMenuFromEllipsis(menu, $parent) {
  menu._setOverflown(false);
  menu.overflowMenu = null;
  if (!menu.rendered) {
    menu.render($parent);
  }
}

export default {
  checkType,
  createEllipsisMenu,
  filter,
  filterAccordingToSelection,
  moveMenuIntoEllipsis,
  removeMenuFromEllipsis,
  updateSeparatorVisibility
};
