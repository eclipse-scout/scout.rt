scout.MenuBar = function(session, menuSorter) {
  scout.MenuBar.parent.call(this);

  this.session = session;
  this.menuSorter = menuSorter;
  this.position = 'top'; // or 'bottom'
  this.size = 'small'; // or 'large'
  this.tabbable = true;
  this.menuItems = [];

  /**
   * This array is === menuItems when menu-bar is not over-sized.
   * When the menu-bar is over-sized, we this property is set be the MenuBarLayout
   * which adds an additional ellipsis-menu, and removes menu items that doesn't
   * fit into the available menu-bar space.
   */
  this.visibleMenuItems = [];

  this.keyStrokeAdapter = this._createKeyStrokeAdapter();

  this._menuItemPropertyChangeListener = function() {
    // We do not update the items directly, because this listener may be fired many times in one
    // user request (because many menus change one or more properties). Therefore, we just invalidate
    // the MenuBarLayout. It will be updated automatically after the user request has finished,
    // because the layout calls rebuildItems().
    scout.HtmlComponent.get(this.$container).invalidateTree();
  }.bind(this);
};
scout.inherits(scout.MenuBar, scout.Widget);

/**
 * @implements Widgets.js
 */
scout.MenuBar.prototype._createKeyStrokeAdapter = function() {
  return new scout.MenuBarKeyStrokeAdapter(this);
};

/**
 * @override Widget.js
 */
scout.MenuBar.prototype._render = function($parent) {
  // Visibility may change when updateItems() function is called, see updateVisibility().
  this.$container = $.makeDiv('menubar')
    .attr('id', 'MenuBar-' + scout.createUniqueId())
    .toggleClass('main-menubar', this.size === 'large')
    .setVisible(this.menuItems.length > 0);

  var htmlComp = new scout.HtmlComponent(this.$container, this.session);
  htmlComp.setLayout(new scout.MenuBarLayout(this));

  if (this.position === 'top') {
    $parent.prepend(this.$container);
  } else {
    this.$container.addClass('bottom');
    $parent.append(this.$container);
  }
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

scout.MenuBar.prototype._remove = function() {
  scout.MenuBar.parent.prototype._remove.call(this);
  this.menuItems.forEach(function(item) {
    item.off('propertyChange', this._menuItemPropertyChangeListener);
    item.remove();
  });
};

scout.MenuBar.prototype.rebuildItems = function() {
  this._updateItems(this.menuItems);
};

scout.MenuBar.prototype.updateItems = function(menuItems) {
  menuItems = scout.arrays.ensure(menuItems);

  // stop if menus are the same as before
  // remove separators before comparison, because menuSorter may add new separators (arrays.equals compares by reference (===))
  if (scout.arrays.equals(this.menuItems.filter(isNotSeparator), menuItems.filter(isNotSeparator))) {
    // NOP
  } else {
    this._updateItems(menuItems);
    // Re-layout menubar (because items might have changed)
    scout.HtmlComponent.get(this.$container).invalidateTree();
  }

  function isNotSeparator(item) {
    if (item instanceof scout.Menu) {
      return !item.separator;
    } else {
      return true;
    }
  }
};

scout.MenuBar.prototype._updateItems = function(menuItems) {
  // remove DOM for existing menu items and destroy items that have
  // been created by the menuSorter (e.g. separators).
  this.menuItems.forEach(function(item) {
    item.off('propertyChange', this._menuItemPropertyChangeListener);
    if (item.createdBy === this.menuSorter) {
      item.destroy();
    } else {
      item.overflow = false;
      item.remove();
    }
  }, this);

  // The menuSorter may add separators to the list of items, that's why we
  // store the return value of menuSorter in this.menuItems and not the
  // menuItems passed to the updateItems method. We must do this because
  // otherwise we could not remove the added separator later.
  var orderedMenuItems = this.menuSorter.order(menuItems, this);
  this.menuItems = orderedMenuItems.left.concat(orderedMenuItems.right);
  this.visibleMenuItems = this.menuItems;
  this._lastVisibleItemLeft = null;
  this._lastVisibleItemRight = null;
  this._defaultMenu = null;

  // Important: "right" items are rendered first! This is a fix for Firefox issue with
  // float:right. In Firefox elements with float:right must come first in the HTML order
  // of elements. Otherwise a strange layout bug occurs.
  this._renderMenuItems(orderedMenuItems.right, true);
  this._renderMenuItems(orderedMenuItems.left, false);
  var lastVisibleItem = this._lastVisibleItemRight || this._lastVisibleItemLeft;
  if (lastVisibleItem) {
    lastVisibleItem.$container.addClass('last');
  }
  if (this._defaultMenu) {
    this._defaultMenu.$container.addClass('default-menu');
  }

  // Make first valid MenuItem tabbable so that it can be focused. All other items
  // are not tabbable. But they can be selected with the arrow keys.
  if (this.tabbable) {
    this.menuItems.some(function(item) {
      if (item.isTabTarget()) {
        item.setTabbable(true);
        return true;
      } else {
        return false;
      }
    });
  }

  this.updateVisibility();
};

/**
 * Ensures that the last visible right-aligned item has the class 'last' (to remove the margin-right).
 * Call this method whenever the visibility of single items change. The 'last' class is assigned
 * initially in _renderMenuItems().
 */
scout.MenuBar.prototype.updateLastItemMarker = function() {
  // Remove the last class from all items
  this.$container.children('.last').removeClass('last');
  // Find last visible right aligned menu item
  var lastMenuItem;
  for (var i = 0; i < this.menuItems.length; i++) {
    var menuItem = this.menuItems[i];
    if (menuItem.rightAligned && menuItem.visible) {
      lastMenuItem = menuItem;
    }
  }
  // Assign the class to the found item
  if (lastMenuItem) {
    lastMenuItem.$container.addClass('last');
  }
};

scout.MenuBar.prototype.updateVisibility = function() {
  var htmlComp = scout.HtmlComponent.get(this.$container),
    oldVisible = htmlComp.isVisible(),
    visible = !this.hiddenByUi && this.menuItems.length > 0;

  // Update visibility, layout and key-strokes
  if (visible !== oldVisible) {
    this.$container.setVisible(visible);
    htmlComp.invalidateTree();
    if (visible) {
      this._installKeyStrokeAdapter();
    } else {
      this._uninstallKeyStrokeAdapter();
    }
  }
};

scout.MenuBar.prototype._renderMenuItems = function(menuItems, right) {
  // Reverse the list if alignment is right to preserve the visible order specified by the
  // Scout model (in HTML, elements with 'float: right' are displayed in reverse order)
  if (right) {
    menuItems.reverse();
  }
  var tooltipPosition = (this.position === 'top' ? 'bottom' : 'top');
  menuItems.forEach(function(item) {
    // Ensure all all items are non-tabbable by default. One of the items will get a tabindex
    // assigned again later in updateItems().
    item.setTabbable(false);
    item.tooltipPosition = tooltipPosition;
    item.render(this.$container);
    if (right) {
      // Mark as right-aligned
      item.rightAligned = true;
      item.$container.addClass('right-aligned');
      if (item.visible && !this._lastVisibleItemRight) {
        this._lastVisibleItemRight = item;
      }
    }
    else {
      if (item.visible) {
        this._lastVisibleItemLeft = item;
      }
    }
    // First rendered item that is enabled and reacts to ENTER keystroke shall be marked as 'defaultMenu'
    if (!this._defaultMenu && item.visible && item.enabled && item.keyStrokeKeyPart === scout.keys.ENTER) {
      this._defaultMenu = item;
    }
    // Attach a propertyChange listener to the item, so the menubar can be updated when one of
    // its items changes (e.g. visible, keystroke etc.)
    item.on('propertyChange', this._menuItemPropertyChangeListener);
  }.bind(this));
};

