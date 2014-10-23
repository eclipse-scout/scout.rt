scout.menus = function() {
};

scout.menus.CLOSING_EVENTS = 'mousedown.contextMenu keydown.contextMenu mousewheel.contextMenu';

scout.menus.filter = function(menus, types) {
  if (!menus) {
    return;
  }
  if (types && !Array.isArray(types)) {
    types = [types];
  }

  var filteredMenus = [];
  var separatorCount = 0;
  for (var i = 0; i < menus.length; i++) {
    var menu = menus[i];

    var childMenus = menu.childMenus;
    if (childMenus.length > 0) {
      childMenus = scout.menus.filter(menu.childMenus, types);
      if (childMenus.length === 0) {
        continue;
      }
    } //don't check the menu type for a group
    else if (!scout.menus._checkType(menu, types)) {
      continue;
    }

    if (!menu.visible) {
      continue;
    }

    if (menu.separator) {
      separatorCount++;
    }

    filteredMenus.push(menu);
  }

  //Ignore menus with only separators
  if (separatorCount == filteredMenus.length) {
    return [];
  }

  return filteredMenus;
};

scout.menus.checkType = function(menu, types) {
  var childMenus;
  if (types && !Array.isArray(types)) {
    types = [types];
  }

  if (menu.childMenus.length > 0) {
    childMenus = scout.menus.filter(menu.childMenus, types);
    return (childMenus.length > 0);
  }

  return scout.menus._checkType(menu, types);
};

/**
 * Checks the type of a menu. Don't use this for menu groups.
 */
scout.menus._checkType = function(menu, types) {
  if (!types) {
    return true;
  }
  if (!menu.menuTypes) {
    return false;
  }
  for (var j = 0; j < types.length; j++) {
    if (menu.menuTypes.indexOf(types[j]) > -1) {
      return true;
    }
  }
};

/**
 * Appends menu items to the given popup and attaches event-handlers on the appended menu items.
 *
 * @param $parent Parent to which the popup is appended
 * @param menus Menus added to the popup
 * @returns
 */
scout.menus.appendMenuItems = function(popup, menus) {
  if (!menus || menus.length === 0) {
    return;
  }
  var i,
    onMenuItemClicked = function() {
      var menu = $(this).data('menu');
      popup.remove();
      menu.sendMenuAction();
    };

  for (i = 0; i < menus.length; i++) {
    var menu = menus[i];
    if (menu.separator) {
      continue;
    }
    menu.sendAboutToShow();
    popup.appendToBody(
      $('<div>').
        addClass('menu-item').
        text(menu.text).
        data('menu', menu).
        on('click', '', onMenuItemClicked).
        one(scout.menus.CLOSING_EVENTS, $.suppressEvent));
  }
};

