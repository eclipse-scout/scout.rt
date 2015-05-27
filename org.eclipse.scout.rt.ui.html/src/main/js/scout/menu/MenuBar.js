scout.MenuBar = function(session, menuSorter) {
  scout.MenuBar.parent.call(this, false);

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
  this.menuItems.forEach(function(item) {
    item.remove();
  });
  if (this.$container) {
    this.$container.remove();
    this.$container = undefined;
  }
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
  // remove existing menu items
  this.menuItems.forEach(function(item) {
    item.remove();
  });

  // The menuSorter may add separators to the list of items, that's why we
  // store the return value of menuSorter in this.menuItems and not the
  // menuItems passed to the updateItems method. We must do this because
  // otherwise we could not remove the added separator later.
  var orderedMenuItems = this.menuSorter.order(menuItems);
  this.menuItems = orderedMenuItems.left.concat(orderedMenuItems.right);

  // Important: "right" items are rendered first! This is a fix for Firefox issue with
  // float:right. In Firefox elements with float:right must come first in the HTML order
  // of elements. Otherwise a strange layout bug occurs.
  this._renderMenuItems(orderedMenuItems.right, true);
  this._renderMenuItems(orderedMenuItems.left, false);
  if (this._lastVisibleItem) {
    this._lastVisibleItem.$container.addClass('last');
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
    item.tooltipPosition = tooltipPosition;
    item.render(this.$container);
    item.menuBar = this; // link to menuBar // FIXME AWE: check if really needed
    // Ensure all all items are non-tabbable by default. One of the items will get a tabindex
    // assigned again later in updateItems().
    item.setTabbable(false);

    if (right) {
      // Mark as right-aligned
      item.rightAligned = true;
      item.$container.addClass('right-aligned');
      // Mark the first visible item as last item (inverse order due to 'float: right')
    }
    if (item.visible) {
      this._lastVisibleItem = item;
    }
  }.bind(this));
};

