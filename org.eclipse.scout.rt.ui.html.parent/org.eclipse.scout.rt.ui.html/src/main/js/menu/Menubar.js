scout.DesktopMenubar = function($parent) {
  //create container
  this.$container = $parent.appendDiv('', 'desktop-menu').data('this', this);
  this.$tree = this.$container.appendDiv('', 'desktop-menu-tree');
  this.$table = this.$container.appendDiv('', 'desktop-menu-table');
};

scout.DesktopMenubar.prototype.updateItems = function(group, menus) {
  var $div, i, existingMenus;

  if (group === 'tree') {
    $div = this.$tree;
  } else {
    $div = this.$table;
  }

  existingMenus = $div.data('menus');
  if (scout.arrays.equals(existingMenus, menus)) {
    return;
  }

  if (existingMenus) {
    for (i = 0; i < existingMenus.length; i++) {
      existingMenus[i].remove();
    }
  }
  $div.data('menus', []);

  if (menus && menus.length > 0) {
    for (i = 0; i < menus.length; i++) {
      if (menus[i].separator) {
        continue;
      }
      menus[i].render($div);
      $div.data('menus').push(menus[i]);
    }
  }

  //TODO cru: border is visible even if there are no tree menus
  // size menu
  $div.widthToContent(150);
};
