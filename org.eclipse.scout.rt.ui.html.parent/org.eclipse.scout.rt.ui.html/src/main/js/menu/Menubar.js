scout.Menubar = function($parent) {
  //create container
  this.$container = $parent.prependDiv('', 'menubar');
  this.menus = [];
  this.lastMenu;
};

scout.Menubar.prototype.updateItems = function(menus) {
  var i, empty;

  // stop if menu the same as before
  if (scout.arrays.equals(this.menus, menus)) {
    return;
  }

  // remove existing menu
  if (this.menus) {
    for (i = 0; i < this.menus.length; i++) {
      this.menus[i].remove();
    }
    this.menus = [];
  }

  // in case of noe menu: finish
  if (!menus || menus.length === 0) return;

  // add empty space menu
  empty = scout.menus.filter(menus, ['Table.EmptySpace']);
  if (empty && empty.length > 0) {
    for (i = 0; i < empty.length; i++) {
      this._addMenuItem(empty[i]);
    }
    this._addMenuSeparator();
  }

  // add all other menues
  for (i = 0; i < menus.length; i++) {
    if (menus[i].separator) {
      this._addMenuSeparator();
    } else if (!scout.menus.checkType(menus[i], ['Table.EmptySpace'])){
      this._addMenuItem(menus[i]);
    }
  }
};

scout.Menubar.prototype._addMenuItem = function(menu) {
  menu.render(this.$container);
  this.menus.push(menu);
  this.lastMenu = menu;
};

scout.Menubar.prototype._addMenuSeparator = function () {
  this.lastMenu.$container.addClass('menu-separator');
};