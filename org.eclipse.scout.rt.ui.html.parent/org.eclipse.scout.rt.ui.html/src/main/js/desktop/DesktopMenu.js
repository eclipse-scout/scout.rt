scout.DesktopMenu = function($parent, session) {
  this.session = session;

  //create container
  this.$container = $parent.appendDiv('', 'desktop-menu').data('this', this);
  this.$tree = this.$container.appendDiv('', 'desktop-menu-tree');
  this.$table = this.$container.appendDiv('', 'desktop-menu-table');
};

scout.DesktopMenu.prototype.addItems = function(menus, tree, selection) {
  var $div;

  if (tree) {
    $div = this.$tree;
  } else {
    $div = this.$table;
  }

  $div.empty();

  if (menus && menus.length > 0) {
    for (var i = 0; i < menus.length; i++) {
      if (menus[i].separator) {
        continue;
      }
      $div.appendDiv('', 'menu-item', menus[i].text)
        .attr('data-icon', menus[i].iconId)
        .data('menu', menus[i])
        .on('click', '', onMenuItemClicked);
    }

  }

  //TODO cru: border is visible even if there are no tree menus
  // size menu
  $div.widthToContent(150);

  var that = this;

  function onMenuItemClicked() {
    var menu = $(this).data('menu');
    menu.sendMenuAction();
  }
};
