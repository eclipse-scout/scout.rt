// left1: buttons (left aligned)
// left2: menus (left aligned)
// right2: menus (right aligned)
// right1: buttons (right aligned)
scout.GroupBoxMenuItemsOrder = {
  order: function(items) {
    var i, item,
      left1Items = [],
      left2Items = [],
      right1Items = [],
      right2Items = [];

    for (i = 0; i < items.length; i++) {
      item = items[i];
      if (scout.menus.isButton(item)) {
        if (item.horizontalAlignment === 1) {
          right1Items.push(item);
        }
        else { // also 0
          left1Items.push(item);
        }
      }
      else {
        if (item.horizontalAlignment === 1) {
          right2Items.push(item);
        }
        else { // also 0
          left2Items.push(item);
        }
      }
    }
    return {
      left: left1Items.concat(left2Items),
      right: right1Items.concat(right2Items)
    };
  }
};

