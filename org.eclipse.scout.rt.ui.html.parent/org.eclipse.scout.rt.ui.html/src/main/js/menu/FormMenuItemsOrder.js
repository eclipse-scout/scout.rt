// left1: buttons (left aligned)
// left2: menus
// right: buttons (right aligned)
scout.FormMenuItemsOrder = {
  order: function(items) {
    var i, item,
      left1Items = [],
      left2Items = [],
      rightItems = [];

    for (i = 0; i < items.length; i++) {
      item = items[i];
      if (scout.menus.isButton(item)) {
        if (item.horizontalAlignment === 1) {
          rightItems.push(item);
        }
        else { // also 0
          left1Items.push(item);
        }
      }
      else {
        // FIXME AWE: (menu) check if it is a tool-menu to right align menus!
        // ... oder einfach im Form Table.Header menus rechts oben rendern
        left1Items.push(item);
      }
    }
    return {
      left: left1Items.concat(left2Items),
      right: rightItems
    };
  }
};

