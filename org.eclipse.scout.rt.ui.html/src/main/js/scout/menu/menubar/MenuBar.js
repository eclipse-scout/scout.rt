/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.MenuBar = function() {
  scout.MenuBar.parent.call(this);

  this.menuSorter = null;
  this.menuFilter = null;
  this.position = 'top'; // or 'bottom'
  this.size = 'small'; // or 'large'
  this.tabbable = true;
  this.menuboxLeft = null;
  this.menuboxRight = null;
  this.menuItems = []; // original list of menuItems that was passed to setMenuItems(), only used to check if menubar has changed
  this.orderedMenuItems = {
    left: [],
    right: [],
    all: []
  };
  this.defaultMenu = null;
  this.visible = false;
  this.ellipsisMenuPosition = scout.MenuBar.EllipsisPosition.RIGHT;

  this._menuItemPropertyChangeHandler = this._onMenuItemPropertyChange.bind(this);

  this._addWidgetProperties('menuItems');
};
scout.inherits(scout.MenuBar, scout.Widget);

scout.MenuBar.EllipsisPosition = {
  LEFT: 'left',
  RIGHT: 'right'
};

scout.MenuBar.prototype._init = function(options) {
  scout.MenuBar.parent.prototype._init.call(this, options);

  this.menuSorter = options.menuOrder || new scout.GroupBoxMenuItemsOrder();
  this.menuSorter.menuBar = this;
  if (options.menuFilter) {
    this.menuFilter = function(menus, destination, onlyVisible, enableDisableKeyStroke) {
      return options.menuFilter(menus, scout.MenuDestinations.MENU_BAR, onlyVisible, enableDisableKeyStroke);
    };
  }

  this.menuboxLeft = scout.create('MenubarBox', {
    parent: this,
    cssClass: 'left',
    position: this.position
  });
  this.menuboxRight = scout.create('MenubarBox', {
    parent: this,
    cssClass: 'right',
    position: this.position
  });

  this._setMenuItems(scout.arrays.ensure(this.menuItems));
  this.updateVisibility();
  this.htmlComp = new scout.HtmlComponent(null, this.session);
};

scout.MenuBar.prototype._destroy = function() {
  scout.MenuBar.parent.prototype._destroy.call(this);
  this._detachMenuHandlers();
};

/**
 * @override
 */
scout.MenuBar.prototype._createKeyStrokeContext = function() {
  return new scout.KeyStrokeContext();
};

/**
 * @override
 */
scout.MenuBar.prototype._initKeyStrokeContext = function() {
  scout.MenuBar.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke([
    new scout.MenuBarLeftKeyStroke(this),
    new scout.MenuBarRightKeyStroke(this)
  ]);
};

/**
 * @override Widget.js
 */
scout.MenuBar.prototype._render = function() {
  this.$container = this.$parent.makeDiv('menubar')
    .toggleClass('main-menubar', this.size === 'large');

  this.htmlComp.bind(this.$container);
  this.htmlComp.setLayout(new scout.MenuBarLayout(this));

  if (this.position === 'top') {
    this.$parent.prepend(this.$container);
  } else {
    this.$container.addClass('bottom');
    this.$parent.append(this.$container);
  }
  this.menuboxRight.render(this.$container);
  this.menuboxLeft.render(this.$container);
};

scout.MenuBar.prototype._renderProperties = function() {
  scout.MenuBar.parent.prototype._renderProperties.call(this);
  this._renderMenuItems();
};

scout.MenuBar.prototype._remove = function() {
  scout.MenuBar.parent.prototype._remove.call(this);
  this._removeMenuItems();
};

scout.MenuBar.prototype.bottom = function() {
  this.position = 'bottom';
};

scout.MenuBar.prototype.top = function() {
  this.position = 'top';
};

scout.MenuBar.prototype.large = function() {
  this.size = 'large';
};

scout.MenuBar.prototype.ellipsisRight = function() {
  this.ellipsisMenuPosition = scout.MenuBar.EllipsisPosition.RIGHT;
};

scout.MenuBar.prototype.ellipsisLeft = function() {
  this.ellipsisMenuPosition = scout.MenuBar.EllipsisPosition.LEFT;
};

/**
 * Set the filter of the menu bar to all the menu items.
 */
scout.MenuBar.prototype._setChildMenuFilters = function() {
  this.orderedMenuItems.all.forEach(function(item) {
    item.setMenuFilter(this.menuFilter);
  }, this);
};

/**
 * This function can be called multiple times. The function attaches the menu handlers only if they are not yet added.
 */
scout.MenuBar.prototype._attachMenuHandlers = function() {
  this.orderedMenuItems.all.forEach(function(item) {
    if (item.events.count('propertyChange', this._menuItemPropertyChangeHandler) === 0) {
      item.on('propertyChange', this._menuItemPropertyChangeHandler);
    }
  }, this);
};

scout.MenuBar.prototype._detachMenuHandlers = function() {
  this.orderedMenuItems.all.forEach(function(item) {
    item.off('propertyChange', this._menuItemPropertyChangeHandler);
  }.bind(this));
};

scout.MenuBar.prototype.setMenuItems = function(menuItems) {
  menuItems = scout.arrays.ensure(menuItems);
  if (scout.arrays.equals(this.menuItems, menuItems)) {
    // Ensure existing menus are correctly linked even if the given menuItems are the same (see TableSpec for reasons)
    this.menuboxRight.link(this.menuboxRight.menuItems);
    this.menuboxLeft.link(this.menuboxLeft.menuItems);
    return;
  }
  this.setProperty('menuItems', menuItems);
};

scout.MenuBar.prototype._setMenuItems = function(menuItems, rightFirst) {
  // remove property listeners of old menu items.
  this._detachMenuHandlers();

  this.orderedMenuItems = this._createOrderedMenus(menuItems);

  if (rightFirst) {
    this.menuboxRight.setMenuItems(this.orderedMenuItems.right);
    this.menuboxLeft.setMenuItems(this.orderedMenuItems.left);

  } else {
    this.menuboxLeft.setMenuItems(this.orderedMenuItems.left);
    this.menuboxRight.setMenuItems(this.orderedMenuItems.right);
  }

  this._setChildMenuFilters();
  this._attachMenuHandlers();
  this.updateVisibility();
  this.updateDefaultMenu();
  this._updateTabbableMenu();

  this._setProperty('menuItems', menuItems);
};

scout.MenuBar.prototype._renderMenuItems = function() {
  this._attachMenuHandlers();
  this.updateLastItemMarker();
  this.updateLeftOfButtonMarker();
  this.invalidateLayoutTree();
};

scout.MenuBar.prototype._removeMenuItems = function() {
  this._detachMenuHandlers();
};

scout.MenuBar.prototype._createOrderedMenus = function(menuItems) {
  var orderedMenuItems = this.menuSorter.order(menuItems, this),
    ellipsisIndex = -1,
    ellipsis;
  orderedMenuItems.right.forEach(function(item) {
    item.rightAligned = true;
  });

  if (orderedMenuItems.all.length > 0) {
    ellipsis = scout.create('EllipsisMenu', {
      parent: this,
      cssClass: 'overflow-menu-item'
    });
    this._ellipsis = ellipsis;

    // add ellipsis to the correct position
    if (this.ellipsisMenuPosition === scout.MenuBar.EllipsisPosition.RIGHT) {
      // try right
      var reverseIndexPosition = this._getFirstStackableIndexPosition(orderedMenuItems.right.slice().reverse());
      if (reverseIndexPosition > -1) {
        ellipsisIndex = orderedMenuItems.right.length - reverseIndexPosition;
        ellipsis.rightAligned = true;
        orderedMenuItems.right.splice(ellipsisIndex, 0, ellipsis);
      } else {
        // try left
        reverseIndexPosition = this._getFirstStackableIndexPosition(orderedMenuItems.left.slice().reverse());
        if (reverseIndexPosition > -1) {
          ellipsisIndex = orderedMenuItems.left.length - reverseIndexPosition;
          orderedMenuItems.left.splice(ellipsisIndex, 0, ellipsis);
        }
      }
    } else {
      // try left
      ellipsisIndex = this._getFirstStackableIndexPosition(orderedMenuItems.left);
      if (ellipsisIndex > -1) {
        orderedMenuItems.left.splice(ellipsisIndex, 0, ellipsis);
      } else {
        // try right
        ellipsisIndex = this._getFirstStackableIndexPosition(orderedMenuItems.right);
        if (ellipsisIndex > -1) {
          ellipsis.rightAligned = true;
          orderedMenuItems.right.splice(ellipsisIndex, 0, ellipsis);
        }
      }
    }
    orderedMenuItems.all = orderedMenuItems.left.concat(orderedMenuItems.right);
  }
  return orderedMenuItems;
};

scout.MenuBar.prototype._getFirstStackableIndexPosition = function(menuList) {
  var foundIndex = -1;
  menuList.some(function(menu, index) {
    if (menu.stackable && menu.visible) {
      foundIndex = index;
      return true;
    }
    return false;
  }, this);

  return foundIndex;
};

scout.MenuBar.prototype._updateTabbableMenu = function() {
  // Make first valid MenuItem tabbable so that it can be focused. All other items
  // are not tabbable. But they can be selected with the arrow keys.
  if (this.tabbable) {
    if (this.defaultMenu && this.defaultMenu.enabled) {
      this.setTabbableMenu(this.defaultMenu);
    } else {
      this.setTabbableMenu(scout.arrays.find(this.orderedMenuItems.all, function(item) {
        return item.isTabTarget();
      }));
    }
  }
};

scout.MenuBar.prototype.setTabbableMenu = function(menu) {
  if (!this.tabbable || menu === this.tabbableMenu) {
    return;
  }
  if (this.tabbableMenu) {
    this.tabbableMenu.setTabbable(false);
  }
  this.tabbableMenu = menu;
  if (menu) {
    menu.setTabbable(true);
  }
};

/**
 * Ensures that the last visible menu item (no matter if it is in the left or the right menu box)
 * has the class 'last' (to remove the margin-right). Call this method whenever the visibility of
 * single items change.
 */
scout.MenuBar.prototype.updateLastItemMarker = function() {
  // Remove the 'last' class from the current last visible item
  if (this._lastVisibleItem && this._lastVisibleItem.rendered) {
    this._lastVisibleItem.$container.removeClass('last');
  }
  this._lastVisibleItem = null;

  // Find the new last visible item (from left to right)
  var setLastVisibleItemFn = function(item) {
    if (item.visible) {
      this._lastVisibleItem = item;
    }
  }.bind(this);
  this.orderedMenuItems.left.forEach(setLastVisibleItemFn);
  this.orderedMenuItems.right.forEach(setLastVisibleItemFn);

  // Assign the class to the found item
  if (this._lastVisibleItem && this._lastVisibleItem.rendered) {
    this._lastVisibleItem.$container.addClass('last');
  }
};

scout.MenuBar.prototype.updateVisibility = function() {
  scout.menus.updateSeparatorVisibility(this.orderedMenuItems.all);
  this.setVisible(!this.hiddenByUi && this.orderedMenuItems.all.some(function(m) {
    return m.visible && !m.ellipsis;
  }));
};

/**
 * First rendered item that is enabled and reacts to ENTER keystroke shall be marked as 'defaultMenu'
 */
scout.MenuBar.prototype.updateDefaultMenu = function() {
  var i, item;
  var defaultMenu = null;
  for (i = 0; i < this.orderedMenuItems.all.length; i++) {
    item = this.orderedMenuItems.all[i];

    if (!item.visible || !item.enabled || item.defaultMenu === false) {
      // Invisible or disabled menus and menus that explicitly have the "defaultMenu"
      // property set to false cannot be the default menu.
      continue;
    }
    if (item.defaultMenu) {
      defaultMenu = item;
      break;
    }
    if (!defaultMenu && this._isDefaultKeyStroke(item.actionKeyStroke)) {
      defaultMenu = item;
    }
  }

  this.setDefaultMenu(defaultMenu);
  if (defaultMenu && defaultMenu.isTabTarget()) {
    this.setTabbableMenu(defaultMenu);
  }
};

scout.MenuBar.prototype._isDefaultKeyStroke = function(keyStroke) {
  return scout.isOneOf(scout.keys.ENTER, keyStroke.which) &&
    !keyStroke.ctrl &&
    !keyStroke.alt &&
    !keyStroke.shift;
};

scout.MenuBar.prototype.setDefaultMenu = function(defaultMenu) {
  this.setProperty('defaultMenu', defaultMenu);
};

scout.MenuBar.prototype._setDefaultMenu = function(defaultMenu) {
  if (this.defaultMenu) {
    this.defaultMenu.setMenuStyle(scout.Menu.MenuStyle.NONE);
  }
  if (defaultMenu) {
    defaultMenu.setMenuStyle(scout.Menu.MenuStyle.DEFAULT);
  }
  this._setProperty('defaultMenu', defaultMenu);
};

/**
 * Add class 'left-of-button' to every menu item which is on the left of a button
 */
scout.MenuBar.prototype.updateLeftOfButtonMarker = function() {
  this._updateLeftOfButtonMarker(this.orderedMenuItems.left);
  this._updateLeftOfButtonMarker(this.orderedMenuItems.right);
};

scout.MenuBar.prototype._updateLeftOfButtonMarker = function(items) {
  var item, previousItem;

  items = items.filter(function(item) {
    return item.visible && item.rendered;
  });

  for (var i = 0; i < items.length; i++) {
    item = items[i];
    item.$container.removeClass('left-of-button');
    if (i > 0 && item.isButton()) {
      previousItem = items[i - 1];
      previousItem.$container.addClass('left-of-button');
    }
  }
};

scout.MenuBar.prototype._onMenuItemPropertyChange = function(event) {
  // We do not update the items directly, because this listener may be fired many times in one
  // user request (because many menus change one or more properties). Therefore, we just invalidate
  // the MenuBarLayout. It will be updated automatically after the user request has finished,
  // because the layout calls rebuildItemsInternal().
  if (event.propertyName === 'overflown' || event.propertyName === 'enabled' || event.propertyName === 'visible' || event.propertyName === 'hidden') {
    if (!this.tabbableMenu || event.source === this.tabbableMenu) {
      this._updateTabbableMenu();
    }
  }
  if (event.propertyName === 'overflown' || event.propertyName === 'hidden') {
    if (!this.defaultMenu || event.source === this.defaultMenu) {
      this.updateDefaultMenu();
    }
  }
  if (event.propertyName === 'horizontalAlignment') {
    // reorder
    this.reorderMenus(event.newValue <= 0);
  }
  if (event.propertyName === 'visible') {
    var oldVisible = this.visible;
    this.updateVisibility();
    if (!oldVisible && this.visible) {
      // If the menubar was previously invisible (because all menus were invisible) but
      // is now visible, the menuboxes and the menus have to be rendered now. Otherwise,
      // calculating the preferred size of the menubar, e.g. in the TableLayout, would
      // return the wrong value (even if the menubar itself is visible).
      this.revalidateLayout();
    }
    // recalculate position of ellipsis if any menu item changed visibility.
    // separators may change visibility during reordering menu items. Since separators do not have any
    // impact of right/left order of menu items they have not to be considered to enforce a reorder.
    if (!event.source.separator) {
      this.reorderMenus();
    }
  }
  if (event.propertyName === 'keyStroke' || event.propertyName === 'enabled' || event.propertyName === 'defaultMenu' || event.propertyName === 'visible') {
    this.updateDefaultMenu();
  }
};

scout.MenuBar.prototype.reorderMenus = function(rightFirst) {
  var menuItems = this.menuItems;
  this._setMenuItems(menuItems, rightFirst);
};
