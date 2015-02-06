scout.MenuBar = function($parent, position, orderFunc) {
  this.position = position;
  this.orderFunc = orderFunc;
  this.menuItems = [];
  this.$parent = $parent;
  this.$container = $.makeDiv('menubar');
  if (this.position === 'top') {
    $parent.prepend(this.$container);
  } else {
    this.$container.addClass('bottom');
    $parent.append(this.$container);
  }
};

scout.MenuBar.prototype.remove = function() {
  this.menuItems.forEach(function(item) {
    item.remove();
  });
};

scout.MenuBar.prototype.updateItems = function(menuItems) {
  var i, orderedMenuItems;

  // stop if menus are the same as before
  if (scout.arrays.equals(this.menuItems, menuItems)) {
    this._updateVisibility();
    return;
  }

  // remove existing menu items
  if (this.menuItems) {
    for (i = 0; i < this.menuItems.length; i++) {
      this.menuItems[i].remove();
    }
  }

  /* The orderFunc may add separators to the list of items, that's why we
   * store the return value of orderFunc in this.menuItems and not the
   * menuItems passed to the updateItems method. We must do this because
   * otherwise we could not remove the added separator later.
   */
  orderedMenuItems = this.orderFunc(menuItems);
  this.menuItems = orderedMenuItems.left.concat(orderedMenuItems.right);
  this._renderMenuItems(orderedMenuItems.left, false);
  this._renderMenuItems(orderedMenuItems.right, true);

  // Fix for Firefox issue with float:right. In Firefox elements with float:right must
  // come first in the HTML order of elements. Otherwise a strange layout bug occurs.
  this.$container.children('.menu-right').
  detach().
  prependTo(this.$container);

  // The _first_ menu-right must have the 'last' class (reverse order because of float:right)
  this.$container.children('.menu-right').
  first().
  addClass('last');

  // FIXME AWE: (menu) check if this code is still needed
  // if (this.lastItem && !this.lastItem.$container.hasClass('menu-right')) {
  //    this.lastItem.$container.addClass('last');
  //  }

  this._updateVisibility();
};

scout.MenuBar.prototype._updateVisibility = function() {
  if (this.menuItems.length === 0) {
    this.$container.hide();
  } else {
    this.$container.show();
  }
  var htmlComp = scout.HtmlComponent.optGet(this.$parent);
  if (htmlComp) {
    htmlComp.revalidate();
  }
};

scout.MenuBar.prototype._renderMenuItems = function(menuItems, right) {
  menuItems.forEach(function(item) {
    item.tooltipPosition = this.position === 'top' ? 'bottom' : 'top';
    item.render(this.$container);
    item.$container.removeClass('form-field');
    if (right) {
      item.$container.addClass('menu-right');
    }
  }.bind(this));
};
