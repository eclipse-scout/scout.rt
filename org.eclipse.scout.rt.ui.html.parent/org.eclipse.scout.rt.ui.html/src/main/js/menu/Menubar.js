scout.Menubar = function($parent, options) {
  options = options || {};
  var position = options.position || 'top';

  this.menus = [];
  this.lastMenu;
  this.menuTypesForLeft1 = [];
  this.menuTypesForLeft2 = [];
  this.menuTypesForRight = [];
  this.staticMenus = [];

  this.$container = $.makeDIV('menubar').hide();
  if (position === 'top') {
    $parent.prepend(this.$container);
  } else {
    this.$container.addClass('bottom');
    $parent.append(this.$container);
  }
};

scout.Menubar.prototype.updateItems = function(menus) {
  var i, left1Menus, left2Menus, hasLeft1Menus, rightMenus, hasMenus;

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

  // FIXME AWE HACK [begin]
  // find Anzeigen menu, move to group left1Menus and change style
  var menu;
  for (i = 0; i < menus.length; i++) {
    menu = menus[i];
    if ('Anzeigen' === menu.text) {
      menu.menuTypes = ['Table.EmptySpace', 'Form.System'];
      menu.defaultMenu = true;
      break;
    }
  }
  // HACK [end]

  // add menus for the first left area
  left1Menus = scout.menus.filter(menus, this.menuTypesForLeft1);
  hasLeft1Menus = left1Menus && left1Menus.length > 0;
  if (hasLeft1Menus) {
    for (i = 0; i < left1Menus.length; i++) {
      this._addMenuItem(left1Menus[i]);
      hasMenus = true;
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
      hasMenus = true;
    }
  }

  // Fix for Firefox issue with float:right. In Firefox elements with float:right must
  // come first in the HTML order of elements. Otherwise a strange layout bug occurs.
  this.$container.children('.menu-right').
    detach().
    prependTo(this.$container);

  // The _first_ menu-right must have the 'last' class (reverse order because of float:right)
  this.$container.children('.menu-right').
    first().
    addClass('last');

  if (this.lastMenu && !this.lastMenu.$container.hasClass('menu-right')) {
    this.lastMenu.$container.addClass('last');
  }

  if (!hasMenus) {
    this.$container.hide();
  } else {
    this.$container.show();
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
};
