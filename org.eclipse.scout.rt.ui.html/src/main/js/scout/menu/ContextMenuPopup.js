scout.ContextMenuPopup = function(session, menuItems) {
  scout.ContextMenuPopup.parent.call(this, session);
  this.menuItems = menuItems;
};
scout.inherits(scout.ContextMenuPopup, scout.Popup);

scout.ContextMenuPopup.prototype._render = function($parent) {
  scout.ContextMenuPopup.parent.prototype._render.call(this, $parent);
  this._beforeRenderMenuItems();
  this._renderMenuItems();
  this._afterRenderMenuItems();
};

/**
 * Override this method to do something before menu items are rendered.
 * The default impl. does nothing.
 */
scout.ContextMenuPopup.prototype._beforeRenderMenuItems = function() {
};

/**
 * Override this method to do something before menu items are rendered.
 * The default impl. does nothing.
 */
scout.ContextMenuPopup.prototype._afterRenderMenuItems = function() {
};

/**
 * Override this method to return menu items or actions used to render menu items.
 */
scout.ContextMenuPopup.prototype._getMenuItems = function() {
  return this.menuItems;
};

scout.ContextMenuPopup.prototype._renderMenuItems = function() {
  var menus = this._getMenuItems();
  if (!menus || menus.length === 0) {
    return;
  }
  var menuClone;
  menus.forEach(function(menu) {
    if (!menu.visible || menu.separator) {
      return;
    }
    menuClone = menu.clone();
    menuClone.render(this.$body); // FIXME AWE: (menus) items nicht mehr in der vertikalen mitte
    menuClone.$container
      .on('click', '', this.closePopup.bind(this));

    var oldSendDoAction = menuClone.sendDoAction;
    var that = this;
    menuClone.sendDoAction = function(){
      oldSendDoAction.call(this);
      that.closePopup();
    };

  }.bind(this));
};

scout.ContextMenuPopup.prototype._createKeyStrokeAdapter = function() {
  return new scout.PopupMenuItemKeyStrokeAdapter(this);
};
