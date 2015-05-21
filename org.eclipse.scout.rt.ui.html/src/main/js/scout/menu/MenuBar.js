scout.MenuBar = function($parent, position, session, menuSorter) {
  this.$parent = $parent;
  this.position = position;
  this.session = session;
  this.menuSorter = menuSorter;
  this.menuItems = [];

  /**
   * This array is === menuItems when menu-bar is not over-sized.
   * When the menu-bar is over-sized, we this property is set be the MenuBarLayout
   * which adds an additional ellipsis-menu, and removes menu items that doesn't
   * fit into the available menu-bar space.
   */
  this.visibleMenuItems = [];
  this.keyStrokeAdapter;

  // Create a menu-bar container and add it to the parent, but don't show it yet. It will
  // be shown automatically when items are added to the menu-bar, see updateVisibility().
  this.$container = $.makeDiv('menubar').hide();
  var htmlComp = new scout.HtmlComponent(this.$container, this.session);
  htmlComp.setLayout(new scout.MenuBarLayout(this));

  if (this.position === 'top') {
    this.$parent.prepend(this.$container);
  } else {
    this.$container.addClass('bottom');
    this.$parent.append(this.$container);
  }
};

scout.MenuBar.prototype.remove = function() {
  this.menuItems.forEach(function(item) {
    item.remove();
  });
  if (this.$container) {
    this.$container.remove();
  }
};

// FIXME AWE: (menu) check if the force parameter is really required
scout.MenuBar.prototype.updateItems = function(menuItems, force) {
  menuItems = scout.arrays.ensure(menuItems);

  // stop if menus are the same as before
  // remove separators before comparison, because menuSorter may add new separators (arrays.equals compares by reference (===))
  if (!force && scout.arrays.equals(this.menuItems.filter(isNotSeparator), menuItems.filter(isNotSeparator))) {
    return;
  }

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

  // Add tabindex 0 to first valid MenuItem so that it can be focused. All other items
  // are not tabbable. They can be selected with the arrow keys.
  this.menuItems.some(function(item) {
    if (item.enabled && item.visible && (item instanceof scout.Button || isNotSeparator(item))) {
      var $target = (item instanceof scout.Button ? item.$field : item.$container);
      $target.attr('tabindex', 0);
      return true;
    } else {
      return false;
    }
  });

  this.updateVisibility();

  // --- Helper functions ---

  function isNotSeparator(item) {
    if (item instanceof scout.Menu) {
      return !item.separator;
    } else {
      return true;
    }
  }
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
  var wasVisible = this.$container.isVisible();

  // Calculate new visibility of the menu-bar
  var visible = !this.hiddenByUi && this.menuItems.length > 0;

  if (visible !== wasVisible) {
    // Update visibility
    this.$container.setVisible(visible);

    // Update layout
    var htmlComp = scout.HtmlComponent.optGet(this.$parent);
    if (htmlComp) {
      htmlComp.invalidateTree();
    }

    // Update keystrokes
    if (visible) {
      this._registerKeyStrokeAdapter();
    } else {
      this._unregisterKeyStrokeAdapter();
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
  var foundLastItem = false;
  menuItems.forEach(function(item) {
    item.tooltipPosition = tooltipPosition;
    item.render(this.$container);
    item.menuBar = this; // link to menuBar
    item.$container.removeClass('form-field'); // FIXME AWE: do this removeClass in Menu.js when menuBar is set

    // Ensure all all items are non-tabbable by default. One of the items will get a tabindex
    // assigned again later in updateItems().
    if (item instanceof scout.Button && item.$field.is('button')) {
      // <button>s are tabbable by default, therefore explicitly disable it by setting the tabindex to -1
      item.$field.attr('tabindex', -1);
    } else if (item instanceof scout.Button){
      item.$field.removeAttr('tabindex');
    } else {
      // For all other items we can just remove the attribute to make them non-tabbable
      item.$container.removeAttr('tabindex');
    }

    if (right) {
      // Mark as right-aligned
      item.rightAligned = true;
      item.$container.addClass('right-aligned');
      // Mark the first visible item as last item (inverse order due to 'float: right')
      if (!foundLastItem && item.visible) {
        item.$container.addClass('last');
        foundLastItem = true;
      }
    }
  }.bind(this));
};

scout.MenuBar.prototype._registerKeyStrokeAdapter = function() {
  if (!this.keyStrokeAdapter) {
    this.keyStrokeAdapter = new scout.MenuBarKeyStrokeAdapter(this);
  }
  scout.keyStrokeManager.installAdapter(this.$container, this.keyStrokeAdapter);
};

scout.MenuBar.prototype._unregisterKeyStrokeAdapter = function() {
  if (this.keyStrokeAdapter) {
    scout.keyStrokeManager.uninstallAdapter(this.keyStrokeAdapter);
  }
};
