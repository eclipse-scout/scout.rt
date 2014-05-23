scout.menus = {
  filter: function(menus, types) {
    if (!menus) {
      return;
    }
    if (types && !Array.isArray(types)) {
      types = [types];
    }

    var filteredMenus = [];
    var hasValidMenus = false;
    var separatorCount = 0;
    for (var i = 0; i < menus.length; i++) {
      var menu = menus[i];

      var childMenus = menu.childMenus;
      if (childMenus && childMenus.length > 0) {
        childMenus = scout.menus.filter(menu.childMenus, types);
        if (childMenus.length === 0) {
          continue;
        }
      }

      if (!menu.visible) {
        continue;
      }
      if (!scout.menus.checkType(menu, types)) {
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
  },
  checkType: function(menu, types) {
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
  }
};
