scout.Menubar = function($parent) {
  //create container
  this.$container = $parent.prependDiv('', 'menubar');
};

scout.Menubar.prototype.updateItems = function(menus) {
  var i, existingMenus;

  existingMenus = this.$container.data('menus');
  if (scout.arrays.equals(existingMenus, menus)) {
    return;
  }

  if (existingMenus) {
    for (i = 0; i < existingMenus.length; i++) {
      existingMenus[i].remove();
    }
  }
  this.$container.data('menus', []);

  if (menus && menus.length > 0) {
    for (i = 0; i < menus.length; i++) {
      if (menus[i].separator) {
        continue;
      }
      menus[i].render(this.$container);
      this.$container.data('menus').push(menus[i]);
    }
  }

  //TODO cru: border is visible even if there are no tree menus
  // size menu
  this.$container.widthToContent(150);
};
