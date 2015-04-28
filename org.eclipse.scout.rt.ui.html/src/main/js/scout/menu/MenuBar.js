scout.MenuBar = function($parent, position, orderFunc) {
  this.position = position;
  this.orderFunc = orderFunc;
  this.menuItems = [];
  this.$parent = $parent;
  this.keyStrokeAdapter;

  // Create a menubar container and add it to the parent, but don't show it yet. It will
  // be shown automatically when items are added to the menubar, see updateVisibility().
  this.$container = $.makeDiv('menubar').hide();
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

scout.MenuBar.prototype.updateItems = function(menuItems) {
  menuItems = scout.arrays.ensure(menuItems);

  // stop if menus are the same as before
  // remove separators before comparison, because orderFunc may add new separators (arrays.equals compares by reference (===))
  if (scout.arrays.equals(this.menuItems.filter(notIsSeparator), menuItems.filter(notIsSeparator))) {
    return;
  }

  // remove existing menu items
  this.menuItems.forEach(function(item) {
    item.remove();
  });

  // The orderFunc may add separators to the list of items, that's why we
  // store the return value of orderFunc in this.menuItems and not the
  // menuItems passed to the updateItems method. We must do this because
  // otherwise we could not remove the added separator later.
  var orderedMenuItems = this.orderFunc(menuItems);
  this.menuItems = orderedMenuItems.left.concat(orderedMenuItems.right);

  // Important: "right" items are rendered first! This is a fix for Firefox issue with
  // float:right. In Firefox elements with float:right must come first in the HTML order
  // of elements. Otherwise a strange layout bug occurs.
  this._renderMenuItems(orderedMenuItems.right, true);
  this._renderMenuItems(orderedMenuItems.left, false);

  //Add tabindex to first valid MenuItem
  for (var i = 0; i < this.menuItems.length; i++) {
    var actualItem = this.menuItems[i];
    if ((actualItem instanceof scout.Button || (actualItem instanceof scout.Menu && !actualItem.separator)) && actualItem.visible && actualItem.enabled) {
      if (actualItem instanceof scout.Button) {
        actualItem.$field.attr('tabindex', 0);
      } else {
        actualItem.$container.attr('tabindex', 0);
      }
      break;
    }
  }

  this.updateVisibility();

  // --- Helper functions ---

  function notIsSeparator(menu) {
    return !menu.separator;
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
    item.$container.removeClass('form-field');

    if (item instanceof scout.Button) {
      item.$field.attr('tabindex', -1);
    } else {
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
