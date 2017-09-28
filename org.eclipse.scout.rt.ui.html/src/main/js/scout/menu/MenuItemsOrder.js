/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.MenuItemsOrder = function(session, objectType) {
  this.session = session;
  this.objectType = objectType;
  this.emptySpaceTypes = ['EmptySpace'];
  this.selectionTypes = ['SingleSelection', 'MultiSelection'];
};

scout.MenuItemsOrder.prototype.order = function(items) {
  var buttons = [],
    emptySpaceItems = [],
    selectionItems = [],
    rightItems = [];

  var isEmptyspaceMenuVisible = false,
  isSelectionMenuVisible =false;
  items.forEach(function(item) {
    // skip separators added dynamically by this class
    if (item.createdBy === this) {
      return;
    }
    if (item.isButton()) {
      buttons.push(item);
    } else if (item.horizontalAlignment === 1) {
      rightItems.push(item);
    } else if (scout.menus.checkType(item, this._menuTypes(this.emptySpaceTypes))) {
      if (item.visible) {
        isEmptyspaceMenuVisible = true;
      }
      emptySpaceItems.push(item);
    } else if (scout.menus.checkType(item, this._menuTypes(this.selectionTypes))) {
      if (item.visible) {
        isSelectionMenuVisible = true;
      }
      selectionItems.push(item);
    }
  }, this);

  // add fixed separator between emptySpace and selection
  if (isEmptyspaceMenuVisible && isSelectionMenuVisible) {
    emptySpaceItems.push(this._createSeparator());
  }

  return {
    left: buttons.concat(emptySpaceItems, selectionItems),
    right: rightItems
  };
};

scout.MenuItemsOrder.prototype._menuTypes = function(types) {
  var i, menuTypes = [];
  types = types || [];
  for (i = 0; i < types.length; i++) {
    menuTypes.push(this.objectType + '.' + types[i]);
  }
  return menuTypes;
};

/**
 * The separator here does not exist in the model delivered by the server-side client.
 * The createdBy property is added to the model to find and destroy items added by the UI later.
 */
scout.MenuItemsOrder.prototype._createSeparator = function() {
  return scout.create('Menu', {
    parent: this.menuBar,
    createdBy: this,
    separator: true
  });
};
