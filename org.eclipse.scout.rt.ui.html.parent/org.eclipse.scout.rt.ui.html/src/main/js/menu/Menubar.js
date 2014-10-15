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
  var i, leftMenus;

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
  leftMenus = scout.menus.filter(menus, this.menuTypesForLeft1);
  if (leftMenus && leftMenus.length > 0) {
    for (i = 0; i < leftMenus.length; i++) {
      this._addMenuItem(leftMenus[i]);
    }
    if (i > 1) {
      this._addMenuSeparator();
    }
  }

  // add all other menus
  menus = scout.menus.filter(menus, this.menuTypesForLeft2.concat(this.menuTypesForRight));
  // It is not possible to display the same menu twice. If menuTypes for left and right are specified, prefer left
  scout.arrays.removeAll(menus, leftMenus);
  for (i = 0; i < menus.length; i++) {
    if (menus[i].separator) {
//      this._addMenuSeparator();
    } else {
      this._addMenuItem(menus[i]);
    }
  }
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
  if (this.lastMenu) {
    this.lastMenu.$container.addClass('last');
  }
};
