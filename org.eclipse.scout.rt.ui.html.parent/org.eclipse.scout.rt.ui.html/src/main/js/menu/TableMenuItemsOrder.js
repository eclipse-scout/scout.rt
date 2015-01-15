// left 1: (static) buttons
// left 2: empty space menus
// left 3: selection menus (single/multi)
// right : header menus
scout.TableMenuItemsOrder = {
  order: function(items) {
    var i, item,
      buttons = [],
      emptySpaceItems = [],
      selectionItems = [],
      rightItems = [];

    for (i = 0; i < items.length; i++) {
      item = items[i];
      if (scout.menus.isButton(item)) {
        buttons.push(item);
      }
      else if (scout.menus.checkType(item, ['Table.EmptySpace'])) {
        emptySpaceItems.push(item);
      }
      else if (scout.menus.checkType(item, ['Table.SingleSelection', 'Table.MultiSelection'])) {
        selectionItems.push(item);
      }
      else if (scout.menus.checkType(item, ['Table.Header'])) {
        rightItems.push(item);
      }
    }

    // add fixed separator between emptySpace and selection
    if (emptySpaceItems.length > 0 && selectionItems.length > 0) {
      var separator = new scout.Menu();
      separator.separator = true;
      emptySpaceItems.push(separator);
    }

    return {
      left: buttons.concat(emptySpaceItems, selectionItems),
      right: rightItems
    };
  }
};

