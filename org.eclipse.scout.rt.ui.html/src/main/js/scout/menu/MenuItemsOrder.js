scout.MenuItemsOrder = function(session, objectType) {
  this.session = session;
  this.objectType = objectType;
};

scout.MenuItemsOrder.prototype.order = function(items) {
  var buttons = [],
    emptySpaceItems = [],
    selectionItems = [],
    rightItems = [];

  items.forEach(function(item) {
    if (scout.menus.isButton(item)) {
      buttons.push(item);
    } else if (scout.menus.checkType(item, this._menuTypes('EmptySpace'))) {
      emptySpaceItems.push(item);
    } else if (scout.menus.checkType(item, this._menuTypes('SingleSelection', 'MultiSelection'))) {
      selectionItems.push(item);
    } else if (scout.menus.checkType(item, this._menuTypes('Header'))) {
      rightItems.push(item);
    }
  }, this);

  // add fixed separator between emptySpace and selection
  if (emptySpaceItems.length > 0 && selectionItems.length > 0) {
    emptySpaceItems.push(this.createSeparator());
  }

  return {
    left: buttons.concat(emptySpaceItems, selectionItems),
    right: rightItems
  };
};

scout.MenuItemsOrder.prototype._menuTypes = function() {
  var i, menuTypes = [];
  for (i = 0; i < arguments.length; i++) {
    menuTypes.push(this.objectType + '.' + arguments[i]);
  }
  return menuTypes;
};

/**
 * The separator here does not exist in the model delivered by the server-side client.
 */
scout.MenuItemsOrder.prototype.createSeparator = function() {
  return this.session.createUiObject({
    objectType: 'Menu',
    separator: true
  });
};
