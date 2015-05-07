// FIXME AWE: move to other package
scout.ContextMenuPopup = function(session, menuItems) {
  scout.ContextMenuPopup.parent.call(this, session);
  this.$head;
  this.$deco;
  this.menuItems = menuItems;
};
scout.inherits(scout.ContextMenuPopup, scout.Popup);

scout.ContextMenuPopup.prototype._render = function($parent) {
  scout.ContextMenuPopup.parent.prototype._render.call(this, $parent);
  var i, menu, menus = this.menuItems;
  if (!menus || menus.length === 0) {
    return;
  }
  for (i = 0; i < menus.length; i++) {
    menu = menus[i];
    if (!menu.visible) {
      continue;
    }
    if (menu.separator) {
      continue;
    }
    menu.sendAboutToShow();
    menu.render(this.$body); // FIXME AWE: menu item nicht mehr in der mitte
    menu.$container
      .on('click', '', this.onMenuItemClicked.bind(this, menu))
      .one(scout.menus.CLOSING_EVENTS, $.suppressEvent);
   }
};

scout.ContextMenuPopup.prototype.onMenuItemClicked = function(menu) {
  this.closePopup();
  menu.sendDoAction();
};

scout.ContextMenuPopup.prototype._createKeyStrokeAdapter = function() {
  return new scout.PopupMenuItemKeyStrokeAdapter(this);
};
