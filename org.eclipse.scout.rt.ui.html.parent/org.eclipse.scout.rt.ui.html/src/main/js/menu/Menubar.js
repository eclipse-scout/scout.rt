scout.Menubar = function($parent) {
  //create container
  this.$container = $parent.prependDiv('', 'menubar');
  this.menus = [];
  this.lastMenu;
  this.menuTypesForLeft1 = [];
  this.menuTypesForLeft2 = [];
  this.menuTypesForRight = [];
  this.staticMenus = [];
};

scout.Menubar.prototype.updateItems = function(menus) {
  var i, left1Menus, left2Menus, hasLeft1Menus, rightMenus;

  menus = this.staticMenus.concat(menus);

  // stop if menus are the same as before
  if (scout.arrays.equals(this.menus, menus)) {
    return;
  }

  // remove existing menus
  if (this.menus) {
    for (i = 0; i < this.menus.length; i++) {
      this.menus[i].remove();
    }
    this.menus = [];
  }

  // in case of no menus: finish
  if (!menus || menus.length === 0) return;

  // add menus for the first left area
  left1Menus = scout.menus.filter(menus, this.menuTypesForLeft1);
  hasLeft1Menus = left1Menus && left1Menus.length > 0;
  if (hasLeft1Menus) {
    for (i = 0; i < left1Menus.length; i++) {
      this._addMenuItem(left1Menus[i]);
    }
  }

  // add all other menus
  left2Menus = scout.menus.filter(menus, this.menuTypesForLeft2);
  rightMenus = scout.menus.filter(menus, this.menuTypesForRight);

  // It is not possible to display the same menu twice. If menuTypes for left and right are specified, prefer left
  menus = left2Menus.concat(rightMenus);
  scout.arrays.removeAll(menus, left1Menus);

  // add a fixed separator between left1 and left2, if required
  if (hasLeft1Menus && left2Menus.length > 0 && this.lastMenu && !this.lastMenu.hasButtonStyle()) {
    this._addMenuSeparator();
  }

  for (i = 0; i < menus.length; i++) {
    if (menus[i].separator) {
      this._addMenuSeparator();
    } else {
      this._addMenuItem(menus[i]);
    }
  }

  // TODO AWE: (menu) 'last' is not added to correct right aligned menu
  // this._addLastClass();

  // Fix for Firefox issue with float:right. In Firefox elements with float:right must
  // come first in the HTML order of elements. Otherwise a strange layout bug occurs.
  this.$container.children('.menu-right').
    detach().
    prependTo(this.$container);
};

scout.Menubar.prototype._addMenuItem = function(menu) {
  menu.render(this.$container);
  if (scout.menus.checkType(menu, this.menuTypesForRight)) {
    menu.$container.addClass('menu-right');
  }
  this.menus.push(menu);
  this.lastMenu = menu;
};

scout.Menubar.prototype._addMenuSeparator = function () {
  var s = this.$container.appendDIV('menu-separator');
  this.menus.push(s);
  this._addLastClass();
};

/**
 * Add 'last' class to each previous menu-item when:
 * - after a menu-separator has been added
 * - after all menu items have been added
 */
scout.Menubar.prototype._addLastClass = function () {
  if (this.lastMenu) {
    this.lastMenu.$container.addClass('last');
  }
};
