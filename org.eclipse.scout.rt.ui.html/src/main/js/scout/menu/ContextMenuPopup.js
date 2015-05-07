scout.ContextMenuPopup = function(session, menuItems) {
  scout.ContextMenuPopup.parent.call(this, session);
  this.$head;
  this.$deco;
  this.menuItems = menuItems;
};
scout.inherits(scout.ContextMenuPopup, scout.Popup);

scout.ContextMenuPopup.prototype._render = function($parent) {
  scout.ContextMenuPopup.parent.prototype._render.call(this, $parent);
  var menuCopy, menus = this.menuItems;
  if (!menus || menus.length === 0) {
    return;
  }
  // FIXME AWE: (menus) see other similar occurrences
  menus.forEach(function(menu) {
    if (!menu.visible || menu.separator) {
      return;
    }
    // FIXME AWE: (menus) try to use a menu-widget and a menu-adpater like MessageBox and MessageBoxModelAdapter
    // what we do here: we create a clone of the existing menu and set the rendered property to false
    // so we can render the same adapter again. The ID is the same as the original, so when we click on
    // the copied item, the event is for the original adapter.
    menuCopy = $.extend({}, menu);
    menuCopy.rendered = false;
    menuCopy.render(this.$body); // FIXME AWE: (menus) items nicht mehr in der vertikalen mitte
    menuCopy.$container
      .on('click', '', this.closePopup.bind(this))
      .one(scout.menus.CLOSING_EVENTS, $.suppressEvent);

  }.bind(this));
};

scout.ContextMenuPopup.prototype._createKeyStrokeAdapter = function() {
  return new scout.PopupMenuItemKeyStrokeAdapter(this);
};
