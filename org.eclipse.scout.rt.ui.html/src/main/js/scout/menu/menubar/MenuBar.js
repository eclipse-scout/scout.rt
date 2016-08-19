/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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

  this._addKeyStrokeContextSupport();
  this._addEventSupport();
  this.menuSorter;
  this.position = 'top'; // or 'bottom'
  this.size = 'small'; // or 'large'
  this.tabbable = true;
  this._internalMenuItems = []; // original list of menuItems that was passed to updateItems(), only used to check if menubar has changed
  this.menuItems = []; // list of menuItems (ordered, may contain additional UI separators, some menus may not be rendered)
  this._orderedMenuItems = {
    left: [],
    right: []
  }; // Object containing "left" and "right" menus
  this.defaultMenu = null;
  this.visible = false;
  this.ellipsis;

  /**
   * This array is === menuItems when menu-bar is not over-sized.
   * When the menu-bar is over-sized, we this property is set be the MenuBarLayout
   * which adds an additional ellipsis-menu, and removes menu items that doesn't
   * fit into the available menu-bar space.
   */
  this.visibleMenuItems = [];

  this._menuItemPropertyChangeListener = function(event) {
    // We do not update the items directly, because this listener may be fired many times in one
    // user request (because many menus change one or more properties). Therefore, we just invalidate
    // the MenuBarLayout. It will be updated automatically after the user request has finished,
    // because the layout calls rebuildItemsInternal().
    if (event.changedProperties.length > 0) {
      if (event.changedProperties.length === 1 && event.changedProperties[0] === 'enabled') {
        this.updateDefaultMenu();
      } else if (event.changedProperties.indexOf('visible') > -1) {
        this.updateVisibility();
      }
    }
  }.bind(this);
};
scout.inherits(scout.MenuBar, scout.Widget);

scout.MenuBar.prototype._init = function(options) {
  scout.MenuBar.parent.prototype._init.call(this, options);

  this.menuSorter = options.menuOrder;
  this.menuSorter.menuBar = this;
  this.menuFilter = options.menuFilter;
  this.updateVisibility();
};

scout.MenuBar.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  keyStrokeContext.registerKeyStroke([
    new scout.MenuBarLeftKeyStroke(this),
    new scout.MenuBarRightKeyStroke(this)
  ]);
};

/**
 * @override Widget.js
 */
scout.MenuBar.prototype._render = function($parent) {
  this.$container = $parent.makeDiv('menubar')
    .attr('id', 'MenuBar-' + scout.objectFactory.createUniqueId())
    .toggleClass('main-menubar', this.size === 'large');

  this.$left = this.$container.appendDiv('menubox left');
  new scout.HtmlComponent(this.$left, this.session);
  this.$right = this.$container.appendDiv('menubox right');
  new scout.HtmlComponent(this.$right, this.session);

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.MenuBarLayout(this));

  if (this.position === 'top') {
    $parent.prepend(this.$container);
  } else {
    this.$container.addClass('bottom');
    $parent.append(this.$container);
  }
  this.rebuildItemsInternal();
};

scout.MenuBar.prototype._remove = function() {
  scout.MenuBar.parent.prototype._remove.call(this);
  this._removeMenuItems();
  this.visibleMenuItems = [];
  this.visible = false;
};

/**
 * @override
 */
scout.MenuBar.prototype._renderVisible = function() {
  this.$container.setVisible(this.visible);
  this.invalidateLayoutTree();
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

scout.MenuBar.prototype._destroyMenuSorterSeparators = function() {
  this.menuItems.forEach(function(item) {
    if (item.createdBy === this.menuSorter) {
      item.destroy();
    }
  }, this);
};

/**
 * Forces the MenuBarLayout to be revalidated, which includes rebuilding the menu items.
 */
scout.MenuBar.prototype.rebuildItems = function(revalidateLayoutTree) {
  this.htmlComp.revalidateLayout(); // this will trigger rebuildItemsInternal()
};

/**
 * Rebuilds the menu items without relayouting the menubar.
 * Do not call this internal method from outside (except from the MenuBarLayout).
 */
scout.MenuBar.prototype.rebuildItemsInternal = function() {
  this._updateItems();
};

scout.MenuBar.prototype.setMenuItems = function(menuItems) {
  menuItems = scout.arrays.ensure(menuItems);
  // Only update if list of menus changed. Don't compare this.menuItems, because that list
  // may contain additional UI separators, and may not be in the same order
  var sameMenuItems = scout.arrays.equals(this._internalMenuItems, menuItems);

  if (!sameMenuItems) {
    if (this.rendered) {
      this._removeMenuItems();
    }

    // The menuSorter may add separators to the list of items -> destroy the old ones first
    this._destroyMenuSorterSeparators();
    this._internalMenuItems = menuItems;
    this._orderedMenuItems = this.menuSorter.order(menuItems, this);
    this.menuItems = this._orderedMenuItems.left.concat(this._orderedMenuItems.right);
    this.link(menuItems);
  }

  if (this.rendered) {
    var hasUnrenderedMenuItems = this.menuItems.some(function(elem) {
      return !elem.rendered;
    });
    if (!sameMenuItems || hasUnrenderedMenuItems) {
      this.rebuildItems(); // Re-layout menubar
    } else {
      // Don't rebuild menubar, but update "markers"
      this.updateVisibility();
      this.updateDefaultMenu();
      this.updateLastItemMarker();
    }
  }
};

scout.MenuBar.prototype._updateItems = function() {
  this._removeMenuItems();

  this.visibleMenuItems = this.menuItems;

  // Make sure menubar is visible before the items get rendered
  // especially important for menu items with open popups to position them correctly
  this.updateVisibility();

  this._renderMenuItems(this._orderedMenuItems.left, false);
  this._renderMenuItems(this._orderedMenuItems.right, true);
  this.updateDefaultMenu();
  this.updateLastItemMarker();

  // Make first valid MenuItem tabbable so that it can be focused. All other items
  // are not tabbable. But they can be selected with the arrow keys.
  if ((!this.defaultMenu || !this.defaultMenu.enabled) && this.tabbable) {
    this.menuItems.some(function(item) {
      if (item.isTabTarget()) {
        this.setTabbableMenu(item);
        return true;
      } else {
        return false;
      }
    }.bind(this));
  }
};

scout.MenuBar.prototype.setTabbableMenu = function(menu) {
  if (!this.tabbable || menu === this.tabbableMenu) {
    return;
  }
  if (this.tabbableMenu) {
    this.tabbableMenu.setTabbable(false);
  }
  menu.setTabbable(true);
  this.tabbableMenu = menu;
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
  this._orderedMenuItems.left.forEach(setLastVisibleItemFn);
  this._orderedMenuItems.right.forEach(setLastVisibleItemFn);

  // Assign the class to the found item
  if (this._lastVisibleItem && this._lastVisibleItem.rendered) {
    this._lastVisibleItem.$container.addClass('last');
  }
};

scout.MenuBar.prototype.updateVisibility = function() {
  this.setVisible(!this.hiddenByUi && this.menuItems.some(function(m) {
    return m.visible;
  }));
};

/**
 * First rendered item that is enabled and reacts to ENTER keystroke shall be marked as 'defaultMenu'
 */
scout.MenuBar.prototype.updateDefaultMenu = function() {
  this.setDefaultMenu(null);
  if (this._orderedMenuItems) {
    var found = this._updateDefaultMenuInItems(this._orderedMenuItems.right);
    if (!found) {
      this._updateDefaultMenuInItems(this._orderedMenuItems.left);
    }
  }
};

scout.MenuBar.prototype._updateDefaultMenuInItems = function(items) {
  var found = false;
  items.some(function(item) {
    if (item.visible && item.enabled && this._isDefaultKeyStroke(item.actionKeyStroke)) {
      this.setDefaultMenu(item);
      this.setTabbableMenu(item);
      found = true;
      return true;
    }
  }.bind(this));
  return found;
};

scout.MenuBar.prototype.setDefaultMenu = function(defaultMenu) {
  if (this.defaultMenu === defaultMenu) {
    return;
  }
  if (this.defaultMenu && this.defaultMenu.rendered) {
    this.defaultMenu.$container.removeClass('default-menu');
  }
  this._setProperty('defaultMenu', defaultMenu);
  if (this.defaultMenu && this.defaultMenu.rendered) {
    this.defaultMenu.$container.addClass('default-menu');
  }
};

scout.MenuBar.prototype._isDefaultKeyStroke = function(keyStroke) {
  return scout.isOneOf(scout.keys.ENTER, keyStroke.which) &&
    !keyStroke.ctrl &&
    !keyStroke.alt &&
    !keyStroke.shift;
};

scout.MenuBar.prototype._renderMenuItems = function(menuItems, right) {
  var tooltipPosition = (this.position === 'top' ? 'bottom' : 'top');
  menuItems.forEach(function(item) {
    // Ensure all all items are non-tabbable by default. One of the items will get a tabindex
    // assigned again later in updateItems().
    item.setTabbable(false);
    if (this.tabbableMenu === item) {
      this.tabbableMenu = undefined;
    }
    item.tooltipPosition = tooltipPosition;
    item.render(right ? this.$right : this.$left);
    item.$container.addClass('menubar-item');
    if (right) {
      // Mark as right-aligned
      item.rightAligned = true;
    }

    // Attach a propertyChange listener to the item, so the menubar can be updated when one of
    // its items changes (e.g. visible, keystroke etc.)
    item.on('propertyChange', this._menuItemPropertyChangeListener);
  }.bind(this));
};

scout.MenuBar.prototype._removeMenuItems = function() {
  this.menuItems.forEach(function(item) {
    item.off('propertyChange', this._menuItemPropertyChangeListener);
    item.overflow = false;
    item.remove();
  }, this);
};
