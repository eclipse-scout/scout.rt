scout.GroupBoxMenuItemsOrder = function() {
};

scout.GroupBoxMenuItemsOrder.prototype.order = function(items) {
  var leftButtons = [],
    leftMenus = [],
    rightButtons = [],
    rightMenus = [];

  items.forEach(function(item) {
    if (scout.menus.isButton(item)) {
      // Buttons have no property 'horizontalAlignment' but a corresponding field on the gridData
      if (item.gridData && item.gridData.horizontalAlignment === 1) {
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
    right: rightButtons.concat(rightMenus)
  };
};

