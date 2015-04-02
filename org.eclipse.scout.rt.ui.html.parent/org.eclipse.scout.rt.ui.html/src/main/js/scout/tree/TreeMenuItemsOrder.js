// left 1: (static) buttons
// left 2: empty space menus
// left 3: selection menus (single/multi)
// right : header menus
scout.TreeMenuItemsOrder = {
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
      } else if (scout.menus.checkType(item, ['Tree.EmptySpace'])) {
        emptySpaceItems.push(item);
      } else if (scout.menus.checkType(item, ['Tree.SingleSelection', 'Tree.MultiSelection'])) {
        selectionItems.push(item);
      } else if (scout.menus.checkType(item, ['Tree.Header'])) {
        rightItems.push(item);
      }
    }

    // add fixed separator between emptySpace and selection
    if (emptySpaceItems.length > 0 && selectionItems.length > 0) {
      emptySpaceItems.push(scout.TreeMenuItemsOrder.createSeparator());
    }

    return {
      left: buttons.concat(emptySpaceItems, selectionItems),
      right: rightItems
    };
  },

  /**
   * The separator here does not exist in the model delivered by Scout.
   * That's why we must set some of the default model values here.
   */
  createSeparator: function() {
    // FIXME AWE: sequence nr. für client seitige adapter
    var separator = new scout.Menu();
    scout.defaultValues.applyTo(separator, 'Menu');
    separator.session = {};
    separator.separator = true;
    return separator;
  }
};
