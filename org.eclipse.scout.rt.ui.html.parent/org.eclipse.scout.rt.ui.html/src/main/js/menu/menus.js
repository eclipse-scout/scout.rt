scout.menus = {

  /**
   * @memberOf scout.menus
   */
  CLOSING_EVENTS: 'mousedown.contextMenu keydown.contextMenu', //FIXME keydown/keyup is a bad idea -> interferes with ctrl click on table to multi select rows

  /**
   * Filters menus that don't match the given types, or in other words: only menus with the given types are returned
   * from this method. The visible state is only checked if the parameter onlyVisible is set to true. Otherwise invisible items are returned and added to the
   * menu-bar DOM (invisible, however). They may change their visible state later.
   */
  filter: function(menus, types, onlyVisible) {
    if (!menus) {
      return;
    }
    types = scout.arrays.ensure(types);

    var i, menu, childMenus,
      filteredMenus = [],
      separatorCount = 0;

    for (i = 0; i < menus.length; i++) {
      menu = menus[i];
      childMenus = menu.childMenus;
      if (childMenus.length > 0) {
        childMenus = scout.menus.filter(menu.childMenus, types);
        if (childMenus.length === 0) {
          continue;
        }
      } // Don't check the menu type for a group
      else if (!scout.menus._checkType(menu, types)) {
        continue;
      }

      if (onlyVisible && !menu.visible) {
        continue;
      }

      if (menu.separator) {
        separatorCount++;
      }

      filteredMenus.push(menu);
    }

    // Ignore menus with only separators
    if (separatorCount === filteredMenus.length) {
      return [];
    }

    return filteredMenus;
  },

  checkType: function(menu, types) {
    var childMenus;
    types = scout.arrays.ensure(types);

    if (menu.childMenus.length > 0) {
      childMenus = scout.menus.filter(menu.childMenus, types);
      return (childMenus.length > 0);
    }

    return scout.menus._checkType(menu, types);
  },

  /**
   * Checks the type of a menu. Don't use this for menu groups.
   */
  _checkType: function(menu, types) {
    if (!types || types.length === 0) {
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
  },

  /**
   * Appends menu items to the given popup and attaches event-handlers on the appended menu items.
   *
   * @param $parent Parent to which the popup is appended
   * @param menus Menus added to the popup
   * @returns
   */
  appendMenuItems: function(popup, menus) {
    if (!menus || menus.length === 0) {
      return;
    }
    var i,
      onMenuItemClicked = function() {
        var menu = $(this).data('menu');
        popup.remove();
        menu.sendDoAction();
      };

    for (i = 0; i < menus.length; i++) {
      var menu = menus[i];
      if (menu.separator) {
        continue;
      }
      menu.sendAboutToShow();
      popup.appendToBody(
        $.makeDiv('menu-item').text(menu.text).data('menu', menu).on('click', '', onMenuItemClicked).one(scout.menus.CLOSING_EVENTS, $.suppressEvent));
    }
  },

  isButton: function(obj) {
    return obj instanceof scout.Button;
  }

};
