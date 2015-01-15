scout.MenuBar = function($parent, position, orderFunc) {
  this.position = position;
  this.orderFunc = orderFunc;
  this.menuItems = [];

  this.$container = $.makeDiv('menubar').hide();
  if (this.position === 'top') {
    $parent.prepend(this.$container);
  } else {
    this.$container.addClass('bottom');
    $parent.append(this.$container);
  }
};

// FIXME AWE: (Menubar) refactor to MenuBar everywhere
scout.MenuBar.prototype.updateItems = function(menuItems) {

  var i, orderedMenuItems;

  // stop if menus are the same as before
  if (scout.arrays.equals(this.menuItems, menuItems)) {
    return;
  }

  // remove existing menu items
  if (this.menuItems) {
    for (i = 0; i < this.menuItems.length; i++) {
      this.menuItems[i].remove();
    }
  }

  this.menuItems = menuItems;
  orderedMenuItems = this.orderFunc(this.menuItems);
  this._renderMenuItems(orderedMenuItems.left , false);
  this._renderMenuItems(orderedMenuItems.right, true);

//  // Fix for Firefox issue with float:right. In Firefox elements with float:right must
//  // come first in the HTML order of elements. Otherwise a strange layout bug occurs.
//  this.$container.children('.menu-right').
//    detach().
//    prependTo(this.$container);
//
//  // The _first_ menu-right must have the 'last' class (reverse order because of float:right)
//  this.$container.children('.menu-right').
//    first().
//    addClass('last');
//
//  if (this.lastItem && !this.lastItem.$container.hasClass('menu-right')) {
//    this.lastItem.$container.addClass('last');
//  }

//  if (!hasMenus) { // FIXME AWE für was ist das gut?
//    this.$container.hide();
//  } else {
    this.$container.show();
//  }
};

scout.MenuBar.prototype._renderMenuItems = function(menuItems, right) {
  for (var i = 0; i < menuItems.length; i++) {
    if (menuItems[i].separator) {
      this._renderMenuSeparator();
    } else {
      this._renderMenuItem(menuItems[i], right);
//      hasMenus = true;
    }
  }
};

scout.MenuBar.prototype._renderMenuItem = function(menuItem, right) {
  menuItem.tooltipPosition = this.position === 'top' ? 'bottom' : 'top';
  menuItem.render(this.$container);
  if (right) {
    menuItem.$container.addClass('menu-right');
  }
};

scout.MenuBar.prototype._renderMenuSeparator = function () {
  this.$container.appendDiv('menu-separator');
};
