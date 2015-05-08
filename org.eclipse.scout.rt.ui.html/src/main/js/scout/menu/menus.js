scout.menus = {

  /**
   * @memberOf scout.menus
   */
  CLOSING_EVENTS: 'mousedown.contextMenu keydown.contextMenu', //FIXME keydown/keyup is a bad idea -> interferes with ctrl click on table to multi select rows

  /**
   * Filters menus that don't match the given types, or in other words: only menus with the given types are returned
   * from this method. Invisible items are returned and added to the menu-bar DOM (invisible, however). They may change their visible state later.
   */
  filter: function(menus, types) {
    if (!menus) {
      return;
    }
    types = scout.arrays.ensure(types);

    var filteredMenus = [],
      separatorCount = 0;


    menus.forEach(function(menu) {
      var childMenus = menu.childActions;
      if (childMenus.length > 0) {
        childMenus = scout.menus.filter(childMenus, types);
        if (childMenus.length === 0) {
          return;
        }
      } // Don't check the menu type for a group
      else if (!scout.menus._checkType(menu, types)) {
        return;
      }
      if (menu.separator) {
        separatorCount++;
      }
      filteredMenus.push(menu);
    });

    // Ignore menus with only separators
    if (separatorCount === filteredMenus.length) {
      return [];
    }

    return filteredMenus;
  },

  checkType: function(menu, types) {
    types = scout.arrays.ensure(types);
    if (menu.childActions.length > 0) {
      var childMenus = scout.menus.filter(menu.childActions, types);
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

  isButton: function(obj) {
    return obj instanceof scout.Button;
  }

};
